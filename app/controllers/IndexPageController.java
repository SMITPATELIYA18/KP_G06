package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import akka.actor.ActorSystem;
import ch.qos.logback.core.util.Duration;
import models.RepositoryModel;
import models.SearchCacheStore;
import models.SearchRepository;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import scala.concurrent.ExecutionContextExecutor;
import scala.reflect.ClassTag;
import services.MyAPIClient;

/**
 * @author SmitPateliya This controller contains action for fetching information
 *         from Github API and send result to the client.
 *
 */

public class IndexPageController extends Controller {
	private final ActorSystem actorSystem;
//	private final ExecutionContextExecutor executor;
	private final AssetsFinder assetsFinder;
	private WSClient client;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	/**
	 * 
	 * @param actorSystem To run code after delay
	 * @param executor    To apply the result of the Completable Future.
	 */

	@Inject
	public IndexPageController(HttpExecutionContext httpExecutionContext, WSClient client, ActorSystem actorSystem,
			ExecutionContextExecutor executor, AssetsFinder assetsFinder, AsyncCacheApi cache) {
		this.actorSystem = actorSystem;
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = cache;
	}
	
	/**
	 * This method executes when user fetches index page.
	 * @param request Getting HTTP Request to get search query 
	 * @return Future CompletionStage Object 
	 */

	public CompletionStage<Result> index(Http.Request request) {
		Optional<String> query = request.queryString("search");
		if (query.isEmpty() || query.get() == "") {
			return asyncCacheApi.get("search").thenApplyAsync((cacheResult) -> {
				SearchCacheStore answer = (SearchCacheStore) cacheResult.orElse(null);
				return ok(views.html.index.render(answer, "Please, Enter the Search Query!",
						assetsFinder));
			});
		}

		MyAPIClient apiClient = new MyAPIClient(client);
		return apiClient.getRepositoryFromSearchBar(query.get()).thenApplyAsync(seaarchRepository -> {
			// ClassTag<SearchCacheStore> tag =
			// scala.reflect.ClassTag$.MODULE$.apply(SearchCacheStore.class);
			CompletionStage<Optional<SearchCacheStore>> data = asyncCacheApi.get("search");
			Optional<SearchCacheStore> cacheData = Optional.empty();
			try {
				cacheData = data.toCompletableFuture().get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (CancellationException e) {
			}
			SearchCacheStore store;
			if (cacheData.isPresent()) {
				store = cacheData.get();
				store.addNewSearch(seaarchRepository);
				asyncCacheApi.set("search", store,60*20);
			} else {
				store = new SearchCacheStore();
				store.addNewSearch(seaarchRepository);
				asyncCacheApi.set("search", store,60*20);
			}
			return ok(views.html.index.render(store, null, assetsFinder));
		}, httpExecutionContext.current());

	}
}
