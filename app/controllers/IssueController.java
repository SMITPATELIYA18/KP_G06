package controllers;

import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import play.libs.ws.WSClient;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import services.GitHubAPIImpl;

/**
 * This class handles all the information regarding the issues.
 * @author Smit Pateliya
 *
 */
public class IssueController extends Controller {
	private final ActorSystem actorSystem;
	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private WSClient client;
	private final Config config;
	
	/**
	 * Injects all the different parameter
	 * @param actorSystem Creates Actor System
	 * @param assetsFinder Display UI Part
	 * @param httpExecutionContext
	 * @param client Handles calling Live API
	 * @param config 
	 */

	@Inject
	public IssueController(ActorSystem actorSystem, AssetsFinder assetsFinder,
			HttpExecutionContext httpExecutionContext, WSClient client, Config config) {
		this.actorSystem = actorSystem;
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.client = client;
		this.config = config;
	}
	
	/**
	 * This method gives issues' title statics which are returning from API.
	 * @param request Gets repository names.
	 * @return Future Result which contains issues' title stats.
	 */

	public CompletionStage<Result> getIssueStat(String repoName) {
		repoName = repoName.replace("+", "/");
		GitHubAPIImpl apiClient = new GitHubAPIImpl(client, config);
		return apiClient.getRepositoryIssue(repoName).thenApplyAsync(
				issueModel -> {return ok(views.html.issues.render(issueModel, assetsFinder));},
				httpExecutionContext.current());
	}
}
