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
import services.github.GitHubAPI;

import java.util.concurrent.CompletionStage;

/**
 * Handles all user profile feature related requests - about retrieving user profile and repositories information
 * @author Pradnya Kandarkar
 */
public class UserProfileActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private ActorRef supervisorActor;
    private GitHubAPI gitHubAPIInst;
    private AsyncCacheApi asyncCacheApi;

    /**
     * @param supervisorActor Actor reference for the supervisor actor
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface, used to make external API calls to GitHub
     * @param asyncCacheApi For temporary data storage
     */
    public UserProfileActor(ActorRef supervisorActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        this.supervisorActor = supervisorActor;
        this.gitHubAPIInst = gitHubAPIInst;
        this.asyncCacheApi = asyncCacheApi;
    }

    /**
     * Creates an actor with properties specified using parameters
     * @param supervisorActor Actor reference for the supervisor actor
     * @param gitHubAPIInst Instance of <code>GitHubAPI</code> inteface, used to make external API calls to GitHub
     * @param asyncCacheApi For temporary data storage
     * @return A <code>Props</code> object holding actor configuration
     * @author Pradnya Kandarkar
     */
    public static Props props(ActorRef supervisorActor, GitHubAPI gitHubAPIInst, AsyncCacheApi asyncCacheApi) {
        return Props.create(UserProfileActor.class, supervisorActor, gitHubAPIInst, asyncCacheApi);
    }

    /**
     * Executes before any other action related to this actor
     * @author Pradnya Kandarkar
     */
    @Override
    public void preStart() {
        System.out.println("Created a user profile actor.");
    }

    /**
     * Handles incoming messages for this actor - matches the class of an incoming message and takes appropriate action
     * @return <code>AbstractActor.Receive</code> defining the messages that can be processed by this actor and how they will be processed
     * @author Pradnya Kandarkar
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.GetUserProfile.class, userProfileRequest -> {
                    onGetUserProfile(userProfileRequest).thenAcceptAsync(this::processUserProfileResult);
                })
//                .matchAny(other -> getSender().tell(new Messages.UnknownMessageReceived(), getSelf()))
                .matchAny(other -> log.error("Received unknown message type: " + other.getClass()))
                .build();
    }

    /**
     * Retrieves all available public profile information about a user, as well as all the repositories of that user
     * @param userProfileRequest <code>Messages.GetUserProfile</code> object containing the username for the request
     * @return <code>CompletionStage&lt;JsonNode&gt;</code> which contains available public profile information and repositories for a user
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
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

    /**
     * Sends the user profile information to be forwarded to the client
     * @param userProfileInfo <code>JsonNode</code> containing retrieved user profile information
     * @author Pradnya Kandarkar
     */
    private void processUserProfileResult(JsonNode userProfileInfo) {
        supervisorActor.tell(new Messages.UserProfileInfo(userProfileInfo), getSelf());
    }
}
