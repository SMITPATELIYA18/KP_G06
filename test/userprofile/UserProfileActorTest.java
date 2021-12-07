package userprofile;

import actors.Messages;
import actors.SupervisorActor;
import actors.UserProfileActor;
import akka.actor.*;
import akka.event.Logging;
import akka.testkit.EventFilter;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;

/**
 * Holds tests for the user profile actor
 * @author Pradnya Kandarkar
 */
public class UserProfileActorTest {

    private static ActorSystem actorSystem;
    private static TestKit testProbe;

    private static Application testApp;
    private static GitHubAPI testGitHubAPIInst;
    private static AsyncCacheApi testAsyncCacheApi;

    /**
     * Creates an application for testing purpose and overrides the binding to use mock implementation instead of the
     * actual implementation. Inject instances of <code>GitHubAPI</code> and <code>AsyncCacheApi</code> in the
     * application for testing purpose. Creates actor system and test kit.
     * @author Pradnya Kandarkar
     */
    @BeforeClass
    public static void setup() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPIInst = testApp.injector().instanceOf(GitHubAPI.class);
        testAsyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);
        actorSystem = ActorSystem.create();
        testProbe = new TestKit(actorSystem);
    }

    /**
     * Performs clean up activities after all tests are performed
     * @author Pradnya Kandarkar
     */
    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
        Helpers.stop(testApp);
    }

    /**
     * Checks whether user profile information is returned from a <code>UserProfileActor</code> upon receiving a request
     * to get user profile and repositories information
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnUserProfileInfo_when_GetUserInfoRequest() {
        final ActorRef userProfileActor = actorSystem.actorOf(
                UserProfileActor.props(testProbe.getRef(), testGitHubAPIInst, testAsyncCacheApi));

        userProfileActor.tell(new Messages.GetUserProfile("test_username"), testProbe.getRef());

        Messages.UserProfileInfo userProfileInfo = testProbe.expectMsgClass(Messages.UserProfileInfo.class);
        assertEquals("userProfileInfo", userProfileInfo.userProfileResult.get("responseType").asText());
        assertTrue(userProfileInfo.userProfileResult.has("profile"));
        assertTrue(userProfileInfo.userProfileResult.has("repositories"));

        actorSystem.stop(userProfileActor);
    }

    /**
     * Checks if a log message is generated from a <code>UserProfileActor</code> upon receiving an unknown message type
     * @throws IOException If the call cannot be completed due to an IO error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_Log_when_UnknownMessageType() throws IOException {
        final ActorRef userProfileActor = actorSystem.actorOf(
                UserProfileActor.props(testProbe.getRef(), testGitHubAPIInst, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode testUnknownMessageType = mapper.readTree(new File("test/resources/userprofile/sampleUserProfileRequest.json"));

        final String unknownMessageStatus = new EventFilter(1) {
            @Override
            public boolean matches(Logging.LogEvent event) {
                if(event.message().toString().equals("Received unknown message type: class com.fasterxml.jackson.databind.node.ObjectNode")) {
                    return true;
                }
                return false;
            }
        }.intercept(() -> {
            userProfileActor.tell(testUnknownMessageType, testProbe.getRef());
            return "Unknown message type intercepted";
        }, actorSystem);

        assertEquals("Unknown message type intercepted", unknownMessageStatus);

        actorSystem.stop(userProfileActor);
    }

    /**
     * Checks whether JSON response containing user profile information is returned upon receiving a request for user
     * profile from client-side
     * @throws IOException If the call cannot be completed due to an IO error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnValidJSONResponse_when_UserProfileQuery() throws IOException {
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPIInst, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode userProfileQuery1 = mapper.readTree(new File("test/resources/userprofile/sampleUserProfileRequest.json"));
        JsonNode userProfileQuery2 = mapper.readTree(new File("test/resources/userprofile/sampleUserProfileRequest.json"));

        supervisorActor.tell(userProfileQuery1, testProbe.getRef());
        JsonNode userProfileInfo1 = testProbe.expectMsgClass(JsonNode.class);
        assertEquals("userProfileInfo", userProfileInfo1.get("responseType").asText());
        assertTrue(userProfileInfo1.has("profile"));
        assertTrue(userProfileInfo1.has("repositories"));

        supervisorActor.tell(userProfileQuery2, testProbe.getRef());
        JsonNode userProfileInfo2 = testProbe.expectMsgClass(JsonNode.class);
        assertEquals("userProfileInfo", userProfileInfo2.get("responseType").asText());
        assertTrue(userProfileInfo2.has("profile"));
        assertTrue(userProfileInfo2.has("repositories"));

        actorSystem.stop(supervisorActor);
    }
}
