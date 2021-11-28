package services;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.IssueModel;
import models.SearchRepository;
import play.libs.ws.*;
import services.github.GitHubAPI;

import com.typesafe.config.Config;

/**
 * This class handles all API of GitHub.
 * @author SmitPateliya
 * @author Pradnya Kandarkar
 * @author Farheen Jamadar
 * @author Indraneel Rachakonda
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

	public CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) throws Exception {
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

	public CompletionStage<JsonNode> getRepositoryIssue(String repoFullName) {
		String finalURL = this.baseURL + "/repos/" + repoFullName + "/issues";
		CompletionStage<JsonNode> searchResult = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> {
					ObjectMapper mapper = new ObjectMapper();
					ObjectNode finalResult = mapper.createObjectNode();
					finalResult.put("repoFullName", repoFullName);
					JsonNode data = result.asJson();
					List<String> issueTitles = new ArrayList<>();
//					LinkedHashMap<String, Long> worldLevelData = new LinkedHashMap<>();
					ObjectNode worldLevelData = mapper.createObjectNode();
					if(!data.has("message")) {
						java.util.Iterator<JsonNode> iteratorItems = data.elements() != null ? data.elements()
								: Collections.emptyIterator();
						iteratorItems.forEachRemaining(issue -> issueTitles.add(issue.get("title").asText()));
						Map<String, Long> unsortedData = issueTitles.stream().flatMap(title -> getIndividualWord(title))
								.collect(groupingBy(Function.identity(), counting()));
						unsortedData.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
								.forEachOrdered(result1 -> worldLevelData.put(result1.getKey(), result1.getValue()));
						finalResult.set("wordLevelData", worldLevelData);
						finalResult.put("error",false);
					}
					else {
						finalResult.put("error", true);
						finalResult.put("errorMessage", "Error! This Repository does not have Issues");
					}
                    return finalResult;
				});
		return searchResult;
	}
	
	private Stream<String> getIndividualWord(String title) {
		return Arrays.asList(title.split(" ")).stream();
	}

	/**
	 * Retrieves all available public profile information about a user
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;JsonNode&gt; which contains available public profile information for a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	public CompletionStage<JsonNode> getUserProfileByUsername(String username) throws Exception {
		String requestURL = this.baseURL + "/users/" + username;
		return client.url(requestURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS)).get()
				.thenApplyAsync(r -> r.getBody(json()));
	}

	/**
	 * Retrieves all available public repositories of a user
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;JsonNode&gt; which contains available public repositories for a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Pradnya Kandarkar
	 */
	public CompletionStage<JsonNode> getUserRepositories(String username) throws Exception {
		String requestURL = this.baseURL + "/users/" + username + "/repos";
		return client.url(requestURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS)).get()
				.thenApplyAsync(r -> {
					JsonNode userRepos = r.getBody(json());
					ObjectMapper mapper = new ObjectMapper();
					ArrayNode userRepoList = mapper.createArrayNode();

					if(userRepos instanceof ArrayNode){
						ArrayNode arrayNode = (ArrayNode) userRepos;
						Iterator<JsonNode> nodeIterator = arrayNode.iterator();
						while (nodeIterator.hasNext()) {
							JsonNode elementNode = nodeIterator.next();
							userRepoList.add(elementNode.findValue("name"));
						}
						return userRepoList;
					} else {
						return userRepos;
					}
				});
	}

	/**
	 * Fetches all the available details of the repository
	 * @param username Owner of the repository
	 * @param repositoryName Repository Name
	 * @return Returns JsonNode containing Repository information
	 * @author Farheen Jamadar
	 */
	public CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) throws Exception{
		String finalURL = this.baseURL + "/repos/" + username + "/" + repositoryName;
		CompletionStage<JsonNode> result = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(7000, ChronoUnit.MILLIS))
				.get()
				.thenApplyAsync(repositoryProfileDetails -> repositoryProfileDetails.getBody(json()));
		return result;
	}

	/**
	 * Retrieves top 10 repositories containing the topic provided by the user.
	 * @param topic Topic based on which the repositories will be retrieved
	 * @return Future CompletionStage SearchRepository
	 * @author Indraneel Rachakonda
	 */
	public CompletionStage<SearchRepository> getTopicRepository(String topic) throws Exception {
		String finalURL = this.baseURL + "/search/repositories";
		CompletionStage<SearchRepository> searchResult = client.url(finalURL)
				.addQueryParameter("q", topic)
				.addQueryParameter("sort", "created")
				.addQueryParameter("order", "desc")
				.addHeader("accept", "application/vnd.github.v3+json")
				.setRequestTimeout(Duration.of(5000, ChronoUnit.MILLIS)).get()
				.thenApplyAsync(result -> new SearchRepository(result.asJson(), topic));
		return searchResult;
	}

	/**
	 * Sets the <code>WSClient</code> value
	 * @param client <code>WSClient</code> value to be set
	 */
	public void setClient(WSClient client) {
		this.client = client;
	}

	/**
	 * Sets the base URL for the application
	 * @param baseURL <code>String</code> value to be set for the base URL
	 */
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
}
