package controllers;

import javax.inject.Inject;

import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import services.GitHubAPIImpl;
import com.typesafe.config.Config;

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
	private GitHubAPIImpl gitHubAPI;

	@Inject
	public RepositoryProfileController(HttpExecutionContext httpExecutionContext, WSClient client, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config, GitHubAPIImpl githubAPI) {
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.config = config;
		this.gitHubAPI = githubAPI;
	}

	//TODO: Optimize, get IssueList from Cache as well -> Map, timeouts, CompletableFuture, javadoc, test cases
	/**
	 * An action that renders an HTML page with repository profile details queried by the user.
	 * The configuration in the <code>routes</code> file means that
	 * this method will be called when the application receives a
	 * <code>GET</code> request with a path of <code>/repositoryProfile/:ownerName/:repositoryName</code>.
	 * @param ownerName  Owner of the repository
	 * @param repositoryName  Repository Name
	 * @return Future CompletionStage Result
	 * @author Farheen Jamadar
	 */

	public CompletionStage<Result> getRepositoryProfile(String ownerName, String repositoryName){

		/*CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> gitHubAPI.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));*/

		return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() ->  gitHubAPI.getRepositoryProfile(ownerName, repositoryName))
				.thenCombineAsync(
						asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
								() -> gitHubAPI.getRepositoryIssue(ownerName + "/" + repositoryName)),
						(repositoryProfileDetail, issueList) -> {
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetail,  60 * 15);
							List<String> list = issueList.getIssueTitles().stream().limit(20).collect(Collectors.toList());
							return ok(views.html.repositoryProfile.profile.render(ownerName, repositoryName, repositoryProfileDetail, Optional.ofNullable(list).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
						},
						httpExecutionContext.current()
				);

		/*return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() -> gitHubAPI.getRepositoryProfile(ownerName, repositoryName)
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
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetails,  60 * 15);
							return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetails, Optional.ofNullable(issueList).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
						}, httpExecutionContext.current()));*/
	}
}
