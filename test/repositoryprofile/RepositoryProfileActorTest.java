package repositoryprofile;

import actors.RepositoryProfileActor;
import actors.RepositoryProfileActor.SendParameters;
import actors.UserActor;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.IssueModel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.WebSocket;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.Helpers;
import services.GitHubAPIImpl;
import services.GitHubAPIMock;
import services.github.GitHubAPI;


import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Results.ok;

public class RepositoryProfileActorTest {
    private static ActorSystem actorSystem;
    private static TestKit testProbe;

    //Old
    private static Application testApp;
    private static GitHubAPI testGitHubAPI;

    @BeforeClass
    public static void setup() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPI = testApp.injector().instanceOf(GitHubAPI.class);

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

    @Test
    public void testUserActorCreation(){
        final ActorRef userActor = actorSystem.actorOf(
                UserActor.props(testProbe.getRef(), "sampleUsername", "sampleRepositoryName"), "userActor1");
        assertThat(userActor).isNotNull();
        //TODO: CHange this assert statement to something better
    }

    @Test
    public void testUserActorGetParametersMessage(){
        final ActorRef userActor = actorSystem.actorOf(
                           UserActor.props(testProbe.getRef(), "sampleUsername", "sampleRepositoryName"));

        userActor.tell(new UserActor.GetParameters(), testProbe.getRef());
        SendParameters sendParameters = testProbe.expectMsgClass(SendParameters.class);

        assertEquals("sampleUsername", sendParameters.getUsername());
        assertEquals("sampleRepositoryName", sendParameters.getRepositoryName());
    }

    @Test
    public void testUserActorDisplayMessage(){
        final ActorRef userActor = actorSystem.actorOf(UserActor.props(testProbe.getRef(), "sampleUserName", "sampleRepository"));

        ObjectMapper mapper = new ObjectMapper();
        CompletionStage<JsonNode> repositoryData = CompletableFuture.supplyAsync(() -> {
                ObjectNode data = mapper.createObjectNode();
                ArrayNode arrayNode = mapper.createArrayNode();

                data.set("repositoryProfile", mapper.createObjectNode());
                data.set("issueList", arrayNode);
                return data;
        });

        userActor.tell(new UserActor.DisplayRepositoryDetails(repositoryData), testProbe.getRef());
        //userActor.tell(new UserActor.DisplayRepositoryDetails(repositoryProfileDetails, issueList), testProbe.getRef());
        //TODO assertion and lambda checks
    }

    @Test
    public void testRepositoryActorCreation(){
        final ActorRef testRepositoryActor = actorSystem.actorOf(RepositoryProfileActor.getProps(testGitHubAPI),
                "repositoryProfileActor");
        assertThat(testRepositoryActor).isNotNull();

    }

    @Test
    public void testRepositoryProfileActorDisplayMessage() throws ExecutionException, InterruptedException {
        final ActorRef testRepositoryActor = actorSystem.actorOf(RepositoryProfileActor.getProps(testGitHubAPI));
        SendParameters sendParameters = new SendParameters("sampleUserName", "sampleRepository");
        testRepositoryActor.tell(sendParameters, testProbe.getRef());
        CompletableFuture testRepositoryProfile = testProbe.expectMsgClass(CompletableFuture.class);
        assertThat(testRepositoryProfile.toCompletableFuture().get().toString().contains("greyli/helloflask"));
    }

}
