package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.IssueModel;
import models.SearchRepository;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import resources.TestResources;
import services.github.GitHubAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Mocks API calls and return fake responses
 * @author Smit Pateliya
 * @author Pradnya Kandarkar
 * @author Farheen Jamadar
 * @author Indraneel Rachakonda
 */

public class GitHubAPIMock implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	private List<String> list;

	public GitHubAPIMock(){
		this.list = new ArrayList<>();
		this.list.add("test/resources/searchreposfeature/sampleSearchResult.json");
		this.list.add("test/resources/searchreposfeature/sampleSearchResult2.json");
		this.list.add("test/resources/searchreposfeature/sampleSearchResult3.json");
	}
	/**
	 * This function returns IssueModel object when API  will call.
	 * @author smitpateliya
	 * @param repoFullName The name of repository name
	 * @return returns completion stage issue model object 
	 */
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName) throws Exception{
		if(repoFullName.equals("sadasd/sadsad")) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode data = null;
			data = mapper.readTree(TestResources.nullIssueData);
			CompletableFuture<IssueModel> futureModel = new CompletableFuture<>();
			IssueModel modelData = new IssueModel(repoFullName, data);
			futureModel.complete(modelData);
			System.out.println(modelData.getWordLevelData());
			futureModel.complete(modelData);
			return futureModel;
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = null;
		data = mapper.readTree(TestResources.issueData);
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
		//System.out.println("Mock implementation for getRepositoryFromSearchBar");
		String filePath = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (this.list.size() > 1) {
				filePath = this.list.get(this.list.size() - 1);
				this.list.remove(this.list.size() - 1);
			} else {
				filePath = this.list.get(0);
			}
		}catch (IndexOutOfBoundsException e){
			System.out.println("Exception occured!");
			this.list.add("test/resources/searchreposfeature/sampleSearchResult.json");
			filePath = "test/resources/searchreposfeature/sampleSearchResult.json";
		}

	/*	if(query.equals("samplequery")){
			filePath = "test/resources/searchreposfeature/sampleSearchResult.json";
		}
		else if(query.equals("samplequery2")){
			filePath = "test/resources/searchreposfeature/sampleSearchResult3.json";
		}
		else{
			query = "samplequery2";
			filePath = "test/resources/searchreposfeature/sampleSearchResult2.json";
		}
*/
		System.out.println("Path: " + filePath + " query: " + query);
		JsonNode sampleSearchResult = mapper.readTree(new File(filePath));
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
		ObjectMapper mapper = new ObjectMapper();
		if(username.equals("empty_user_profile_response")) {
			System.out.println("Received the expected request.");
			JsonNode sampleUserProfile = mapper.readTree(new File("test/resources/userprofile/emptyUserProfileInformation.json"));
			CompletableFuture<JsonNode> futureUserProfile = new CompletableFuture<JsonNode>();
			futureUserProfile.complete(sampleUserProfile);
			return futureUserProfile;
		}
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
		String sampleUserRepositoriesData = "[\"testRepoForPlayProject\",\"testRepositoryForPlayProject2\"]";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleUserRepositories = mapper.readTree(sampleUserRepositoriesData);
		CompletableFuture<JsonNode> futureUserRepositories = new CompletableFuture<JsonNode>();
		futureUserRepositories.complete(sampleUserRepositories);
		return futureUserRepositories;
	}

	/**
	 * Mock method to fetch all the available details of the repository
	 * @param username Owner of the repository
	 * @param repositoryName Repository Name
	 * @return Returns JsonNode containing Repository information
	 * @author Farheen Jamadar
	 */
	@Override
	public CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) throws Exception{
		//System.out.println("Mock implementation for getRepositoryProfile");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode sampleRepositoryProfile = mapper.readTree(new File("test/resources/repositoryprofile/validRepositoryProfileDetails.json"));
		CompletableFuture<JsonNode> futureUserRepositories = new CompletableFuture<>();
		futureUserRepositories.complete(sampleRepositoryProfile);
		return futureUserRepositories;
	}

	/**
	 * Mock Implementation to retrieve top 10 repositories containing the topic provided by the user.
	 * @param topic Topic based on which the repositories will be retrieved
	 * @return Future CompletionStage SearchRepository
	 * @author Indraneel Rachakonda
	 */
	@Override
	public CompletionStage<SearchRepository> getTopicRepository(String topic) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		SearchRepository sampleTopicRepositoryListURL = new SearchRepository(mapper.readTree(TestResources.sampleTopicRepositories), topic);
		CompletableFuture<SearchRepository> futureTopicRepositoryList = new CompletableFuture<>();
		futureTopicRepositoryList.complete(sampleTopicRepositoryListURL);
		return futureTopicRepositoryList;
	}
}
