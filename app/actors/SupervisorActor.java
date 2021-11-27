package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import services.github.GitHubAPI;

import java.util.HashMap;
import java.util.Map;

public class SupervisorActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef ws;
    private GitHubAPI gitHubAPIInst;
    final Map<String, ActorRef> queryToSearchActor = new HashMap<String, ActorRef>();

    public SupervisorActor(final ActorRef wsOut, GitHubAPI gitHubAPIInst) {
        ws =  wsOut;
        this.gitHubAPIInst = gitHubAPIInst;
    }

    public static Props props(final ActorRef wsout, GitHubAPI gitHubAPIInst) {
        return Props.create(SupervisorActor.class, wsout, gitHubAPIInst);
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
                .match(Messages.SearchResult.class, searchResult -> {
                    ws.tell(searchResult.searchResult, self());
                })
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
            searchActor.tell(new Messages.TrackSearch(searchQuery), getSelf());
        } else if(receivedJson.has("user_profile")) {
            System.out.println("It works. Received JSON: " + receivedJson);
        }
    }
}
