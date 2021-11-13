package controllers;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import akka.actor.ActorSystem;
import models.SearchCacheStore;
import models.SearchRepository;
import play.cache.Cached;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import scala.concurrent.ExecutionContextExecutor;
import services.MyAPIClient;
import com.typesafe.config.Config;

/**
 * @author SmitPateliya This controller contains action for fetching information
 *         from Github API and send result to the client.
 *
 */

public class IndexPageController extends Controller {
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
	public IndexPageController(HttpExecutionContext httpExecutionContext, WSClient client, ActorSystem actorSystem,
			ExecutionContextExecutor executor, AssetsFinder assetsFinder, AsyncCacheApi cache, Config config) {
		this.actorSystem = actorSystem;
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
			return asyncCacheApi.get("search").thenApplyAsync((cacheResult) -> {
				// SearchCacheStore answer = (SearchCacheStore) cacheResult.orElse(null);
				return ok(views.html.index.render(null, assetsFinder));
			});
		}

		MyAPIClient apiClient = new MyAPIClient(client, config);
		//apiClient.getRepositoryFromSearchBar(query.get()).thenCompose(searc);
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
//		return apiClient.getRepositoryFromSearchBar(query.get()).thenApplyAsync(searchRepository -> {
//			CompletionStage<Optional<SearchCacheStore>> data = asyncCacheApi.get("search");
//			Optional<SearchCacheStore> cacheData = Optional.empty();
//			try {
//				cacheData = data.toCompletableFuture().get();
//			} catch (InterruptedException e) {
//
//			} catch (ExecutionException e) {
//
//			} catch (CancellationException e) {
//
//			}
//			SearchCacheStore store;
//
//			if (cacheData.isPresent()) {
//				store = cacheData.get();
//			} else {
//				store = new SearchCacheStore();
//			}
//			store.addNewSearch(searchRepository);
//			asyncCacheApi.set("search", store, 60 * 20);
//			return ok(views.html.index.render(store, null, assetsFinder));
//		}, httpExecutionContext.current());
	}

}
