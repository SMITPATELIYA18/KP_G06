package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import services.github.GitHubAPI;
import play.cache.AsyncCacheApi;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles sending/receiving data/messages to/from clients. Acts as a supervisor to all other actors - creates ana manages them
 * @author Pradnya Kandarkar
 */
public class SupervisorActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef wsOut;
    private GitHubAPI gitHubAPIInst;
    private final AsyncCacheApi asyncCacheApi;

    final Map<String, ActorRef> queryToSearchActor = new HashMap<String, ActorRef>();
    private ActorRef userProfileActor = null;
    private ActorRef repositoryProfileActor = null;
    private ActorRef issueStatActor = null;

    /**
     * @param wsOut For sending data/messages to the client
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface, for making external API calls to GitHub
     * @param asyncCacheApi For temporary data storage
     */
    public SupervisorActor(final ActorRef wsOut, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        this.wsOut =  wsOut;
        this.gitHubAPIInst = gitHubAPIInst;
        this.asyncCacheApi = asyncCacheApi;
    }

    /**
     * Creates an actor with properties specified using parameters
     * @param wsout For sending data/messages to the client
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface, for making external API calls to GitHub
     * @param asyncCacheApi For temporary storing data
     * @return A <code>Props</code> object holding actor configuration
     * @author Pradnya Kandarkar
     */
    public static Props props(final ActorRef wsout, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        return Props.create(SupervisorActor.class, wsout, gitHubAPIInst, asyncCacheApi);
    }

    /**
     * Executes before any other action related to this actor
     * @author Pradnya Kandarkar
     */
    @Override
    public void preStart() {
        System.out.println("Created a session actor.");
    }

    /**
     * Handles incoming messages for this actor - matches the class of an incoming message and takes appropriate action
     * @return <code>AbstractActor.Receive</code> defining the messages that can be processed by this actor and how they will be processed
     * @author Pradnya Kandarkar
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, this::processRequest)
                .match(Messages.SearchResult.class, searchResult -> wsOut.tell(searchResult.searchResult, self()))
                .match(Messages.UserProfileInfo.class, userProfileInfo -> wsOut.tell(userProfileInfo.userProfileResult, self()))
                .match(Messages.RepositoryProfileInfo.class, repositoryProfileInfo -> wsOut.tell(repositoryProfileInfo.repositoryProfileResult, self()))
                .match(Messages.IssueStatInfo.class, issueStatInfo -> wsOut.tell(issueStatInfo.issueModel, self()))
                .matchAny(other -> log.error("Received unknown message type: " + other.getClass()))
                .build();
    }

    /**
     * Processes JSON data received from client requests, and creates actors to handle the requests accordingly
     * @param receivedJson <code>JsonNode</code> object holding the client request
     * @author Pradnya Kandarkar
     */
    private void processRequest(JsonNode receivedJson) {
        if(receivedJson.has("search_query")) {
            /* For "search_query" requests, gets the query string and checks id there is a search actor created to handle
            * this query. If no search actor is created, starts a new search actor and sends it a message to start
            * tracking this search query. A new actor is created for every new search query and added to
            * "queryToSearchActor" map which holds the (query, searchActor) mappings. */
            String searchQuery = receivedJson.get("search_query").asText();
            ActorRef searchActor = queryToSearchActor.get(searchQuery);
            if (searchActor == null) {
                log.info("Creating search actor for {}.", searchQuery);
                searchActor = getContext().actorOf(SearchActor.props(self(), searchQuery, this.gitHubAPIInst));
                queryToSearchActor.putIfAbsent(searchQuery, searchActor);
            }
            searchActor.tell(new Messages.TrackSearch(searchQuery, "fromSupervisor"), getSelf());
        } else if(receivedJson.has("user_profile")) {
            /* For "user_profile" requests, checks if a "UserProfileActor" is already created. If no actor exists,
            * creates a new "UserProfileActor" to handle all "user_profile" requests and sends it the current request
            * to get the user profile information. */
            String username = receivedJson.get("user_profile").asText();
            if(userProfileActor == null) {
                log.info("Creating a user profile actor.");
                userProfileActor = getContext().actorOf(UserProfileActor.props(self(), this.gitHubAPIInst, this.asyncCacheApi));
            }
            userProfileActor.tell(new Messages.GetUserProfile(username), getSelf());
        } else if(receivedJson.has("repository_profile")) {
            String repositoryName = receivedJson.get("repository_profile").asText();
            String username = receivedJson.get("username").asText();
            if(repositoryProfileActor == null) {
                log.info("Creating a repository profile actor.");
                repositoryProfileActor = getContext().actorOf(RepositoryProfileActor.props(self(), this.gitHubAPIInst, this.asyncCacheApi));
            }
            repositoryProfileActor.tell(new Messages.GetRepositoryProfileActor(username, repositoryName), getSelf());
        } else if(receivedJson.has("issues")) {
        	String repoFullName = receivedJson.get("repoFullName").asText();
        	if(issueStatActor == null) {
        		log.info("Creating a Issue Stat info");
        		issueStatActor  =getContext().actorOf(IssueStatActor.props(self(), this.gitHubAPIInst));
        	}
        	issueStatActor.tell(new Messages.GetRepositoryIssueActor(repoFullName), getSelf());
        }
        //TODO: Else condition
    }
}
