package services;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
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
 */

public class GitHubAPIImpl implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	private WSClient client;
	private String baseURL;

	/**
	 *
	 * @param client Constructor gets data from the Controller class.
	 * @param config Placeholder for external properties
	 */

	@Inject
	public GitHubAPIImpl(WSClient client, Config config) {
		this.client = client;
		this.baseURL = config.getString("git.baseUrl");
	}
	
	/**
	 * An action that fetches query from user and returns query information
	 *
	 * @param query Query string from User to search repositories.
	 * @return Returns SearchRepository Model containing repository information.
	 * @author SmitPateliya
	 */

	public CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) {
		System.out.println("Using the actual implementation for getRepositoryFromSearchBar.");
		String finalURL = this.baseURL + "/search/repositories";
		CompletionStage<SearchRepository> searchResult = client.url(finalURL).addQueryParameter("q", query)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new SearchRepository(result.asJson(), query));
		return searchResult;
	}
	
	/**
	 * An action that fetches repository's issues from API
	 * @author smitpateliya
	 * @param repoFullName repository's full name
	 * @return IssueModel's future instance
	 */

	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName) {
		System.out.println("Using the actual implementation for getRepositoryIssue.");
		String finalURL = this.baseURL + "/repos/" + repoFullName + "/issues";
		CompletionStage<IssueModel> searchResult = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new IssueModel(repoFullName, result.asJson()));
		System.out.println("Result issues: " + searchResult);
		return searchResult;
	}

	public CompletionStage<JsonNode> getUserProfileByUsername(String username) {
		System.out.println("Using the actual implementation for getUserProfileByUsername.");
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

	/**
	 * An action that fetches all the available details of the repository
	 * @param ownerName Owner of the repository
	 * @param repositoryName Repository Name
	 * @return Returns JsonNode containing Repository information
	 * @author Farheen Jamadar
	 */
	public CompletionStage<JsonNode> getRepositoryProfile(String ownerName, String repositoryName) {
		String finalURL = this.baseURL + "/repos/" + ownerName + "/" + repositoryName;
		CompletionStage<JsonNode> result = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS))
				.get()
				.thenApplyAsync(repositoryProfileDetails -> repositoryProfileDetails.asJson());
		return result;
	}

	public CompletionStage<SearchRepository> getTopicRepository(String topic) {
		String finalURL = this.baseURL + "/search/repositories?q=topic:" + topic + "&sort=created&order=desc";
		CompletionStage<SearchRepository> searchResult = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new SearchRepository(result.asJson(), topic));
		return searchResult;
	}


	public CompletionStage<List<String>> getRepositories() {
		return client.url(baseURL + "/repositories")
				.get()
				.thenApply(
						response ->
								response.asJson().findValues("full_name").stream()
										.map(JsonNode::asText)
										.collect(Collectors.toList()));
	}

	public void setClient(WSClient client) {
		this.client = client;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public WSClient getClient() {
		return client;
	}

	public String getBaseURL() {
		return baseURL;
	}
}
