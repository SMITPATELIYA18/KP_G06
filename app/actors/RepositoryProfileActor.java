package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.cache.AsyncCacheApi;
import services.github.GitHubAPI;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Actor to handle Repository profile feature
 * @author Farheen Jamadar
 */
public class RepositoryProfileActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef sessionActor;
    private GitHubAPI gitHubAPIInst;
    private AsyncCacheApi asyncCacheApi;

    /**
     * @param sessionActor Supervisor Actor Reference
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface for GitHub API calls
     * @param asyncCacheApi Asynchronous Cache
     */
    public RepositoryProfileActor(ActorRef sessionActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        this.sessionActor = sessionActor;
        this.gitHubAPIInst = gitHubAPIInst;
        this.asyncCacheApi = asyncCacheApi;
    }

    /**
     * Creates Repository profile actor
     * @param sessionActor Supervisor Actor Reference
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface for GitHub API calls
     * @param asyncCacheApi Asynchronous Cache
     * @return <code>Props</code> object Repository profile actor
     * @author Farheen Jamadar
     */
    public static Props props(ActorRef sessionActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        return Props.create(RepositoryProfileActor.class, sessionActor, gitHubAPIInst, asyncCacheApi);
    }

    /**
     * Called before Repository Actor receives any message
     * @author Farheen Jamadar
     */
    @Override
    public void preStart() {
        System.out.println("Created a repository profile actor.");
    }

    /**
     * Called after Repository Actor children/activites are stopped
     * @author Farheen Jamadar
     */
    @Override
    public void postStop() {
        log.info("Stopped the supervisor actor.");
    }

    /**
     * Handles messages by matching the class of an incoming message and takes appropriate action
     * @return <code>AbstractActor.Receive</code> defining the messages that can be processed by this actor and how they will be processed
     * @author Farheen Jamadar
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.GetRepositoryProfileActor.class, repositoryProfileRequest ->
                    onGetRepositoryProfile(repositoryProfileRequest).thenAcceptAsync(this::processRepositoryProfileResult))
                .matchAny(other -> log.error("Received unknown message type: " + other.getClass()))
                .build();
    }

    /**
     * Fetches and builds response containing repository profile information and corresponding top 20 issues
     * @return <code>CompletionStage&lt;JsonNode&gt;</code> which containing repository profile information and corresponding top 20 issues
     * @author Farheen Jamadar
     */
    private CompletionStage<JsonNode> onGetRepositoryProfile(Messages.GetRepositoryProfileActor repositoryProfileRequest) throws Exception {

        return asyncCacheApi.getOrElseUpdate(repositoryProfileRequest.username + "/" + repositoryProfileRequest.repositoryName,
                        () -> gitHubAPIInst.getRepositoryProfile(repositoryProfileRequest.username, repositoryProfileRequest.repositoryName))
                .thenCombineAsync(
                        asyncCacheApi.getOrElseUpdate(repositoryProfileRequest.username + repositoryProfileRequest.repositoryName + "/20issues",
                                () -> gitHubAPIInst.getRepositoryIssue(repositoryProfileRequest.username + "/" + repositoryProfileRequest.repositoryName)),
                        (repositoryProfileDetail, issueList) -> {
                            asyncCacheApi.set(repositoryProfileRequest.username + repositoryProfileRequest.repositoryName + "/20issues", issueList,  60 * 15);
                            asyncCacheApi.set(repositoryProfileRequest.username + "/" + repositoryProfileRequest.repositoryName, repositoryProfileDetail,  60 * 15);

                            List<String> list = issueList.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode repositoryData = mapper.createObjectNode();
                            ArrayNode arrayNode = mapper.createArrayNode();
                            list.forEach(arrayNode::add);

                            repositoryData.put("responseType", "repositoryProfileInfo");
                            repositoryData.set("repositoryProfile", repositoryProfileDetail);
                            repositoryData.set("issueList", arrayNode);

                            return repositoryData;
                        }
                );
    }

    /**
     * Sends the repository profile information to be forwarded to the client
     * @param repositoryProfileInfo <code>JsonNode</code> containing retrieved repository profile information and corresponding top 20 issues
     * @author Farheen Jamadar
     */
    private void processRepositoryProfileResult(JsonNode repositoryProfileInfo) {
        sessionActor.tell(new Messages.RepositoryProfileInfo(repositoryProfileInfo), getSelf());
    }
}
