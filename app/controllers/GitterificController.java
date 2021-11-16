package controllers;

import com.google.inject.Inject;
import models.SearchCacheStore;
import models.SearchRepository;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.*;
import services.GitterificService;
import services.github.GitHubAPI;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import views.html.repositoryprofile.*;

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
			/*if(query.isEmpty()) {
				System.out.println("Received query.isEmpty()");
			}
			if(query.get().equals("")) {
				System.out.println("Received query.get().equals(\"\")");
			}*/
			asyncCacheApi.remove("search");
			return CompletableFuture.supplyAsync(() -> ok(views.html.index.render(null, assetsFinder)));
		}

		CompletionStage<SearchRepository> newSearchData = asyncCacheApi.getOrElseUpdate("search_" + query.get(), () -> {
			CompletionStage<SearchRepository> searchRepository = gitHubAPIInst.getRepositoryFromSearchBar(query.get());
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

		/*CompletionStage<IssueModel> issues = asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues", () -> gitHubAPIImpl.getRepositoryIssue(ownerName + "/" + repositoryName)
				.thenApplyAsync(issueModel -> issueModel,
						httpExecutionContext.current()));*/

		return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
						() ->  gitHubAPIInst.getRepositoryProfile(ownerName, repositoryName))
				.thenCombineAsync(
						asyncCacheApi.getOrElseUpdate(repositoryName + "/20issues",
								() -> gitHubAPIInst.getRepositoryIssue(ownerName + "/" + repositoryName)),
						(repositoryProfileDetail, issueList) -> {
							asyncCacheApi.set(repositoryName + "/20issues", issueList,  60 * 15);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetail,  60 * 15);

							//TODO: Optimize
							List<String> list = issueList.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());
							if(list.get(0) == "Issue does not Present!" || list.get(0) == "Error! Repository does not present!"){
								list = null;
							}
							return ok(repositoryProfile.render(ownerName, repositoryName, repositoryProfileDetail, Optional.ofNullable(list).orElse(new ArrayList<String>()), assetsFinder));
						},
						httpExecutionContext.current()
				);

		/*return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName,
				() -> gitHubAPIImpl.getRepositoryProfile(ownerName, repositoryName)
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
							return ok(views.html.repositoryProfile.profile.render(ownerName, repositoryName, repositoryProfileDetails, Optional.ofNullable(issueList).orElse(Arrays.asList("No Issues Reported.")), assetsFinder));
						}, httpExecutionContext.current()));*/
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
						() -> gitHubAPIInst.getTopicRepository(topic))
				.thenApplyAsync((searchResult) -> {
							asyncCacheApi.set("topic_" + topic, searchResult,  60 * 15);
							return ok(views.html.topics.topics.render(searchResult, assetsFinder));
						}, httpExecutionContext.current()
				);
	}
}
