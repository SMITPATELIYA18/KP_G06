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
import services.GitHubAPIImpl;
import com.typesafe.config.Config;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page i.e. the search repository page
 * @author SmitPateliya, Farheen Jamadar
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
