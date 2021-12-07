package actors;

import java.util.HashMap;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import play.cache.AsyncCacheApi;
import scala.collection.concurrent.Map;
import services.github.GitHubAPI;


/**
 * Handles all topics feature related requests - about retrieving topics
 * information
 * 
 * @author Indraneel Rachakonda
 */
public class TopicActor extends AbstractActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	private ActorRef supervisorActor;
	private GitHubAPI gitHubAPIInst;
	private AsyncCacheApi asyncCacheApi;

	/**
	 * @param supervisorActor Actor reference for the supervisor actor
	 * @param gitHubAPIInst   Instance of <code>GitHubAPI</code> inteface, used to
	 *                        make external API calls to GitHub
	 * @param asyncCacheApi   For temporary data storage
	 */
	public TopicActor(ActorRef supervisorActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
		this.supervisorActor = supervisorActor;
		this.gitHubAPIInst = gitHubAPIInst;
		this.asyncCacheApi = asyncCacheApi;
	}

	/**
	 * Creates an actor with properties specified using parameters
	 * 
	 * @param supervisorActor Actor reference for the supervisor actor
	 * @param gitHubAPIInst   Instance of <code>GitHubAPI</code> inteface, used to
	 *                        make external API calls to GitHub
	 * @param asyncCacheApi   For temporary data storage
	 * @return A <code>Props</code> object holding actor configuration
	 * @author Indraneel Rachakonda
	 */
	public static Props props(ActorRef supervisorActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
		return Props.create(TopicActor.class, supervisorActor, gitHubAPIInst, asyncCacheApi);
	}

	/**
	 * Executes before any other action related to this actor
	 * 
	 * @author Indraneel Rachakonda
	 */
	@Override
	public void preStart() {
		log.info("Created a user profile actor.");
	}

	/**
	 * Executes after all other actions related to this actor
	 * 
	 * @author Indraneel Rachakonda
	 */
	@Override
	public void postStop() {
		log.info("Stopped the user profile actor.");
	}

	/**
	 * Handles incoming messages for this actor - matches the class of an incoming
	 * message and takes appropriate action
	 * 
	 * @return <code>AbstractActor.Receive</code> defining the messages that can be
	 *         processed by this actor and how they will be processed
	 * @author Indraneel Rachakonda
	 */
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Messages.GetTopic.class, topicRequest -> {
			onGetTopic(topicRequest).thenAcceptAsync(this::processTopicResult);
		})
//                .matchAny(other -> getSender().tell(new Messages.UnknownMessageReceived(), getSelf()))
				.matchAny(other -> log.error("Received unknown message type: " + other.getClass())).build();
	}

	/**
	 * Retrieves all available public profile information about a user, as well as
	 * all the repositories of that user
	 * 
	 * @param userProfileRequest <code>Messages.GetUserProfile</code> object
	 *                           containing the username for the request
	 * @return <code>CompletionStage&lt;JsonNode&gt;</code> which contains available
	 *         public profile information and repositories for a user
	 * @throws Exception If the call cannot be completed due to an error
	 * @author Indraneel Rachakonda
	 */
	private CompletionStage<JsonNode> onGetTopic(Messages.GetTopic topicRequest) throws Exception {
		return asyncCacheApi.getOrElseUpdate(topicRequest.topic + "_topic",
				() -> gitHubAPIInst.getTopicRepository(topicRequest.topic)).thenApplyAsync((topic) -> {

					ObjectMapper mapper = new ObjectMapper();
					ObjectNode finalResult = mapper.createObjectNode();
					asyncCacheApi.set(topicRequest.topic + "_topic", topic);
					finalResult.put("responseType", "topicInfo");
					finalResult.put("query", topic.getQuery());
					finalResult.set("repositoryList", mapper.valueToTree(topic.getRepositoryList()));

					return finalResult;
				});
	}

	/**
	 * Sends the topic information to be forwarded to the client
	 * 
	 * @param topicInfo <code>JsonNode</code> containing retrieved topic information
	 * @author Indraneel Rachakonda
	 */
	private void processTopicResult(JsonNode topicInfo) {
		supervisorActor.tell(new Messages.TopicInfo(topicInfo), getSelf());
	}
}
