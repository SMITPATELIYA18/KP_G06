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

public class SupervisorActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef ws;
    private GitHubAPI gitHubAPIInst;
    private final AsyncCacheApi asyncCacheApi;
    final Map<String, ActorRef> queryToSearchActor = new HashMap<String, ActorRef>();
    private ActorRef userProfileActor;

    public SupervisorActor(final ActorRef wsOut, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        ws =  wsOut;
        this.gitHubAPIInst = gitHubAPIInst;
        this.asyncCacheApi = asyncCacheApi;
    }

    public static Props props(final ActorRef wsout, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        return Props.create(SupervisorActor.class, wsout, gitHubAPIInst, asyncCacheApi);
    }

    static public class TimeMessage {
        public final String time;
        public TimeMessage(String time) {
            this.time = time;
        }
    }


    @Override
    public void preStart() {
        System.out.println("Created a session actor.");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, this::processRequest)
                .match(Messages.SearchResult.class, searchResult -> ws.tell(searchResult.searchResult, self()))
                .match(Messages.UserProfileInfo.class, userProfileInfo -> ws.tell(userProfileInfo.userProfileResult, self()))
                .build();
    }

    private void processRequest(JsonNode receivedJson) {
        if(receivedJson.has("search_query")) {
            // System.out.println("Received search query: " + receivedJson.get("search_query").asText());
            String searchQuery = receivedJson.get("search_query").asText();
            ActorRef searchActor = queryToSearchActor.get(searchQuery);
            if (searchActor == null) {
                log.info("Creating search actor for {}.", searchQuery);
                searchActor = getContext().actorOf(SearchActor.props(self(), searchQuery, this.gitHubAPIInst));
                queryToSearchActor.putIfAbsent(searchQuery, searchActor);
            }
            searchActor.tell(new Messages.TrackSearch(searchQuery, "fromSupervisor"), getSelf());
        } else if(receivedJson.has("user_profile")) {
            String username = receivedJson.get("user_profile").asText();
            if(userProfileActor == null) {
                log.info("Creating a user profile actor.");
                userProfileActor = getContext().actorOf(UserProfileActor.props(self(), this.gitHubAPIInst, this.asyncCacheApi));
            }
            userProfileActor.tell(new Messages.GetUserProfile(username), getSelf());
        }
    }
}
