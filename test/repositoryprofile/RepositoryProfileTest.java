package repositoryprofile;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.Helpers;
import services.GitHubAPIImpl;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;

public class RepositoryProfileTest {
    private static Application testApp;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern;
    private static String testResourceName;

    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        server =
                Server.forRouter(
                        (components) ->
                                RoutingDsl.fromComponents(components)
                                        .GET(routePattern)
                                        .routingTo((request, username, repositoryName) -> ok().sendResource(testResourceName))
                                        .build());
        wsClient = play.test.WSTestClient.newClient(server.httpPort());

        testGitHubAPIImpl = testApp.injector().instanceOf(GitHubAPIImpl.class);
        testGitHubAPIImpl.setBaseURL("");
        testGitHubAPIImpl.setClient(wsClient);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        try {
            wsClient.close();
        } finally {
            server.stop();
        }
        Helpers.stop(testApp);
    }

    /*Controller*/
    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String sampleRepositoryProfileURL = "/repositoryProfile/sampleUsername/sampleRepository";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(sampleRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

    @Test
    public void should_ReturnNOT_FOUND_when_NotGETRequest() {
        String sampleRepositoryProfileURL = "/repositoryProfile/sampleUsername/sampleRepository";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(sampleRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(NOT_FOUND, result.status());
    }

    /*Actual Method*/
    @Test
    public void should_ReturnRepositoryProfileDetails_when_GitHubUser() throws Exception {
        routePattern = "/repos/:username/:repositoryName";
        testResourceName = "repositoryprofile/validRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                                                    .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("greyli/helloflask", testRepositoryProfile.get("full_name").textValue());
    }

    @Test
    public void shouldNot_ReturnRepositoryProfileDetails_when_GitHubUser() throws Exception {
        routePattern = "/repos/:username/:repositoryName";
        testResourceName = "repositoryprofile/invalidRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testRepositoryProfile.get("message").textValue());
    }

}
