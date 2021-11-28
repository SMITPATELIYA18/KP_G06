package repositoryprofile;

import actors.Messages;
import actors.RepositoryProfileActor;
import actors.SupervisorActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.test.Helpers;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;

public class RepositoryProfileActorTest {
    private static ActorSystem actorSystem;
    private static TestKit testProbe;

    //Old
    private static Application testApp;
    private static GitHubAPI testGitHubAPI;
    private static AsyncCacheApi testAsyncCacheApi;

    @BeforeClass
    public static void setup() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPI = testApp.injector().instanceOf(GitHubAPI.class);
        testAsyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);

        //Actor
        actorSystem = ActorSystem.create();
        testProbe = new TestKit(actorSystem);
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
        Helpers.stop(testApp);
    }

    //Main test cases
    @Test
    public void testSupervisorActorJsonNodeMessage() throws IOException {
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode searchQuery = mapper.readTree(
                new File("test/resources/searchreposfeature/sampleSearchQuery.json"));

        supervisorActor.tell(searchQuery, testProbe.getRef());
        JsonNode jsonNode = testProbe.expectMsgClass(JsonNode.class);
        assertEquals("github", jsonNode.get("repositoryList").get(0).get("repositoryName").asText());
    }


    @Test
    public void testSupervisorActorReceivesRandomQuery() throws IOException {
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileQuery = mapper.readTree(
                new File("test/resources/searchreposfeature/arbitrarySearchQuery.json"));

        supervisorActor.tell(repositoryProfileQuery, testProbe.getRef());
        //TODO: Add Assertion after code addition
        //JsonNode repositoryProfileInfo = testProbe.expectMsgClass(JsonNode.class);
        //assertEquals("helloflask", repositoryProfileInfo.get("repositoryProfile").get("name").asText());
    }



    //Repository Profile

    @Test
    public void testSupervisorActorRepositoryProfileQuery() throws IOException {
        final ActorRef supervisorActor = actorSystem.actorOf(
                SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileQuery = mapper.readTree(
                new File("test/resources/repositoryprofile/sampleRepositoryProfileQuery.json"));

        supervisorActor.tell(repositoryProfileQuery, testProbe.getRef());
        JsonNode repositoryProfileInfo = testProbe.expectMsgClass(JsonNode.class);
        assertEquals("helloflask", repositoryProfileInfo.get("repositoryProfile").get("name").asText());
    }

}
