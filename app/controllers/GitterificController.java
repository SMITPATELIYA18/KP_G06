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

import views.html.repositoryprofile.*;

import static akka.pattern.PatternsCS.ask;

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

	private final Timeout t = new Timeout(Duration.create(1, TimeUnit.SECONDS));
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("controllers.GitterificController");

	/**
	 * @param assetsFinder For finding assets according to configured base path and URL base
	 * @param httpExecutionContext For managing Play Java HTTP thread local state
	 * @param asyncCacheApi For utilizing asynchronous cache
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
	 * @param query request HTTP Request containing the search query
	 * @return Future CompletionStage Result
	 * @author SmitPateliya, Farheen Jamadar
	 */
	public CompletionStage<Result> index(String query, Http.Request request) {
		if (query.isEmpty()) {
			asyncCacheApi.remove("search");
			return CompletableFuture.supplyAsync(() -> ok(views.html.index.render(request, null, assetsFinder)));
		}

		return gitterificService.getRepositoryFromSearch(query).thenApplyAsync(
				searchResults -> ok(views.html.index.render(request, searchResults,
						assetsFinder)),
				httpExecutionContext.current());

	}

	/**
	 * Renders an HTML page containing all available public profile information about a user, as well as all the
	 * repositories of that user
	 *
	 * The configuration in the <code>routes</code> file means that this method will be called when the application
	 * receives a <code>GET</code> request with a path of <code>/user-profile/:username</code>
	 * @param username Username to fetch the details for
	 * @return CompletionStage&lt;Result&gt; which contains available public profile information and repositories for a user
	 * @author Pradnya Kandarkar
	 */
	public CompletionStage<Result> getUserProfile(String username, Http.Request request) {

		return gitterificService.getUserProfile(username).thenApplyAsync(
				userInfo -> ok(views.html.userprofile.userprofile.render(request, username,
						userInfo.get("profile"),
						userInfo.get("repositories"),
						assetsFinder)),
				httpExecutionContext.current());
	}
/**
	 * Renders an HTML page with repository profile details.
	 *
	 * The configuration in the <code>routes</code> file means that this method will be called when the application
	 * receives a <code>GET</code> request with a path of <code>/repositoryProfile/:username/:repositoryName</code>.
	 * @param username  Owner of the repository
	 * @param repositoryName  Repository Name
	 * @return Future CompletionStage Result
	 * @author Farheen Jamadar
	 */
	public CompletionStage<Result> getRepositoryProfile(String username, String repositoryName, Http.Request request){
		return gitterificService.getRepositoryProfile(username, repositoryName).thenApplyAsync(
				repositoryData -> ok(repositoryProfile.render(request, username,
							repositoryName,
							repositoryData.get("repositoryProfile"),
							repositoryData.get("issueList"),
							assetsFinder)),
				httpExecutionContext.current());
	}

	/**
	 * This method gives issues' title statics which are returning from API.
	 * @author smitpateliya
	 * @param repoName Gets repository names.
	 * @return Future Result which contains issues' title stats.
	 */

	public CompletionStage<Result> getIssueStat(String repoName, Http.Request request) {
		repoName = repoName.replace("+", "/");
		return gitHubAPIInst.getRepositoryIssue(repoName).thenApplyAsync(
				issueModel -> {return ok(views.html.issues.render(request, issueModel, assetsFinder));},
				httpExecutionContext.current());
	}

	/**
	 * Renders an HTML page with the top 10 repositories containing the topic provided by the user.
	 *
	 * The configuration in the <code>routes</code> file means that
	 * this method will be called when the application receives a
	 * <code>GET</code> request with a path of <code>/topics/:topic</code>.
	 * @param topic  Topic based on which the repositories will be retrieved
	 * @return Future CompletionStage Result
	 * @author Indraneel Rachakonda
	 */
	public CompletionStage<Result> getTopicRepository(String topic, Http.Request request) {
		return gitterificService.getTopicRepository(topic).thenApplyAsync(
				topicDetails -> ok(views.html.topics.topics.render(request, topicDetails,
						assetsFinder)),
				httpExecutionContext.current());
	}

	public WebSocket ws() {
		return WebSocket.Json.accept(request -> ActorFlow.actorRef(out -> SupervisorActor.props(out, gitHubAPIInst, asyncCacheApi), actorSystem, materializer));

		// return WebSocket.Json.accept(request -> ActorFlow.actorRef(out -> supervisorActor., actorSystem, materializer));

		/*return WebSocket.Json.acceptOrResult(request -> {
			System.out.println("Type of 'request': " + request.getClass());
			System.out.println("Request URI: " + request.uri());
			if (sameOriginCheck(request)) {
				final CompletionStage<Flow<JsonNode, JsonNode, NotUsed>> future = wsFutureFlow(request);
				final CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> stage = future.thenApply(F.Either::Right);
				return stage.exceptionally(this::logException);
			} else {
				return forbiddenResult();
			}
		});*/
	}

	/*@SuppressWarnings("unchecked")
	private CompletionStage<Flow<JsonNode, JsonNode, NotUsed>> wsFutureFlow(Http.RequestHeader request) {
		long id = request.asScala().id();
		SupervisorActor.Create create = new SupervisorActor.Create(Long.toString(id));

		return ask(supervisorActor, create, t).thenApply((Object flow) -> {
			final Flow<JsonNode, JsonNode, NotUsed> f = (Flow<JsonNode, JsonNode, NotUsed>) flow;
			return f.named("websocket");
		});
	}

	private CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> forbiddenResult() {
		final Result forbidden = Results.forbidden("forbidden");
		final F.Either<Result, Flow<JsonNode, JsonNode, ?>> left = F.Either.Left(forbidden);

		return CompletableFuture.completedFuture(left);
	}

	private F.Either<Result, Flow<JsonNode, JsonNode, ?>> logException(Throwable throwable) {
		logger.error("Cannot create websocket", throwable);
		Result result = Results.internalServerError("error");
		return F.Either.Left(result);
	}

	*/
	/**
	 * Checks that the WebSocket comes from the same origin.  This is necessary to protect
	 * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
	 * <p>
	 * See https://tools.ietf.org/html/rfc6455#section-1.3 and
	 * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
	 */
	/*
	private boolean sameOriginCheck(Http.RequestHeader rh) {
		final Optional<String> origin = rh.header("Origin");

		if (! origin.isPresent()) {
			logger.error("originCheck: rejecting request because no Origin header found");
			return false;
		} else if (originMatches(origin.get())) {
			logger.debug("originCheck: originValue = " + origin);
			return true;
		} else {
			logger.error("originCheck: rejecting request because Origin header value " + origin + " is not in the same origin");
			return false;
		}
	}

	private boolean originMatches(String origin) {
		return origin.contains("localhost:9000") || origin.contains("localhost:19001");
	}*/
}
