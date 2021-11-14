package services;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.IssueModel;
import models.SearchRepository;
import play.libs.ws.*;
import services.github.GitHubAPI;

import com.typesafe.config.Config;

/**
 * This class handles all API of GitHub.
 * @author SmitPateliya, Pradnya Kandarkar, Farheen Jamadar
 *
 */

public class GitHubAPIImpl implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	private final WSClient client;
	private String baseURL;
	
	/**
	 *  
	 * @param client Constructor gets data from the Controller class.
	 */

	@Inject
	public GitHubAPIImpl(WSClient client, Config config) {
		this.client = client;
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

	public CompletionStage<JsonNode> getRepositoryProfile(String ownerName, String repositoryName) {
		String finalURL = this.baseURL + "/repos/" + ownerName + "/" + repositoryName;
		CompletionStage<JsonNode> result = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
				.get()
				.thenApplyAsync(repositoryProfileDetails -> repositoryProfileDetails.asJson());
		return result;
	}
}
