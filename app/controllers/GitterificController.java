package controllers;

import com.google.inject.Inject;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.*;
import services.GitterificService;
import services.github.GitHubAPI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import views.html.repositoryprofile.*;

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

	/**
	 * @param assetsFinder For finding assets according to configured base path and URL base
	 * @param httpExecutionContext For managing Play Java HTTP thread local state
	 * @param asyncCacheApi For utilizing asynchronous cache
	 */
	@Inject
	public GitterificController(AssetsFinder assetsFinder, HttpExecutionContext httpExecutionContext, AsyncCacheApi asyncCacheApi, GitHubAPI gitHubAPIInst, GitterificService gitterificService) {
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = asyncCacheApi;
		this.gitHubAPIInst = gitHubAPIInst;
		this.gitterificService = gitterificService;
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
	public CompletionStage<Result> index(String query) {
		if (query.isEmpty()) {
			asyncCacheApi.remove("search");
			return CompletableFuture.supplyAsync(() -> ok(views.html.index.render(null, assetsFinder)));
		}

		return gitterificService.getRepositoryFromSearch(query).thenApplyAsync(
				searchResults -> ok(views.html.index.render(searchResults,
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
	public CompletionStage<Result> getUserProfile(String username) {

		return gitterificService.getUserProfile(username).thenApplyAsync(
				userInfo -> ok(views.html.userprofile.userprofile.render(username,
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
	public CompletionStage<Result> getRepositoryProfile(String username, String repositoryName){
		return gitterificService.getRepositoryProfile(username, repositoryName).thenApplyAsync(
				repositoryData -> ok(repositoryProfile.render(username,
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

	public CompletionStage<Result> getIssueStat(String repoName) {
		repoName = repoName.replace("+", "/");
		return gitHubAPIInst.getRepositoryIssue(repoName).thenApplyAsync(
				issueModel -> {return ok(views.html.issues.render(issueModel, assetsFinder));},
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
	public CompletionStage<Result> getTopicRepository(String topic) {
		return gitterificService.getTopicRepository(topic).thenApplyAsync(
				topicDetails -> ok(views.html.topics.topics.render(topicDetails,
						assetsFinder)),
				httpExecutionContext.current());
	}
}
