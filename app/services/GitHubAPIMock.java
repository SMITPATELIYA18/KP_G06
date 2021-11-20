package services;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.IssueModel;
import models.SearchRepository;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import resources.TestResources;
import services.github.GitHubAPI;

/**
 * Mocks API calls and return fake responses
 * @author Smit Pateliya, Pradnya Kandarkar
 *
 */

public class GitHubAPIMock implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	
	/**
	 * This function returns IssueModel object when API  will call.
	 * @author smitpateliya
	 * @param repoFullName The name of repository name
	 * @return returns completion stage issue model object 
	 */
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName){
		if(repoFullName.equals("sadasd/sadsad")) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode data = null;
			try {
				data = mapper.readTree(TestResources.nullIssueData);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CompletableFuture<IssueModel> futureModel = new CompletableFuture<>();
			IssueModel modelData = new IssueModel(repoFullName, data);
			futureModel.complete(modelData);
//			System.out.println(modelData.getWordLevelData());
			return futureModel;
		}
		System.out.println("Using the mock implementation for getRepositoryIssue.");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = null;
		try {
			data = mapper.readTree(TestResources.issueData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompletableFuture<IssueModel> futureModel = new CompletableFuture<>();
		IssueModel modelData = new IssueModel(repoFullName, data);
		futureModel.complete(modelData);
//		System.out.println(modelData.getWordLevelData());
		return futureModel;
	}

	/**
	 * Mock method for an action that fetches query from user and returns query information
	 * @param query Query string from User to search repositories
	 * @return Returns SearchRepository Model containing repository information
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	@Override
	public CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) throws Exception {
		System.out.println("Using the mock implementation for getRepositoryFromSearchBar");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleSearchResult = mapper.readTree(new File("test/resources/searchreposfeature/sampleSearchResult.json"));
		CompletableFuture<SearchRepository> futureModel = new CompletableFuture<>();
		SearchRepository modelData = new SearchRepository(sampleSearchResult, query);
		futureModel.complete(modelData);
		return futureModel;
	}

	/**
	 * Mock method for fetching all available public profile information about a user
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;JsonNode&gt; which contains sample data for available public profile information of a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	@Override
	public CompletionStage<JsonNode> getUserProfileByUsername(String username) throws Exception {
		System.out.println("Using the mock implementation for getUserProfileByUsername");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleUserProfile = mapper.readTree(new File("test/resources/userprofile/validGitHubUserProfile.json"));
		CompletableFuture<JsonNode> futureUserProfile = new CompletableFuture<JsonNode>();
		futureUserProfile.complete(sampleUserProfile);
		return futureUserProfile;
	}

	/**
	 * Mock method for fetching all available public repositories of a user
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;JsonNode&gt; which contains sample data for available public repositories of a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	@Override
	public CompletionStage<JsonNode> getUserRepositories(String username) throws Exception {
		System.out.println("Using the mock implementation for getUserRepositories");
		String sampleUserRepositoriesData = "[\"testRepoForPlayProject\",\"testRepositoryForPlayProject2\"]";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleUserRepositories = mapper.readTree(sampleUserRepositoriesData);
		CompletableFuture<JsonNode> futureUserRepositories = new CompletableFuture<JsonNode>();
		futureUserRepositories.complete(sampleUserRepositories);
		return futureUserRepositories;
	}

	@Override
	public CompletionStage<JsonNode> getRepositoryProfile(String ownerName, String repositoryName) {
		System.out.println("Using the mock implementation for getRepositoryProfile");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleRepositoryProfile = null;
		try {
			sampleRepositoryProfile = mapper.readTree(TestResources.sampleRepositoryProfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//TODO: Farheen
		CompletableFuture<JsonNode> futureUserRepositories = new CompletableFuture<>();
		futureUserRepositories.complete(sampleRepositoryProfile);
		return futureUserRepositories;
	}

	// ToDo: Indraneel
	@Override
	public CompletionStage<SearchRepository> getTopicRepository(String topic) {
		System.out.println("Using the mock implementation for getTopicRepository");
		ObjectMapper mapper = new ObjectMapper();
		SearchRepository sampleTopicRepositoryListURL = null;
		try {
			sampleTopicRepositoryListURL = new SearchRepository(mapper.readTree(TestResources.sampleRepositoryProfile), topic);
		} catch (Exception e) {
			e.printStackTrace();
		}

		CompletableFuture<SearchRepository> futureTopicRepositoryList = new CompletableFuture<>();
		futureTopicRepositoryList.complete(sampleTopicRepositoryListURL);
		return futureTopicRepositoryList;
	}
}
