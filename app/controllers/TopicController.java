package controllers;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.GitHubAPIImpl;

/**
 * This controller contains an action to handle HTTP requests
 * to the topic list page.
 * @author Indraneel Rachakonda
 */
public class TopicController extends Controller {

	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;
	private GitHubAPIImpl gitHubAPI;

	@Inject
	public TopicController(HttpExecutionContext httpExecutionContext, AssetsFinder assetsFinder,
						   AsyncCacheApi cache, GitHubAPIImpl githubAPI) {
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.gitHubAPI = githubAPI;
	}

	/**
	 * An action that renders an HTML page with the top 10 repositories containing the topic provided by the user.
	 * The configuration in the <code>routes</code> file means that
	 * this method will be called when the application receives a
	 * <code>GET</code> request with a path of <code>/topics/:topic</code>.
	 * @param topic  Topic based on which the repositories will be retrieved
	 * @return Future CompletionStage Result
	 * @author Indraneel Rachakonda
	 */
	public CompletionStage<Result> getTopicRepository(String topic) {
		return asyncCacheApi.getOrElseUpdate(
				"topic_" + topic,
				() -> gitHubAPI.getTopicRepository(topic))
						 .thenApplyAsync((searchResult) -> {
								asyncCacheApi.set("topic_" + topic, searchResult,  60 * 15);
								return ok(views.html.topics.topics.render(searchResult, assetsFinder));
							}, httpExecutionContext.current()
				);
	}
}