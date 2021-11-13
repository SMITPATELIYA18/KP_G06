package controllers;

import play.cache.AsyncCacheApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.*;
import services.GitHubAPIImpl;


import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Handles all requests regarding the user profile
 * @author Pradnya Kandarkar
 */
public class UserProfileController extends Controller {
	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private AsyncCacheApi asyncCacheApi;
	private GitHubAPIImpl apiClient;

	@Inject
	public UserProfileController(AssetsFinder assetsFinder, HttpExecutionContext httpExecutionContext, AsyncCacheApi asyncCacheApi, GitHubAPIImpl apiClient) {
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.asyncCacheApi = asyncCacheApi;
		this.apiClient = apiClient;
	}

	public CompletionStage<Result> getUserProfile(String username) {

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
