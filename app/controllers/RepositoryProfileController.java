package controllers;

import javax.inject.Inject;

import models.IssueModel;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import services.GitHubAPIImpl;
import com.typesafe.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import views.html.repositoryProfile.*;

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
	public CompletionStage<Result> getRepositoryProfile(String ownerName, String repositoryName) throws ExecutionException, InterruptedException {

		CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> gitHubAPI.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));

		/*return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() ->  gitHubAPI.getRepositoryProfile(ownerName, repositoryName))
				.thenCombineAsync(
						asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
								() -> gitHubAPI.getRepositoryIssue(ownerName + "/" + repositoryName)),
						(repositoryProfileDetail, issueList) -> {
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetail,  60 * 15);
							//TODO: Optimize
							return ok(profile.render(ownerName, repositoryName, repositoryProfileDetail, issueList.getIssueTitles().stream().limit(20).collect(Collectors.toList()), assetsFinder));
						},
						httpExecutionContext.current()
				);*/

		return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() -> gitHubAPI.getRepositoryProfile(ownerName, repositoryName)
						.thenApplyAsync(repositoryProfileDetails -> {
							CompletableFuture<List<String>> issueList = CompletableFuture.supplyAsync( () ->
									{
										List<String> list = null;
										try {
											list = issues.toCompletableFuture()
													.get()
													.getIssueTitles()
													.stream()
													.limit(20).parallel()
													.collect(Collectors.toList());
										} catch (InterruptedException e) {
											e.printStackTrace();
										} catch (ExecutionException e) {
											e.printStackTrace();
										}
										//
										return list;
									}
							);

							asyncCacheApi.set(repositoryName + "_20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "_" + repositoryName, repositoryProfileDetails,  60 * 15);
							List<String> list = null;
							try {
								list = issueList.get();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
							return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetails, Optional.ofNullable(list).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
					}, httpExecutionContext.current()));
	}
}
