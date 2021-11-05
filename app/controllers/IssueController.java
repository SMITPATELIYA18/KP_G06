package controllers;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

import akka.actor.ActorSystem;
import play.libs.ws.WSClient;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import services.MyAPIClient;

public class IssueController extends Controller {
	private final ActorSystem actorSystem;
	private final AssetsFinder assetsFinder;
	private HttpExecutionContext httpExecutionContext;
	private WSClient client;

	@Inject
	public IssueController(ActorSystem actorSystem, AssetsFinder assetsFinder,
			HttpExecutionContext httpExecutionContext, WSClient client) {
		this.actorSystem = actorSystem;
		this.assetsFinder = assetsFinder;
		this.httpExecutionContext = httpExecutionContext;
		this.client = client;
	}

	public CompletionStage<Result> getIssueStat(Http.Request request) {
		Optional<String> query = request.queryString("repoName");
		MyAPIClient apiClient = new MyAPIClient(client);
		return apiClient.getRepositoryIssue(query.get()).thenApplyAsync(
				issueModel -> ok(views.html.issues.render(issueModel, assetsFinder)),
				httpExecutionContext.current());
	}
}
