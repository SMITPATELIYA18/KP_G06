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

public class RepositoryProfileActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef sessionActor;
    private GitHubAPI gitHubAPIInst;
    private AsyncCacheApi asyncCacheApi;

    public RepositoryProfileActor(ActorRef sessionActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        this.sessionActor = sessionActor;
        this.gitHubAPIInst = gitHubAPIInst;
        this.asyncCacheApi = asyncCacheApi;
    }

    public static Props props(ActorRef sessionActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        return Props.create(RepositoryProfileActor.class, sessionActor, gitHubAPIInst, asyncCacheApi);
    }

    @Override
    public void preStart() {
        System.out.println("Created a repository profile actor.");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.GetRepositoryProfileActor.class, repositoryProfileRequest -> {
                    onGetRepositoryProfile(repositoryProfileRequest).thenAcceptAsync(this::processRepositoryProfileResult);
                })
                .build();
    }

    private CompletionStage<JsonNode> onGetRepositoryProfile(Messages.GetRepositoryProfileActor repositoryProfileRequest) throws Exception {

        return asyncCacheApi.getOrElseUpdate(repositoryProfileRequest.username + "/" + repositoryProfileRequest.repositoryName,
                        () -> gitHubAPIInst.getRepositoryProfile(repositoryProfileRequest.username, repositoryProfileRequest.repositoryName))
                .thenCombineAsync(
                        asyncCacheApi.getOrElseUpdate(repositoryProfileRequest.username + repositoryProfileRequest.repositoryName + "/20issues",
                                () -> gitHubAPIInst.getRepository20Issue(repositoryProfileRequest.username + "/" + repositoryProfileRequest.repositoryName)),
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

    private void processRepositoryProfileResult(JsonNode repositoryProfileInfo) {
        //System.out.println("Received repository profile result: " + repositoryProfileInfo);
        sessionActor.tell(new Messages.RepositoryProfileInfo(repositoryProfileInfo), getSelf());
    }
}
