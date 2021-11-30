package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.SearchCacheStore;
import models.SearchRepository;
import play.cache.AsyncCacheApi;
import services.github.GitHubAPI;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds all services utilized by <code>GitterificController</code>
 * @author Pradnya Kandarkar
 */
public class GitterificService {

    private AsyncCacheApi asyncCacheApi;
    private GitHubAPI gitHubAPIInst;

    @Inject
    public GitterificService(AsyncCacheApi asyncCacheApi, GitHubAPI gitHubAPIInst) {
        this.asyncCacheApi = asyncCacheApi;
        this.gitHubAPIInst = gitHubAPIInst;
    }

    /**
     * Retrieves repository list based on the search query provided by the user
     * @param username Owner of the repository
     * @return Future CompletionStage SearchCacheStore
     * @author Farheen Jamadar, SmitPateliya
     */
   /* public CompletionStage<SearchCacheStore> getRepositoryFromSearch(String username) {
        CompletionStage<SearchRepository> newSearchData = asyncCacheApi.getOrElseUpdate("search_" + username, () -> {
            CompletionStage<SearchRepository> searchRepository = gitHubAPIInst.getRepositoryFromSearchBar(username);
            asyncCacheApi.set("search_" + username, searchRepository, 60 * 15);
            return searchRepository;
        });

        return newSearchData.thenCombineAsync(
                asyncCacheApi.get("search"),
                (newData, cacheData) -> {
                    SearchCacheStore store = new SearchCacheStore();
                    if (cacheData.isPresent()) {
                        store = (SearchCacheStore) cacheData.get();
                    }
                    store.addNewSearch(newData);
                    asyncCacheApi.set("search", store, 60 * 15);
                    return store;
                });
    }*/

    /**
     * Retrieves repository profile details with top 20 issues
     * @param username Owner of the repository
     * @param repositoryName  Repository Name
     * @return Future CompletionStage JsonNode
     * @author Farheen Jamadar
     */
    /*public CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) {
        return asyncCacheApi.getOrElseUpdate(username + "/" + repositoryName,
                        () -> gitHubAPIInst.getRepositoryProfile(username, repositoryName))
                .thenCombineAsync(
                        asyncCacheApi.getOrElseUpdate(username + repositoryName + "/20issues",
                                () -> gitHubAPIInst.getRepositoryIssue(username + "/" + repositoryName)),
                        (repositoryProfileDetail, issueList) -> {
                            asyncCacheApi.set(username + repositoryName + "/20issues", issueList,  60 * 15);
                            asyncCacheApi.set(username + "/" + repositoryName, repositoryProfileDetail,  60 * 15);

                            List<String> list = new ArrayList<>();//issueList.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode repositoryData = mapper.createObjectNode();
                            ArrayNode arrayNode = mapper.createArrayNode();
                            list.forEach(arrayNode::add);

                            repositoryData.set("repositoryProfile", repositoryProfileDetail);
                            repositoryData.set("issueList", arrayNode);

                            return repositoryData;
                        }
                );
    }
*/
    /**
     * Retrieves top 10 repositories containing the topic provided by the user.
     * @param topic Topic based on which the repositories will be retrieved
     * @return Future CompletionStage SearchRepository
     * @author Indraneel Rachakonda
     */
    public CompletionStage<SearchRepository> getTopicRepository(String topic) {
        return asyncCacheApi.getOrElseUpdate(
                        "topic_" + topic,
                        () -> gitHubAPIInst.getTopicRepository(topic))
                .thenApplyAsync((searchResult) -> {
                            asyncCacheApi.set("topic_" + topic, searchResult,  60 * 15);
                            return searchResult;
                        });
    }
    
    public CompletionStage<JsonNode> getRepositoryIssueStat(String repoFullName) {
    	return gitHubAPIInst.getRepositoryIssue(repoFullName).thenApplyAsync(data -> {
					ObjectMapper mapper = new ObjectMapper();
					ObjectNode finalResult = mapper.createObjectNode();
					finalResult.put("repoFullName", repoFullName);
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
    }
    
    /**
	 * This functions return String stream of title array.
	 * @param title Receives title for splitting into individual words
	 * @return Stream of title's word 
	 * 
	 */
	
	private Stream<String> getIndividualWord(String title) {
		return Arrays.asList(title.split(" ")).stream();
	}
}
