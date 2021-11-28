package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.cache.AsyncCacheApi;
import scala.concurrent.duration.Duration;
import services.github.GitHubAPI;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class UserProfileActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef sessionActor;
    private GitHubAPI gitHubAPIInst;
    private AsyncCacheApi asyncCacheApi;

    public UserProfileActor(ActorRef sessionActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        this.sessionActor = sessionActor;
        this.gitHubAPIInst = gitHubAPIInst;
        this.asyncCacheApi = asyncCacheApi;
    }

    public static Props props(ActorRef sessionActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        return Props.create(UserProfileActor.class, sessionActor, gitHubAPIInst, asyncCacheApi);
    }

    @Override
    public void preStart() {
        System.out.println("Created a user profile actor.");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.GetUserProfile.class, userProfileRequest -> {
                    onGetUserProfile(userProfileRequest).thenAcceptAsync(this::processUserProfileResult);
                })
                .build();
    }

    private CompletionStage<JsonNode> onGetUserProfile(Messages.GetUserProfile userProfileRequest) throws Exception {
        return asyncCacheApi.getOrElseUpdate(userProfileRequest.username + "_profile",
                        () -> gitHubAPIInst.getUserProfileByUsername(userProfileRequest.username))
                .thenCombineAsync(asyncCacheApi.getOrElseUpdate(userProfileRequest.username + "_repositories",
                                () -> gitHubAPIInst.getUserRepositories(userProfileRequest.username)),
                        (userProfile, userRepositories) -> {
                            asyncCacheApi.set(userProfileRequest.username + "_profile", userProfile);
                            asyncCacheApi.set(userProfileRequest.username + "_repositories", userRepositories);
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode userInfo = mapper.createObjectNode();
                            userInfo.put("responseType", "userProfileInfo");
                            userInfo.set("profile", userProfile);
                            userInfo.set("repositories", userRepositories);
                            return userInfo;
                        }
                );
    }

    private void processUserProfileResult(JsonNode userProfileInfo) {
        System.out.println("Received user profile result: " + userProfileInfo);
        sessionActor.tell(new Messages.UserProfileInfo(userProfileInfo), getSelf());
    }
}
