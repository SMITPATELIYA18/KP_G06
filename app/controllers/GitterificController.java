package controllers;

import actors.SupervisorActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.util.Timeout;

import com.google.inject.Inject;
import org.slf4j.Logger;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.*;
import scala.concurrent.duration.Duration;
import services.GitterificService;
import services.github.GitHubAPI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Contains actions to handle HTTP requests to the application
 * @author Smit Pateliya
 * @author Farheen Jamadar
 * @author Indraneel Rachakonda
 * @author Pradnya Kandarkar
 */
public class GitterificController extends Controller {
	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;
	private GitHubAPI gitHubAPIInst;
	private GitterificService gitterificService;
	private final ActorSystem actorSystem;
	private Materializer materializer;

	/**
	 * @param assetsFinder For finding assets according to configured base path and URL base
	 * @param httpExecutionContext For managing Play Java HTTP thread local state
	 * @param asyncCacheApi For utilizing asynchronous cache
	 * @param gitHubAPIInst Instance of <code>GitHubAPI</code> interface for GitHub API calls
	 *                      //TODO: Farheen: Remove service class
	 * @param gitterificService For using GitHub Service Class
	 * @param actorSystem For creating actor system
	 * @param materializer Factory for stream execution engines
	 */
	@Inject
	public GitterificController(AssetsFinder assetsFinder, HttpExecutionContext httpExecutionContext, AsyncCacheApi asyncCacheApi, GitHubAPI gitHubAPIInst, GitterificService gitterificService, ActorSystem actorSystem, Materializer materializer) {
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = asyncCacheApi;
		this.gitHubAPIInst = gitHubAPIInst;
		this.gitterificService = gitterificService;
		this.actorSystem = actorSystem;
		this.materializer = materializer;
	}

	/**
	 * Renders an HTML page with search input form and displays the result once search query is keyed in.
	 *
	 * The configuration in the <code>routes</code> file means that this method will be called when the application
	 * receives a <code>GET</code> request with a path of <code>/</code>.
	 * @param request An HTTP request
	 * @return Future CompletionStage Result
	 * @author SmitPateliya, Farheen Jamadar
	 */
	public CompletionStage<Result> index(Http.Request request) {
		return CompletableFuture.supplyAsync(() -> ok(views.html.index.render(request, null, assetsFinder)));
	}

	/**
	 * Renders an HTML page with the top 10 repositories containing the topic provided by the user.
	 *
	 * The configuration in the <code>routes</code> file means that
	 * this method will be called when the application receives a
	 * <code>GET</code> request with a path of <code>/topics/:topic</code>.
	 * @param topic  Topic based on which the repositories will be retrieved
	 * @param request An HTTP request
	 * @return Future CompletionStage Result
	 * @author Indraneel Rachakonda
	 */
//	public CompletionStage<Result> getTopicRepository(String topic, Http.Request request) {
//		return gitterificService.getTopicRepository(topic).thenApplyAsync(
//				topicDetails -> ok(views.html.topics.topics.render(request, topicDetails,
//						assetsFinder)),
//				httpExecutionContext.current());
//	}

	/**
	 * Creates a websocket, connected to the supervisor actor and returns it
	 * @return <code>WebSocket</code> for further communication with client
	 * @author Pradnya Kandarkar, Smit Pateliya
	 */
	public WebSocket ws() {
		return WebSocket.Json.accept(request -> ActorFlow.actorRef(out -> SupervisorActor.props(out, gitHubAPIInst, asyncCacheApi), actorSystem, materializer));
	}
}
