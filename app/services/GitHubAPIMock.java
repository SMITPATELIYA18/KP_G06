package services;

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

	@Override
	public CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) {
		return null;
	}

	@Override
	public CompletionStage<JsonNode> getUserProfileByUsername(String username) {
		System.out.println("Using the mock implementation for getUserProfileByUsername");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleUserProfile = null;
		try {
			sampleUserProfile = mapper.readTree(TestResources.sampleUserProfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CompletableFuture<JsonNode> futureUserProfile = new CompletableFuture<JsonNode>();
		futureUserProfile.complete(sampleUserProfile);
		return futureUserProfile;
	}

	@Override
	public CompletionStage<JsonNode> getUserRepositories(String username) {
		System.out.println("Using the mock implementation for getUserRepositories");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleUserRepositories = null;
		try {
			sampleUserRepositories = mapper.readTree(TestResources.sampleUserRepositories);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		SearchRepository sampleTopicList = null;
		try {
			sampleTopicList = new SearchRepository(mapper.readTree(TestResources.sampleRepositoryProfile), topic);
		} catch (Exception e) {
			e.printStackTrace();
		}

		CompletableFuture<SearchRepository> futureTopicList = new CompletableFuture<>();
		futureTopicList.complete(sampleTopicList);
		return futureTopicList;
	}
}
