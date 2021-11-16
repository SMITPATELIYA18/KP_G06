package topics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import play.twirl.api.Content;
import services.GitHubAPIImpl;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;


import controllers.AssetsFinder;

public class TopicsTest {
    private static AssetsFinder assetsFinder;
    private static Application testApp;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern;
    private static String testResourceName;

    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();

        assetsFinder = testApp.injector().instanceOf(AssetsFinder.class);

        server =
                Server.forRouter(
                        (components) ->
                                RoutingDsl.fromComponents(components)
                                        .GET(routePattern)
                                        .routingTo((request, topic) -> ok().sendResource(testResourceName))
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
        String sampleTopicListURL = "/topics/python";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(sampleTopicListURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

    /*@Test
    public void should_ReturnNOT_FOUND_when_NotGETRequest() {
        String sampleRepositoryProfileURL = "/topics/python";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(sampleRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(NOT_FOUND, result.status());
    }*/

    /*Actual Method*/
   /* @Test
    public void should_ReturnRepositoryProfileDetails_provided_UserNameRepositoryName() throws Exception {
        routePattern = "/search/repositories?q=topic:Python&sort=created&order=desc";
        testResourceName = "repositoryprofile/validRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("greyli/helloflask", testRepositoryProfile.get("full_name").textValue());
    }

    @Test
    public void shouldNot_ReturnRepositoryProfileDetails_provided_invalidUserNameRepositoryName() throws Exception {
        routePattern = "/repos/:username/:repositoryName";
        testResourceName = "repositoryprofile/invalidRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testRepositoryProfile.get("message").textValue());
    }


    //UI
    @Test
    public void should_DisplayRepositoryProfileDetails_provided_UserRepositoryNameList() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/validRepositoryProfileDetails.json"));

        File fileObject = new File("test/resources/repositoryprofile/validIssueListDetails.txt");
        Scanner readObject = new Scanner(fileObject);
        List<String> list = List.of(readObject.nextLine().split(","));
        readObject.close();

        String username = "greyli";
        String repositoryName = "helloflask";

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, Optional.ofNullable(list).orElse(new ArrayList<String>()), assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("List to top 5 issues:"));
    }

    @Test
    public void should_DisplayIssueMessage_provided_InvalidUserRepositoryName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));

        List<String> list = null;

        String username = "sampleusername";
        String repositoryName = "samplerepositoryname";

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, Optional.ofNullable(list).orElse(new ArrayList<String>()), assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("Not Found"));
    }

    @Test
    public void should_DisplayDetails_with_IssueMessage_provided_UserRepositoryName_noList() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));

        List<String> list = null;

        String username = "sampleusername";
        String repositoryName = "samplerepositoryname";

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, Optional.ofNullable(list).orElse(new ArrayList<String>()), assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Issues Reported."));
    }*/
}
