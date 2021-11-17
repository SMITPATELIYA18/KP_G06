package repositoryprofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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


import views.html.repositoryprofile.*;

public class RepositoryProfileTest {
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

    //Controller
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

    //implementation
    @Test
    public void should_ReturnRepositoryProfileDetails_provided_UserNameRepositoryName() throws Exception {
        routePattern = "/repos/:username/:repositoryName";
        testResourceName = "repositoryprofile/validRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                                                    .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("greyli/helloflask", testRepositoryProfile.get("full_name").textValue());
    }

    @Test
    public void should_ReturnNotFound_provided_invalidUserName() throws Exception {
        routePattern = "/repos/:invalidUsername/:repositoryName";
        testResourceName = "repositoryprofile/invalidRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testRepositoryProfile.get("message").textValue());
    }

    @Test
    public void should_ReturnNotFound_provided_invalidRepositoryName() throws Exception {
        routePattern = "/repos/:username/:invalid";
        testResourceName = "repositoryprofile/invalidRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl.getRepositoryProfile("sampleUsername", "sampleRepository")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testRepositoryProfile.get("message").textValue());
    }

    //UI
    @Test
    public void should_DisplayRepositoryProfileDetails_provided_UserRepositoryIssueList() throws Exception {

        String username = "sampleUsername";
        String repositoryName = "sampleRepositoryName";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/validRepositoryProfileDetails.json"));
        ArrayNode issueList = (ArrayNode) mapper.readTree(new File("test/resources/repositoryprofile/validIssueListDetails.txt"));
        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issueList, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("List to top 5 issues:"));
    }

   /* @Test
    public void should_DisplayRepositoryProfileDetails_provided_SameUsernameAndLoginName() throws Exception {
        File fileObject = new File("test/resources/repositoryprofile/validIssueListDetails.txt");
        Scanner readObject = new Scanner(fileObject);
        List<String> list = List.of(readObject.nextLine().split(","));
        readObject.close();

        String username = "greyli";
        String repositoryName = "sampleRepositoryName";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/sample.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, list, assetsFinder);

        assertEquals("text/html", html.contentType());
        System.out.println(html);
        assertTrue(contentAsString(html).contains(""));
    }

    @Test
    public void should_DisplayNotFound_provided_InvalidUsername() throws Exception {
        File fileObject = new File("test/resources/repositoryprofile/validIssueListDetails.txt");
        Scanner readObject = new Scanner(fileObject);
        List<String> list = List.of(readObject.nextLine().split(","));
        readObject.close();

        String username = "invalidUsername";
        String repositoryName = "sampleRepositoryName";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, list, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("Not Found"));
    }

    @Test
    public void should_DisplayNotFound_provided_InvalidRepository() throws Exception {
        File fileObject = new File("test/resources/repositoryprofile/validIssueListDetails.txt");
        Scanner readObject = new Scanner(fileObject);
        List<String> list = List.of(readObject.nextLine().split(","));
        readObject.close();

        String username = "sampleUsername";
        String repositoryName = "invalidRepositoryName";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, list, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("Not Found"));
    }

    @Test
    public void should_DisplayIssueListNotFound_provided_NullList() throws Exception {
        List<String> list = null;

        String username = "sampleUsername";
        String repositoryName = "sampleRepositoryName";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(new File("test/resources/repositoryprofile/validRepositoryProfileDetails.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, list, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Issues Reported."));
    }*/
}
