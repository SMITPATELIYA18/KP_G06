package actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.RepositoryModel;
import models.SearchRepository;
import scala.concurrent.duration.Duration;
import services.github.GitHubAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles all search repositories feature related requests
 * @author Pradnya Kandarkar
 */
public class SearchActor extends AbstractActorWithTimers {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private ActorRef supervisorActor;
    private String trackedSearchQuery;
    private GitHubAPI gitHubAPIInst;

    private List<RepositoryModel> baseSearchResults;
    private ObjectNode searchResultJson;

    /**
     * @param supervisorActor Actor reference for the supervisor actor
     * @param trackedSearchQuery Search query for which this actor is responsible
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface, used to make external API calls to GitHub
     */
    public SearchActor(ActorRef supervisorActor, String trackedSearchQuery, GitHubAPI gitHubAPIInst) {
        this.supervisorActor = supervisorActor;
        this.trackedSearchQuery = trackedSearchQuery;
        this.gitHubAPIInst = gitHubAPIInst;
    }

    /**
     * Creates an actor with properties specified using parameters
     * @param supervisorActor Actor reference for the supervisor actor
     * @param trackedSearchQuery Search query for which created actor will be responsible
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface, used to make external API calls to GitHub
     * @return A <code>Props</code> object holding actor configuration
     * @author Pradnya Kandarkar
     */
    public static Props props(ActorRef supervisorActor, String trackedSearchQuery, GitHubAPI gitHubAPIInst) {
        return Props.create(SearchActor.class, supervisorActor, trackedSearchQuery, gitHubAPIInst);
    }

    /**
     * Executes before any other action related to this actor. Initiates periodic tracking requests for this actor.
     * @author Pradnya Kandarkar
     */
    @Override
    public void preStart() {
        getTimers().startPeriodicTimer("RefreshSearch",
                new Messages.TrackSearch(trackedSearchQuery, "periodic"),
                Duration.create(10, TimeUnit.SECONDS));
        //TODO: Farheen, change this to 120 before submission
    }

    /**
     * Handles incoming messages for this actor - matches the class of an incoming message and takes appropriate action
     * @return <code>AbstractActor.Receive</code> defining the messages that can be processed by this actor and how they will be processed
     * @author Pradnya Kandarkar
     */
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Messages.TrackSearch.class, this::onTrackSearch)
                .matchAny(other -> log.error("Received unknown message type: " + other.getClass()))
                .build();
    }

    /**
     * Gets search result information for provided query and calls <code>processSearchResult</code> to process it
     * @param trackSearchRequest <code>TrackSearch</code> request to retrieve the information for
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    private void onTrackSearch(Messages.TrackSearch trackSearchRequest) throws Exception {
        gitHubAPIInst.getRepositoryFromSearchBar(trackSearchRequest.searchQuery).thenAcceptAsync(
                searchRepository -> processSearchResult(searchRepository, trackSearchRequest.requestType));
    }

    /**
     * Based on provides search response, creates and sends a JSON response
     * @param searchRepository Search result containing information about 10 repositories
     * @param requestType Indicates whether the request is a periodic search query sent by the search actor itself or a request sent from client side
     * @author Pradnya Kandarkar
     */
    private void processSearchResult(SearchRepository searchRepository, String requestType) {
        if(baseSearchResults == null) {
            /* If "baseSearchResults" has no repositories, it means this is the first search request. In this case, initiates
             * "baseSearchResults" with the repository list from provided searchRepository and creates a JSON response.
             * "responseType" set to "searchResult" indicates that this is a fresh request. */

            baseSearchResults = searchRepository.getRepositoryList();
            ObjectMapper mapper = new ObjectMapper();
            searchResultJson = mapper.valueToTree(searchRepository);
            searchResultJson.put("responseType", "searchResult");
        } else {
            /* If "baseSearchResults" has repository entries, checks if any repositories provided in "searchRepository"
            * are already present in "baseSearchResults" and constructs "searchResultUpdate" which contains only the
            * new repositories  */
            List<RepositoryModel> searchResultUpdate = new ArrayList<>();
            for(RepositoryModel repositoryModel: searchRepository.getRepositoryList()) {
                if(!baseSearchResults.contains(repositoryModel)){
                    searchResultUpdate.add(repositoryModel);
                }
            }

            /* This if block is added to help during the development process and should be removed before project
            * submission. */
            //TODO: Farheen: Remove before submission
            if(searchResultUpdate.isEmpty()) {
                RepositoryModel sampleRepositoryModel = new RepositoryModel("sampleOwnerName", "sampleRepositoryName", new ArrayList<String>());
                searchResultUpdate.add(sampleRepositoryModel);
            }

            /* Sets the list of repositories to be returned using "searchResultUpdate". Constructs the final JSON
            * response, indicating whether the response is for a fresh search request or for a periodic search result
            * update */
            searchRepository.setRepositoryList(searchResultUpdate);
            ObjectMapper mapper = new ObjectMapper();
            searchResultJson = mapper.valueToTree(searchRepository);
            if(requestType.equals("periodic")) {
                searchResultJson.put("responseType", "searchResultPeriodicUpdate");
            } else {
                searchResultJson.put("responseType", "searchResultUpdate");
            }

            /* Updates "baseSearchResults" with the new repositories as well */
            baseSearchResults.addAll(searchResultUpdate);
        }
        /* Sends the search result to be forwarded to the client */
        supervisorActor.tell(new Messages.SearchResult(searchResultJson), getSelf());
    }
}
