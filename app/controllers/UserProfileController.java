package controllers;

import com.typesafe.config.Config;
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

	@Inject
	public UserProfileController(AssetsFinder assetsFinder, WSClient client, Config config, HttpExecutionContext httpExecutionContext) {
		this.assetsFinder = assetsFinder;
		this.client = client;
		this.config = config;
		this.httpExecutionContext = httpExecutionContext;
	}

	public CompletionStage<Result> getUserProfile(String username) {
		MyAPIClient apiClient = new MyAPIClient(client, config);
		return apiClient.getUserProfileByUsername(username)
				.thenApplyAsync(profileData ->
					ok(views.html.userprofile.profile.render(username, profileData, assetsFinder)),
						httpExecutionContext.current());

	}
	
}
