package actors;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Holds messages used by the application actors
 * @author Pradnya Kandarkar
 */
public class Messages {
    /**
     * Used to ask <code>SearchActor</code> to keep track of and update the result of provided search query
     * @author Pradnya Kandarkar
     */
    public static final class TrackSearch {
        public final String searchQuery;
        public final String requestType;

        /**
         * @param searchQuery Search query to be tracked by <code>SearchActor</code>
         * @param requestType Indicates whether the request is a periodic search query sent by the search actor itself or a request sent from client side
         */
        public TrackSearch(String searchQuery, String requestType) {
            this.searchQuery = searchQuery;
            this.requestType = requestType;
        }
    }

    /**
     * Used to send the search result from <code>SearchActor</code>
     * @author Pradnya Kandarkar
     */
    public static final class SearchResult {
        public final JsonNode searchResult;

        /**
         * @param searchResult Holds result of a search request
         */
        public SearchResult(JsonNode searchResult) {
            this.searchResult = searchResult;
        }
    }

    /**
     * Used to ask <code>UserProfileActor</code> to retrieve profile and repository information for a user
     * @author Pradnya Kandarkar
     */
    public static final class GetUserProfile {
        public final String username;

        /**
         * @param username Username to fetch the details for
         */
        public GetUserProfile(String username) {
            this.username = username;
        }
    }

    /**
     * Used to send the user profile and repositories information from <code>UserProfileActor</code>
     * @author Pradnya Kandarkar
     */
    public static final class UserProfileInfo {
        public final JsonNode userProfileResult;

        /**
         * @param userProfileResult Holds user profile and repositories information
         */
        public UserProfileInfo(JsonNode userProfileResult) {
            this.userProfileResult = userProfileResult;
        }
    }

    public static final class UnknownMessageReceived {
    }


    public static final class GetRepositoryProfileActor {
        public final String repositoryName;
        public final String username;

        public GetRepositoryProfileActor(String username, String repositoryName) {
            this.username = username;
            this.repositoryName = repositoryName;
        }
    }

    public static final class RepositoryProfileInfo {
        public final JsonNode repositoryProfileResult;

        public RepositoryProfileInfo(JsonNode repositoryProfileResult) {
            this.repositoryProfileResult = repositoryProfileResult;
        }
    }
    
    /**
     * This message class uses for receiving data from supervisor actor
     * @author Smit Pateliya
     *
     */
    
    public static final class GetRepositoryIssueActor{
    	public final String repoFullName;
    	
    	/**
    	 * This methods initialize repoFullNme variable
    	 * @param repoFullName
    	 * @author Smit Pateliya
    	 */
    	
    	public GetRepositoryIssueActor(String repoFullName) {
    		this.repoFullName = repoFullName;
    	}
    }
    
    /**
     * This message class uses for sending data to user
     * @author Smit Pateliya
     *
     */
    
    public static final class IssueStatInfo {
    	public final JsonNode issueModel;
    	
    	/**
    	 * Initializes issueModel variable
    	 * @param issueModel
    	 * @author Smit Pateliya
    	 */
    	
    	public IssueStatInfo(JsonNode issueModel) {
    		this.issueModel = issueModel;
    	}
    }
    
    /**
     * Used to ask <code>TopicActor</code> to retrieve topic information
     * @author Indraneel Rachakonda
     */
    public static final class GetTopic {
        public final String topic;

        /**
         * @param topic Topic to fetch the details for
         */
        public GetTopic(String topic) {
            this.topic = topic;
        }
    }
    
    /**
     * Used to send the topic information from <code>TopicActor</code>
     * @author Indraneel Rachakonda
     */
    public static final class TopicInfo {
        public final JsonNode topicResult;

        /**
         * @param topicResult Holds information based on topic
         */
        public TopicInfo(JsonNode topicResult) {
            this.topicResult = topicResult;
        }
    }

}
