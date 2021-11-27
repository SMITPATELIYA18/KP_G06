package actors;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import services.github.GitHubAPI;

import static java.util.Objects.requireNonNull;

public class Messages {

    public static final class StartSession {
        public final ActorRef wsOut;
        public final GitHubAPI gitHubAPIInst;

        public StartSession(ActorRef wsOut, GitHubAPI gitHubAPIInst) {
            this.wsOut = wsOut;
            this.gitHubAPIInst = gitHubAPIInst;
        }
    }

    public static final class SessionStarted {
        public final ActorRef sessionActor;

        public SessionStarted(ActorRef sessionActor) {
            this.sessionActor = sessionActor;
        }
    }

    public static final class TrackSearch {
        public final String searchQuery;

        public TrackSearch(String searchQuery) {
            this.searchQuery = searchQuery;
        }
    }

    public static final class SearchResult {
        public final JsonNode searchResult;

        public SearchResult(JsonNode searchResult) {
            this.searchResult = searchResult;
        }
    }

    public static final class GetUserProfile {
        public final String username;

        public GetUserProfile(String username) {
            this.username = username;
        }
    }
}
