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

public class SearchActor extends AbstractActorWithTimers {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private String trackedSearchQuery;
    private GitHubAPI gitHubAPIInst;
    private List<RepositoryModel> baseSearchResults;
    private ObjectNode searchResultJson;
    private ActorRef sessionActor;

    public SearchActor(ActorRef sessionActor, String trackedSearchQuery, GitHubAPI gitHubAPIInst) {
        this.sessionActor = sessionActor;
        this.trackedSearchQuery = trackedSearchQuery;
        this.gitHubAPIInst = gitHubAPIInst;
    }

    public static Props props(ActorRef sessionActor, String trackedSearchQuery, GitHubAPI gitHubAPIInst) {
        return Props.create(SearchActor.class, sessionActor, trackedSearchQuery, gitHubAPIInst);
    }

    @Override
    public void preStart() {
        System.out.println("Created a search actor for " + trackedSearchQuery);
        getTimers().startPeriodicTimer("RefreshSearch", new Messages.TrackSearch(trackedSearchQuery, "periodic"), Duration.create(15, TimeUnit.SECONDS));
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Messages.TrackSearch.class, this::onTrackSearch)
                .build();
    }

    private void onTrackSearch(Messages.TrackSearch trackSearchRequest) throws Exception {
        System.out.println("Search actor is tracking this query: " + trackSearchRequest.searchQuery);
        System.out.println("Received request from : " + trackSearchRequest.requestType);
        gitHubAPIInst.getRepositoryFromSearchBar(trackSearchRequest.searchQuery).thenAcceptAsync(searchRepository -> {
            processSearchResult(searchRepository, trackSearchRequest.requestType);
        });
    }

    private void processSearchResult(SearchRepository searchRepository, String requestType) {
        if(baseSearchResults == null) {
            baseSearchResults = searchRepository.getRepositoryList();
            ObjectMapper mapper = new ObjectMapper();
            searchResultJson = mapper.valueToTree(searchRepository);
            searchResultJson.put("responseType", "searchResult");
        } else {
            List<RepositoryModel> searchResultUpdate = new ArrayList<>();
            for(RepositoryModel repositoryModel: searchRepository.getRepositoryList()) {
                if(!baseSearchResults.contains(repositoryModel)){
                    searchResultUpdate.add(repositoryModel);
                }
            }
            if(searchResultUpdate.isEmpty()) {
                RepositoryModel sampleRepositoryModel = new RepositoryModel("sampleOwnerName", "sampleRepositoryName", new ArrayList<String>());
                searchResultUpdate.add(sampleRepositoryModel);
            }
            searchRepository.setRepositoryList(searchResultUpdate);
            ObjectMapper mapper = new ObjectMapper();
            searchResultJson = mapper.valueToTree(searchRepository);
            if(requestType.equals("periodic")) {
                searchResultJson.put("responseType", "searchResultPeriodicUpdate");
            } else {
                searchResultJson.put("responseType", "searchResultUpdate");
            }
            System.out.println("Update for search " + trackedSearchQuery + ":\n " + searchResultJson);
            baseSearchResults.addAll(searchResultUpdate);
        }
        sessionActor.tell(new Messages.SearchResult(searchResultJson), getSelf());
    }
}
