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

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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

    public CompletionStage<SearchCacheStore> getRepositoryFromSearch(String username) {
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
    }

    /**
     * Retrieves all available public profile information about a user, as well as all the repositories of that user
     * @param username Username to fetch the details for
     * @return CompletionStage&lt;JsonNode&gt; which contains available public profile information and repositories for a user
     * @author Pradnya Kandarkar
     */
    public CompletionStage<JsonNode> getUserProfile(String username) {

        return asyncCacheApi.getOrElseUpdate(username + "_profile",
                        () -> gitHubAPIInst.getUserProfileByUsername(username))
                .thenCombineAsync(asyncCacheApi.getOrElseUpdate(username + "_repositories",
                                () -> gitHubAPIInst.getUserRepositories(username)),
                        (userProfile, userRepositories) -> {
                            asyncCacheApi.set(username + "_profile", userProfile);
                            asyncCacheApi.set(username + "_repositories", userRepositories);
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode userInfo = mapper.createObjectNode();
                            userInfo.set("profile", userProfile);
                            userInfo.set("repositories", userRepositories);
                            return userInfo;
                        }
                );
    }

    /**
     * Retrieves repository profile details with corresponding top 20 issues
     * @param username Owner of the repository
     * @param repositoryName  Repository Name
     * @return Future CompletionStage JsonNode
     * @author Farheen Jamadar
     */
    public CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) {
        return asyncCacheApi.getOrElseUpdate(username + "/" + repositoryName,
                        () -> gitHubAPIInst.getRepositoryProfile(username, repositoryName))
                .thenCombineAsync(
                        asyncCacheApi.getOrElseUpdate(username + repositoryName + "/20issues",
                                () -> gitHubAPIInst.getRepositoryIssue(username + "/" + repositoryName)),
                        (repositoryProfileDetail, issueList) -> {
                            asyncCacheApi.set(username + repositoryName + "/20issues", issueList,  60 * 15);
                            asyncCacheApi.set(username + "/" + repositoryName, repositoryProfileDetail,  60 * 15);

                            List<String> list = issueList.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());

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
}
