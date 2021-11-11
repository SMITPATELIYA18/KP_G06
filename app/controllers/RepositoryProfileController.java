package controllers;

import javax.inject.Inject;
import akka.actor.ActorSystem;
import models.IssueModel;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import scala.concurrent.ExecutionContextExecutor;
import services.MyAPIClient;
import com.typesafe.config.Config;

import java.util.List;
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
	public RepositoryProfileController(HttpExecutionContext httpExecutionContext, WSClient client, ActorSystem actorSystem,
									   ExecutionContextExecutor executor, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config) {
		this.actorSystem = actorSystem;
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.config = config;
	}

	//TODO: Optimize, get IssueList from Cache as well -> Map, timeouts, CompletableFuture

	public CompletionStage<Result> getRepositoryProfile(String ownerName, String repositoryName) throws ExecutionException, InterruptedException {

		MyAPIClient apiClient = new MyAPIClient(client, config);

		/*CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> apiClient.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));*/
		/*CompletionStage<IssueModel> issues = apiClient.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
				httpExecutionContext.current());*/

		/*return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() -> apiClient.getRepositoryProfile(ownerName, repositoryName).thenApplyAsync(
						repositoryProfileDetails -> {
							List<String> issueList = null;
							try {
								issueList = issues.toCompletableFuture().get().getIssueTitles().stream().limit(20).collect(Collectors.toList());
								asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetails,  60 * 15);
							return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetails,  issueList, assetsFinder));
						},
						httpExecutionContext.current()));*/

		CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> apiClient.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));

		return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() -> apiClient.getRepositoryProfile(ownerName, repositoryName)
						.thenApplyAsync(repositoryProfileDetails -> {
							List<String> issueList = null;
							try {
								issueList = issues.toCompletableFuture().get().getIssueTitles().stream().limit(20).collect(Collectors.toList());
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetails,  60 * 15);
						return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetails, issueList, assetsFinder));
					}, httpExecutionContext.current()));


	}
}
