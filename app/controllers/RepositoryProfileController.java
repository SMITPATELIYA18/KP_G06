package controllers;

import javax.inject.Inject;

import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import services.GitHubAPIImpl;
import com.typesafe.config.Config;
import services.github.GitHubAPI;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This controller contains an action to handle HTTP requests
 * to the repository profile page.
 * @author Farheen Jamadar
 */
public class RepositoryProfileController extends Controller {
	private final Config config;
	private final AssetsFinder assetsFinder;
	private final WSClient client;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	@Inject
	private GitHubAPI gitHubAPIImpl;

	@Inject
	public RepositoryProfileController(HttpExecutionContext httpExecutionContext, WSClient client, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config, GitHubAPIImpl gitHubAPIImpl) {
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.config = config;
		this.gitHubAPIImpl = gitHubAPIImpl;
	}

	/*TODO: Optimize, get IssueList from Cache as well -> Map, timeouts, CompletableFuture, javadoc, test cases*/
	/**
	 * An action that renders an HTML page with repository profile details queried by the user.
	 * The configuration in the <code>routes</code> file means that
	 * this method will be called when the application receives a
	 * <code>GET</code> request with a path of <code>/repositoryProfile/:username/:repositoryName</code>.
	 * @param username  Owner of the repository
	 * @param repositoryName  Repository Name
	 * @return Future CompletionStage Result
	 * @author Farheen Jamadar
	 */

	public CompletionStage<Result> getRepositoryProfile(String username, String repositoryName){

		/*CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> gitHubAPI.getRepositoryIssue(username + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));*/

		return asyncCacheApi.getOrElseUpdate(username + "/" + repositoryName,
				() ->  gitHubAPIImpl.getRepositoryProfile(username, repositoryName))
				.thenCombineAsync(
						asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
								() -> gitHubAPIImpl.getRepositoryIssue(username + "/" + repositoryName)),
						(repositoryProfileDetail, issueList) -> {
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(username + "/" + repositoryName, repositoryProfileDetail,  60 * 15);
							List<String> list = issueList.getIssueTitles().stream().limit(20).collect(Collectors.toList());
							return ok(views.html.repositoryProfile.profile.render(username, repositoryName, repositoryProfileDetail, Optional.ofNullable(list).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
						},
						httpExecutionContext.current()
				);

		/*return asyncCacheApi.getOrElseUpdate(username + "/" + repositoryName,
				() -> gitHubAPI.getRepositoryProfile(username, repositoryName)
						.thenApplyAsync(repositoryProfileDetails -> {
							List<String> issueList = null;
							try {
								//TODO: Optimize
								issueList = issues.toCompletableFuture().get().getIssueTitles().stream().limit(20).collect(Collectors.toList());
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(username + "/" + repositoryName, repositoryProfileDetails,  60 * 15);
							return ok(repositoryProfile.render(username, repositoryName, repositoryProfileDetails, Optional.ofNullable(issueList).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
						}, httpExecutionContext.current()));*/
	}
}
