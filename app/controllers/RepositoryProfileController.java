package controllers;

import javax.inject.Inject;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import services.MyAPIClient;
import scala.concurrent.ExecutionContextExecutor;
import services.GitHubAPIImpl;
import com.typesafe.config.Config;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import views.html.RepositoryProfile.*;

/**
 * @author Farheen Jamadar
 * This controller contains action for fetching information
 * from Github API and send result to the client.
 *
 */

public class RepositoryProfileController extends Controller {
	private final Config config;
	private final AssetsFinder assetsFinder;
	private final WSClient client;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	@Inject
	public RepositoryProfileController(HttpExecutionContext httpExecutionContext, WSClient client, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config) {
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.config = config;
	}

	//TODO: Optimize, get IssueList from Cache as well -> Map, timeouts, CompletableFuture, javadoc, test cases
	public CompletionStage<Result> getRepositoryProfile(String ownerName, String repositoryName) throws ExecutionException, InterruptedException {

		MyAPIClient apiClient = new MyAPIClient(client, config);
		/*CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> apiClient.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));*/

		return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() ->  apiClient.getRepositoryProfile(ownerName, repositoryName))
				.thenCombineAsync(
						asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
								() -> apiClient.getRepositoryIssue(ownerName + "/" + repositoryName)),
						(repositoryProfileDetail, issueList) -> {
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetail,  60 * 15);
							//TODO: Optimize
							return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetail, issueList.getIssueTitles().stream().limit(20).collect(Collectors.toList()), assetsFinder));
						},
						httpExecutionContext.current()
				);

		/*return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() -> apiClient.getRepositoryProfile(ownerName, repositoryName)
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
						return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetails, issueList, assetsFinder));
					}, httpExecutionContext.current()));*/
	}
}
