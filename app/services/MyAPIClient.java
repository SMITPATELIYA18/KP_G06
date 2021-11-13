package services;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.IssueModel;
import models.RepositoryModel;
import models.RepositoryProfileModel;
import models.SearchRepository;
import org.checkerframework.checker.units.qual.A;
import play.libs.ws.*;
import services.github.GitHubAPI;

import com.typesafe.config.Config;

/**
 * This class handles all API of GitHub.
 * @author SmitPateliya
 *
 */

public class MyAPIClient implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	private final WSClient client;
	private final Config config;
	private String baseURL;
	
	/**
	 *  
	 * @param client Constructor gets data from the Controller class.
	 */

	@Inject
	public MyAPIClient(WSClient client, Config config) {
		this.client = client;
		this.config = config;
		this.baseURL = config.getString("git.baseUrl");
	}
	
	/**
	 * This method is getting information from user and returns
	 * all the information regarding the query.
	 * 
	 * @param query Gets the query from User to search repositories.
	 * @return Returns SearchRepository Model containing all Repository information.
	 */

	public CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) {
		String finalURL = this.baseURL + "/search/repositories";
		CompletionStage<SearchRepository> searchResult = client.url(finalURL).addQueryParameter("q", query)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new SearchRepository(result.asJson(), query));
		return searchResult;
	}
	
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName) {
		String finalURL = this.baseURL + "/repos/" + repoFullName + "/issues";
		CompletionStage<IssueModel> searchResult = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new IssueModel(repoFullName, result.asJson()));
		System.out.println("Result issues: " + searchResult);
		return searchResult;
	}

	public CompletionStage<JsonNode> getUserProfileByUsername(String username) {
		String requestURL = this.baseURL + "/users/" + username;
		return client.url(requestURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS)).get()
				.thenApplyAsync(r -> r.getBody(json()));
	}

	public CompletionStage<JsonNode> getUserRepositories(String username) {
		String requestURL = this.baseURL + "/users/" + username + "/repos";
		return client.url(requestURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS)).get()
				.thenApplyAsync(r -> {
					JsonNode userRepos = r.getBody(json());
					ObjectMapper mapper = new ObjectMapper();
					ArrayNode userRepoList = mapper.createArrayNode();

					if(userRepos != null){
						if(userRepos instanceof ArrayNode){
							ArrayNode arrayNode = (ArrayNode) userRepos;
							Iterator<JsonNode> nodeIterator = arrayNode.iterator();
							while (nodeIterator.hasNext()) {
								JsonNode elementNode = nodeIterator.next();
								userRepoList.add(elementNode.findValue("name"));
							}
						}
					}
					return userRepoList;
				});
	}

	/*public CompletableFuture<RepositoryProfileModel> getRepositoryProfile(String ownerName, String repositoryName){
		String finalURL = this.config.getString("git.baseUrl") + "/repos/" + ownerName + "/" + repositoryName;
		CompletableFuture<RepositoryProfileModel> result = client.url(finalURL).get()
										.toCompletableFuture().thenApplyAsync(output -> RepositoryProfileModel.initialize(output.asJson()));

		System.out.println("Result: " + result);
		return  result;
	}*/

	//TODO: Optimize
	public CompletionStage<WSResponse> getRepositoryProfile(String ownerName, String repositoryName){
		String finalURL = this.config.getString("git.baseUrl") + "/repos/" + ownerName + "/" + repositoryName;
		CompletionStage<WSResponse> result = client.url(finalURL).get();
		//.thenApplyAsync(output -> new RepositoryProfileModel(output.asJson()));
		//System.out.println("Result: " + result);
		return  result;
	}

	public void setBaseURL(String URL) {
		this.baseURL = URL;
	}

	public String getBaseURL() {
		return this.baseURL;
	}
}
