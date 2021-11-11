package controllers;

import javax.inject.Inject;
import akka.actor.ActorSystem;
import play.mvc.*;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.*;
import scala.concurrent.ExecutionContextExecutor;
import services.MyAPIClient;
import com.typesafe.config.Config;

import java.util.concurrent.CompletionStage;

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

	public CompletionStage<Result> getRepositoryProfile(String ownerName, String repositoryName) {

		MyAPIClient apiClient = new MyAPIClient(client, config);
		return asyncCacheApi.getOrElseUpdate(ownerName + "/" + repositoryName, () -> apiClient.getRepositoryProfile(ownerName, repositoryName).thenApplyAsync(
						repositoryProfileDetails -> {
							//System.out.println("Controller: " + repositoryProfileDetails);
							asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetails,  60 * 15);
							return ok(repositoryProfile.render(repositoryProfileDetails.asJson(), assetsFinder));
						},
						httpExecutionContext.current()));

			/*return apiClient.getRepositoryProfile(ownerName, repositoryName).thenApplyAsync(
					repositoryProfileDetails -> {
						//System.out.println("Controller: " + repositoryProfileDetails);
						asyncCacheApi.set(ownerName + "/" + repositoryName, repositoryProfileDetails,  60 * 15);
						return ok(repositoryProfile.render(repositoryProfileDetails.asJson(), assetsFinder));
					},
			httpExecutionContext.current());*/
	}

	/*public Result repositoryDetails(String ownerName, String repositoryName) {
		System.out.println("Owner Name1: " + ownerName + " Repository Name: " + repositoryName);
		return ok(views.html.RepositoryProfile.repositoryProfile.render(ownerName, repositoryName));
	}*/
}
