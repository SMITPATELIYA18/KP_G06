package services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.IssueModel;
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

	// ToDo: Implement
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

	// ToDo: Implement
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
}
