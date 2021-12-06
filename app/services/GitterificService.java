package services;

import com.google.inject.Inject;
import models.SearchRepository;
import play.cache.AsyncCacheApi;
import services.github.GitHubAPI;
import java.util.concurrent.CompletionStage;

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
}
