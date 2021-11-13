package controllers;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import models.SearchCacheStore;
import models.SearchRepository;
import play.cache.Cached;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import services.MyAPIClient;
import com.typesafe.config.Config;

/**
 * @author SmitPateliya This controller contains action for fetching information
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
			return asyncCacheApi.get("search").thenApplyAsync((cacheResult) -> ok(views.html.index.render(null, assetsFinder)));
		}

		MyAPIClient apiClient = new MyAPIClient(client, config);
		CompletionStage<SearchRepository> searchRepoStage = apiClient.getRepositoryFromSearchBar(query.get());
		CompletionStage<Optional<SearchCacheStore>> cacheDataStage = asyncCacheApi.get("search");
		return searchRepoStage.thenCombineAsync(cacheDataStage, (searchData, cacheData) -> {
			SearchCacheStore store;

			if (cacheData.isPresent()) {
				store = cacheData.get();
			} else {
				store = new SearchCacheStore();
			}
			store.addNewSearch(searchData);
			asyncCacheApi.set("search", store, 60 * 20);
			return ok(views.html.index.render(store, assetsFinder));
		},httpExecutionContext.current());
	}

}
