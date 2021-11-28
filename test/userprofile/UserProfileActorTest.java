package userprofile;

import actors.Messages;
import actors.UserProfileActor;
import akka.actor.*;
import akka.event.Logging;
import akka.testkit.EventFilter;
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
        System.out.println("Received JSON response: " + userProfileInfo.userProfileResult);
        assertEquals("userProfileInfo", userProfileInfo.userProfileResult.get("responseType").asText());
        assertTrue(userProfileInfo.userProfileResult.has("profile"));
        assertTrue(userProfileInfo.userProfileResult.has("repositories"));
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
                System.out.println("Printing log event: " + event.message().toString());
                return true;
            }
        }.intercept(() -> {
            userProfileActor.tell(testUnknownMessageType, testProbe.getRef());
            return "Unknown Message type intercepted";
        }, actorSystem);

        assertEquals("Unknown Message type intercepted", unknownMessageStatus);
    }
}
