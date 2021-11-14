package controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import models.SearchCacheStore;
import models.SearchRepository;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import scala.concurrent.ExecutionContextExecutor;
import services.GitHubAPIImpl;
import com.typesafe.config.Config;

/**
 * @author SmitPateliya, Farheen Jamadar
 * This controller contains action for fetching information
 *         from Github API and send result to the client.
 *
 */

public class IndexPageController extends Controller {
	private final Config config;
	private final AssetsFinder assetsFinder;
	private final WSClient client;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	@Inject
	public IndexPageController(HttpExecutionContext httpExecutionContext, WSClient client, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config) {
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
		this.config = config;
	}

	/**
	 * This method executes when user fetches index page.
	 * 
	 * @param request Getting HTTP Request to get search query
	 * @return Future CompletionStage Object
	 */

	public CompletionStage<Result> index(Http.Request request) {
		Optional<String> query = request.queryString("search");
		if (query.isEmpty() || query.get() == "") {
			asyncCacheApi.remove("search");
			return CompletableFuture.supplyAsync(() -> ok(views.html.index.render(null, assetsFinder)));
		}

		GitHubAPIImpl apiClient = new GitHubAPIImpl(client, config);

		CompletionStage<SearchRepository> newSearchData = asyncCacheApi.getOrElseUpdate("search_" + query.get(), () -> {
			CompletionStage<SearchRepository> searchRepository = apiClient.getRepositoryFromSearchBar(query.get());
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

}
