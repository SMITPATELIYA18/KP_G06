package controllers;

import models.SearchCacheStore;
import models.SearchRepository;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.*;
import services.GitHubAPIImpl;
import services.github.GitHubAPI;


import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This controller contains actions to handle HTTP requests to the application
 * @author Smit Pateliya
 * @author Farheen Jamadar
 * @author Indraneel Rachakonda
 * @author Pradnya Kandarkar
 */
public class GitterificController extends Controller {
	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	@Inject
	private GitHubAPI gitHubAPIImpl;

	@Inject
	public GitterificController(AssetsFinder assetsFinder, HttpExecutionContext httpExecutionContext, AsyncCacheApi asyncCacheApi) {
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = asyncCacheApi;
	}

	/**
	 * An action that renders an HTML page with search input form.
	 * The configuration in the <code>routes</code> file means that
	 * this method will be called when the application receives a
	 * <code>GET</code> request with a path of <code>/</code>.
	 * @param request HTTP Request containing the search query
	 * @return Future CompletionStage Result
	 * @author SmitPateliya, Farheen Jamadar
	 */

	public CompletionStage<Result> index(Http.Request request) {
		Optional<String> query = request.queryString("search");
		if (query.isEmpty() || query.get().equals("")) {
			asyncCacheApi.remove("search");
			return CompletableFuture.supplyAsync(() -> ok(views.html.index.render(null, assetsFinder)));
		}

		CompletionStage<SearchRepository> newSearchData = asyncCacheApi.getOrElseUpdate("search_" + query.get(), () -> {
			CompletionStage<SearchRepository> searchRepository = gitHubAPIImpl.getRepositoryFromSearchBar(query.get());
			asyncCacheApi.set("search_" + query.get(), searchRepository, 60 * 15);
			return searchRepository;
		});

		return  newSearchData.thenCombineAsync(
				asyncCacheApi.get("search"),
				(newData, cacheData) -> {
					SearchCacheStore store = new SearchCacheStore();
					if(cacheData.isPresent()){
						store = (SearchCacheStore) cacheData.get();
					}
					if(!store.getSearches().contains(newData)){
						store.addNewSearch(newData);
					}
					asyncCacheApi.set("search", store, 60 * 15);
					return ok(views.html.index.render(store, assetsFinder));
				},
				httpExecutionContext.current()
		);
	}

	/**
	 * Gets the user profile information
	 * @param username
	 * @return
	 * @author Pradnya Kandarkar
	 */
	public CompletionStage<Result> getUserProfile(String username) {

		return asyncCacheApi.getOrElseUpdate(username + "_profile",
				() -> gitHubAPIImpl.getUserProfileByUsername(username))
						.thenCombineAsync(asyncCacheApi.getOrElseUpdate(username + "_repositories",
								() -> gitHubAPIImpl.getUserRepositories(username)),
								(userProfile, userRepositories) -> {
									asyncCacheApi.set(username + "_profile", userProfile);
									asyncCacheApi.set(username + "_repositories", userRepositories);
									return ok(views.html.userprofile.profile.render(username, userProfile, userRepositories, assetsFinder));
								},
								httpExecutionContext.current()
						);
	}

	/*TODO: Optimize, get IssueList from Cache as well -> Map, timeouts, CompletableFuture, javadoc, test cases*/
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
						() ->  gitHubAPIImpl.getRepositoryProfile(ownerName, repositoryName))
				.thenCombineAsync(
						asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
								() -> gitHubAPIImpl.getRepositoryIssue(ownerName + "/" + repositoryName)),
						(repositoryProfileDetail, issueList) -> {
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetail,  60 * 15);
							List<String> list = issueList.getIssueTitles().stream().limit(20).collect(Collectors.toList());
							return ok(views.html.RepositoryProfile.profile.render(ownerName, repositoryName, repositoryProfileDetail, Optional.ofNullable(list).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
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

	/**
	 * This method gives issues' title statics which are returning from API.
	 * @author smitpateliya
	 * @param request Gets repository names.
	 * @return Future Result which contains issues' title stats.
	 */

	public CompletionStage<Result> getIssueStat(String repoName) {
		repoName = repoName.replace("+", "/");
		return gitHubAPIImpl.getRepositoryIssue(repoName).thenApplyAsync(
				issueModel -> {return ok(views.html.issues.render(issueModel, assetsFinder));},
				httpExecutionContext.current());
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
						() -> gitHubAPIImpl.getTopicRepository(topic))
				.thenApplyAsync((searchResult) -> {
							asyncCacheApi.set("topic_" + topic, searchResult,  60 * 15);
							return ok(views.html.topics.topics.render(searchResult, assetsFinder));
						}, httpExecutionContext.current()
				);
	}
}
