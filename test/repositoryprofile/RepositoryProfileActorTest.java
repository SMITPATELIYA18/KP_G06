package repositoryprofile;

import actors.RepositoryProfileActor;
import actors.SupervisorActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
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
import static play.inject.Bindings.bind;

/**
 * Contains cases to test RepositoryProfileActor used in repository profile functionality
 * @author Farheen Jamadar
 */
public class RepositoryProfileActorTest {
    private static ActorSystem actorSystem;
    private static TestKit testProbe;
    private static Application testApp;
    private static GitHubAPI testGitHubAPI;
    private static AsyncCacheApi testAsyncCacheApi;

    /**
     * Binds the interface to the mock implementation of GitHub API.
     * Initialization of Testkit
     * @author Farheen Jamadar
     */
    @BeforeClass
    public static void setup() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPI = testApp.injector().instanceOf(GitHubAPI.class);
        testAsyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);

        actorSystem = ActorSystem.create();
        testProbe = new TestKit(actorSystem);
    }

    /**
     * Cleans up after all the test cases are executed
     * @author Farheen Jamadar
     */
    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
        Helpers.stop(testApp);
    }

    /**
     * Checks if the Repository Profile Actor handles repository query request
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @author Farheen Jamadar
     */
    @Test
    public void testRepositoryProfileActorRepositoryProfileQuery() throws IOException {
        //Supervisor actor required as it is the entrypoint
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileQuery = mapper.readTree(
                new File("test/resources/repositoryprofile/sampleRepositoryProfileQuery.json"));

        supervisorActor.tell(repositoryProfileQuery, testProbe.getRef());
        supervisorActor.tell(repositoryProfileQuery, testProbe.getRef());
        JsonNode repositoryProfileInfo = testProbe.expectMsgClass(JsonNode.class);
        assertEquals("helloflask", repositoryProfileInfo.get("repositoryProfile").get("name").asText());
    }

    /**
     * Checks if the Repository Profile Actor handles unknown query request
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @author Farheen Jamadar
     */
    @Test
    public void testRepositoryProfileActorReceivesRandomQuery() throws IOException {
        final ActorRef repositoryProfileActor = actorSystem.actorOf(
                RepositoryProfileActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode arbitrarySearchQuery = mapper.readTree(
                new File("test/resources/repositoryprofile/arbitraryRepositoryQuery.json"));

        final String unknownMessageStatus = new EventFilter(1) {
            @Override
            public boolean matches(Logging.LogEvent event) {
                return true;
            }
        }.intercept(() -> {
            repositoryProfileActor.tell(arbitrarySearchQuery, testProbe.getRef());
            return "Unknown Message type intercepted";
        }, actorSystem);

        assertEquals("Unknown Message type intercepted", unknownMessageStatus);
    }

}
