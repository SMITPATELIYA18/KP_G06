package services.github;

import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;

import models.IssueModel;
import models.SearchRepository;

/**
 * Declares methods used for interaction with GitHub REST API
 * @author Smit Pateliya, Pradnya Kandarkar
 */
public interface GitHubAPI {

	/**
	 * Declaration of the function that fetches query from user and returns query information
	 * @param query Query string from User to search repositories.
	 * @return Returns SearchRepository Model containing repository information.
	 * @throws Exception If the call cannot be completed due to an error
	 * @author SmitPateliya
	 */
	CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) throws Exception;

	/**
	 * Declaration of the function that retrieves all available public profile information about a user
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;JsonNode&gt; which contains available public profile information for a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	CompletionStage<JsonNode> getUserProfileByUsername(String username) throws Exception;

	/**
	 * Declaration of the function that retrieves all available public repositories of a user
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;JsonNode&gt; which contains available public repositories for a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	CompletionStage<JsonNode> getUserRepositories(String username) throws Exception;

	/**
	 * Declaration of the function that fetches all the available details of the repository
	 * @param username Owner of the repository
	 * @param repositoryName Repository Name
	 * @return Returns JsonNode containing Repository information
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Farheen Jamadar
	 */
	CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) throws Exception;

	/**
	 * Declaration of the function that fetches repository's issues from API
	 * @author smitpateliya
	 * @param repoFullName repository's full name
	 * @throws Exception If the call cannot be completed due to an error
	 * @return IssueModel's future instance
	 */
	CompletionStage<IssueModel> getRepositoryIssue(String repoFullName) throws Exception;

	/**
	 * Declaration of the function that retrieves top 10 repositories containing the topic provided by the user.
	 * @param topic Topic based on which the repositories will be retrieved
	 * @return Future CompletionStage SearchRepository
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Indraneel Rachakonda
	 */
	CompletionStage<SearchRepository> getTopicRepository(String topic) throws Exception;
}
