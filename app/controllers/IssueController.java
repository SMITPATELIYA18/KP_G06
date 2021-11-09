package controllers;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import play.libs.ws.WSClient;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import services.MyAPIClient;
import com.typesafe.config.Config;

public class IssueController extends Controller {
	private final ActorSystem actorSystem;
	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private WSClient client;
	private final Config config;

	@Inject
	public IssueController(ActorSystem actorSystem, AssetsFinder assetsFinder,
			HttpExecutionContext httpExecutionContext, WSClient client, Config config) {
		this.actorSystem = actorSystem;
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.client = client;
		this.config = config;
	}

	public CompletionStage<Result> getIssueStat(Http.Request request) {
		Optional<String> query = request.queryString("repoName");
		MyAPIClient apiClient = new MyAPIClient(client, config);
		return apiClient.getRepositoryIssue(query.get()).thenApplyAsync(
				issueModel -> { System.out.println(issueModel); return ok(views.html.issues.render(issueModel, assetsFinder));},
				httpExecutionContext.current());
	}
}
