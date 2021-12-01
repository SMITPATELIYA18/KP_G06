package actors;

import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import models.IssueModel;
import services.github.GitHubAPI;

/**
 * This Actor handles Issue Statistics related activity - processing data using Stream API 
 * @author Smit Pateliya
 *
 */

public class IssueStatActor extends AbstractActor {
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(),this);
	private ActorRef superviserActor;
	private GitHubAPI gitHubAPI;
	
	/**
	 * This constructor initializes data.
	 * @param superviserActor Receiving  Supervisor Actor Reference
	 * @param gitHubAPI To call Git hub Rest API
	 * @author Smit Pateliya
	 */
	public IssueStatActor(ActorRef superviserActor, GitHubAPI gitHubAPI) {
		this.gitHubAPI = gitHubAPI;
		this.superviserActor = superviserActor;
	}
	
	/**
	 * Creating Actor for Issue Statistics
	 * @param supervisorActor Receiving  Supervisor Actor Reference
	 * @param gitHubAPI To call Git hub Rest API
	 * @return return Actor Reference
	 * @author Smit Pateliya
	 */
	
	public static Props props(ActorRef  supervisorActor, GitHubAPI gitHubAPI) {
		return Props.create(IssueStatActor.class, supervisorActor, gitHubAPI);
	}
	
	/**
	 * This method calls before initializing actor
	 * @author Smit Pateliya
	 */
	@Override
	public void preStart() {
		log.info
		("Issue Stat Actor has started");
	}
	
	/**
	 * This method Handles Incoming messages and redirect to appropriate method
	 * @author Smit Pateliya
	 */
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Messages.GetRepositoryIssueActor.class, issueStatRequest -> {
			getIssueStat(issueStatRequest).thenAcceptAsync(this::returnResult);
		}).matchAny(other -> log.error("Error! PLease check your Message")).build();
	}
	
	/**
	 * This method calls REST API for getting data.
	 * @param issueStatRequest Receiving Repository name for calling API
	 * @return JSON data in the form Future
	 * @author Smit Pateliya
	 * @throws Exception 
	 */
	private CompletionStage<JsonNode> getIssueStat(Messages.GetRepositoryIssueActor issueStatRequest) throws Exception {
		return gitHubAPI.getRepositoryIssue(issueStatRequest.repoFullName).thenApplyAsync((IssueModel issueModel) -> {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode finalResult = mapper.createObjectNode();
			if(issueModel.getIssueTitles().get(0).equals("Error! Repository does not present!")) {
				finalResult.put("error", true);
				finalResult.put("errorMessage", "Error! This Repository does not have Issues");
				finalResult.put("responseType", "issueStatInfo");
				ObjectNode result = mapper.createObjectNode();
				result.put("repoFullName", issueModel.getRepoFullName());
				finalResult.set("result", result);
				return finalResult;
			}
			ObjectNode result = mapper.createObjectNode();
			result.put("repoFullName", issueModel.getRepoFullName());
			ObjectNode worldLevelData = mapper.createObjectNode();
			issueModel.getWordLevelData().forEach((key,value) -> worldLevelData.put(key, value));
			result.set("wordLevelData", worldLevelData);
			finalResult.put("responseType", "issueStatInfo");
			finalResult.set("result", result);
			return finalResult;
		});
	}
	
	/**
	 * This method return API reponse to the supervisor actor
	 * @param issueModel data from API
	 * @author Smit Pateliya
	 */
	private void returnResult(JsonNode issueModel) {
		superviserActor.tell(new Messages.IssueStatInfo(issueModel), getSelf());
	}
	
}
