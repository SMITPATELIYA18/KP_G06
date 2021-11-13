package controllers;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContextExecutor;
import services.MyAPIClient;

public class TopicController extends Controller {

	private final Config config;
	private final ActorSystem actorSystem;
//	private final ExecutionContextExecutor executor;
	private final AssetsFinder assetsFinder;
	private final WSClient client;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	/**
	 * 
	 * @param actorSystem To run code after delay
	 * @param executor    To apply the result of the Completable Future.
	 */

	@Inject
	public TopicController(HttpExecutionContext httpExecutionContext, WSClient client, ActorSystem actorSystem,
			ExecutionContextExecutor executor, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config) {
		this.actorSystem = actorSystem;
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.config = config;
	}
	
	public CompletionStage<Result> getTopicRepository(String topic) {
		MyAPIClient apiClient = new MyAPIClient(client, config);
		return apiClient.getTopicRepository(topic).thenApplyAsync(
				searchResult -> {return ok(views.html.topics.topics.render(searchResult, assetsFinder));},
				httpExecutionContext.current());
	}
}