package searchreposfeature;

import actors.Messages;
import actors.SearchActor;
import actors.SupervisorActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
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
import static org.junit.Assert.assertNotEquals;
import static play.inject.Bindings.bind;

/**
 * Contains cases to test SupervisorActor and SearchActor used in search functionality
 * @author Farheen Jamadar
 */

public class SearchFeatureActorTest {
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

        //Actor
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
     * Checks if the Supervisor handles:
     * 1. Different search query requests
     * 2. Tracking the search query request in case of any new update
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @throws InterruptedException Exception thrown by Thread class in case of any issue
     * @author Farheen Jamadar
     */
    @Test
    public void testSupervisorActorJsonNodeDifferentMessage() throws InterruptedException, IOException {
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode searchQuery = mapper.readTree(
                new File("test/resources/searchreposfeature/sampleSearchQuery.json"));

        JsonNode searchQuery2 = mapper.readTree(
                new File("test/resources/searchreposfeature/sampleSearchQuery2.json"));

        supervisorActor.tell(searchQuery, testProbe.getRef());
        supervisorActor.tell(searchQuery2, testProbe.getRef());
        supervisorActor.tell(searchQuery2, testProbe.getRef());
        Thread.sleep(10000);

        supervisorActor.tell(searchQuery2, testProbe.getRef());
        JsonNode jsonNode = testProbe.expectMsgClass(JsonNode.class);
        assertNotEquals(null, jsonNode.get("repositoryList").get(0).get("repositoryName").asText());
    }


    /**
     * Checks if the Supervisor handles repeated search query
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @author Farheen Jamadar
     */
    @Test
    public void testSupervisorActorJsonNodeSameMessage() throws IOException {

        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode searchQuery = mapper.readTree(
                new File("test/resources/searchreposfeature/sampleSearchQuery.json"));

        supervisorActor.tell(searchQuery, testProbe.getRef());
        supervisorActor.tell(searchQuery, testProbe.getRef());
        JsonNode jsonNode = testProbe.expectMsgClass(JsonNode.class);
        assertNotEquals(null, jsonNode.get("repositoryList").get(0).get("repositoryName").asText());
    }

    /**
     * Checks if the Supervisor handles arbitrary query request
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @author Farheen Jamadar
     */
    @Test
    public void testSupervisorActorReceivesRandomQuery() throws IOException {

        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode arbitrarySearchQuery = mapper.readTree(
                new File("test/resources/searchreposfeature/arbitrarySearchQuery.json"));

        final String unknownMessageStatus = new EventFilter(1) {
            @Override
            public boolean matches(Logging.LogEvent event) {
                return true;
            }
        }.intercept(() -> {
            supervisorActor.tell(arbitrarySearchQuery, testProbe.getRef());
            return "Unknown Message type intercepted";
        }, actorSystem);

        assertEquals("Unknown Message type intercepted", unknownMessageStatus);
    }

    /**
     * Checks if the Search Actor handles arbitrary query request
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @author Farheen Jamadar
     */
    @Test
    public void testSearchActorReceivesRandomQuery() throws IOException {
        final ActorRef searchActor = actorSystem.actorOf(
                SearchActor.props(testProbe.getRef(), "sampleQuery", testGitHubAPI));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode searchUnknownMessageType = mapper.readTree(
                new File("test/resources/searchreposfeature/sampleSearchQuery.json"));

        final String unknownMessageStatus = new EventFilter(1) {
            @Override
            public boolean matches(Logging.LogEvent event) {
                //System.out.println("Printing log event: " + event.message().toString());
                return true;
            }
        }.intercept(() -> {
            searchActor.tell(searchUnknownMessageType, testProbe.getRef());
            return "Unknown Message type intercepted";
        }, actorSystem);

        assertEquals("Unknown Message type intercepted", unknownMessageStatus);
    }

    /**
     * Checks if the Messages class instance is created
     * @author Farheen Jamadar
     */
    @Test
    public void testMessageClass(){
        assertNotEquals(null, new Messages());
    }

    /**
     * Tests GitHubMock Search Function
     * @throws IOException Exception thrown by Mapper class in case of any issue while reading the file
     * @author Farheen Jamadar
     */
    @Test
    public void testGitMockSearchFunction() throws IOException {
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode searchQuery = mapper.readTree(
                new File("test/resources/searchreposfeature/sampleSearchQuery3.json"));

        supervisorActor.tell(searchQuery, testProbe.getRef());
        JsonNode jsonNode = testProbe.expectMsgClass(JsonNode.class);
        assertNotEquals(null, jsonNode.get("repositoryList").get(0).get("repositoryName").asText());
    }

    /**
     * Tests whether supervisor handles failures as expected
     * @author Pradnya Kandarkar
     */
    @Test
    public void testSupervisorStrategy() {
        final Props props = Props.create(SupervisorActor.class, testProbe.getRef(), testGitHubAPI, testAsyncCacheApi);
        final TestActorRef<SupervisorActor> supervisorActor = TestActorRef.create(actorSystem, props);

        SupervisorStrategy.Directive directive = supervisorActor.underlyingActor()
                .supervisorStrategy().decider().apply(new Exception());
        assertEquals(SupervisorStrategy.restart(), directive);

        SupervisorStrategy.Directive directive2 = supervisorActor.underlyingActor()
                .supervisorStrategy().decider().apply(new Throwable());
        assertEquals(SupervisorStrategy.escalate(), directive2);
    }
}
