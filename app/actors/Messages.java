package actors;

import com.fasterxml.jackson.databind.JsonNode;

public class Messages {
    public static final class TrackSearch {
        public final String searchQuery;
        public final String requestType;

        public TrackSearch(String searchQuery, String requestType) {
            this.searchQuery = searchQuery;
            this.requestType = requestType;
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

    public static final class UserProfileInfo {
        public final JsonNode userProfileResult;

        public UserProfileInfo(JsonNode userProfileResult) {
            this.userProfileResult = userProfileResult;
        }
    }
}
