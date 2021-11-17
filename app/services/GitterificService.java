package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
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

    public CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) {
        return asyncCacheApi.getOrElseUpdate(username + "/" + repositoryName,
                        () ->  gitHubAPIInst.getRepositoryProfile(username, repositoryName))
                .thenCombineAsync(
                        asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
                                () -> gitHubAPIInst.getRepositoryIssue(username + "/" + repositoryName)),
                        (repositoryProfileDetail, issueList) -> {
                            asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
                            asyncCacheApi.set(username + "/" + repositoryName, repositoryProfileDetail,  60 * 15);

                            //TODO: Optimize
                            List<String> list = issueList.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());

                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode repositoryData = mapper.createObjectNode();

                            ArrayNode arrayNode = mapper.createArrayNode();

                                list.forEach(element -> {

                                    if(element == "Issue does not Present!"|| element == "Error! Repository does not present!"){
                                        arrayNode.add("No Issue Reported.");
                                    }
                                    else{
                                        arrayNode.add(element);
                                    }
                                });

                            System.out.println("List: " + list);

                            System.out.println("ArrayNode: " + arrayNode);
                            repositoryData.set("repositoryProfile", repositoryProfileDetail);
                            repositoryData.set("issueList", arrayNode);

                            return repositoryData;
                        }
                );
    }
}