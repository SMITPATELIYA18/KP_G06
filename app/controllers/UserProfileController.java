package controllers;

import com.typesafe.config.Config;
import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.*;
import services.MyAPIClient;


import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class UserProfileController extends Controller {
	private final AssetsFinder assetsFinder;
	private WSClient client;
	private final Config config;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;

	@Inject
	public UserProfileController(AssetsFinder assetsFinder, WSClient client, Config config, HttpExecutionContext httpExecutionContext, AsyncCacheApi asyncCacheApi) {
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.config = config;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = asyncCacheApi;
	}

	public CompletionStage<Result> getUserProfile(String username) {
		MyAPIClient apiClient = new MyAPIClient(client, config);

		return asyncCacheApi.getOrElseUpdate(username + "_profile",
				() -> apiClient.getUserProfileByUsername(username))
						.thenCombineAsync(asyncCacheApi.getOrElseUpdate(username + "_repositories",
								() -> apiClient.getUserRepositories(username)),
								(userProfile, userRepositories) -> {
									asyncCacheApi.set(username + "_profile", userProfile);
									asyncCacheApi.set(username + "_repositories", userRepositories);
									return ok(views.html.userprofile.profile.render(username, userProfile, userRepositories, assetsFinder));
								},
								httpExecutionContext.current()
						);
	}
}
