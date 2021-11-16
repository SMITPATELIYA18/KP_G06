package userprofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.AssetsFinder;
import org.junit.Test;

import static org.junit.Assert.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;

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

import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Holds tests related to the user profile feature
 * @author Pradnya Kandarkar
 */
public class UserProfileTest {

    private static Application testApp;
    private static AssetsFinder assetsFinder;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern;                 /* For holding the route pattern. Chnages for every test depending on the test case. */
    private static String testResourceName;             /* For returning the resources */

    /**
     * Overrides the binding to use mock implementation instead of the actual implematation and creates a fake application.
     * Sets up an embedded server for testing.
     * @author Pradnya Kandarkar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        assetsFinder = testApp.injector().instanceOf(AssetsFinder.class);
        server =
                Server.forRouter(
                        (components) ->
                                RoutingDsl.fromComponents(components)
                                        .GET(routePattern)
                                        .routingTo((request, username) -> ok().sendResource(testResourceName))
                                        .build());
        wsClient = play.test.WSTestClient.newClient(server.httpPort());
        testGitHubAPIImpl = testApp.injector().instanceOf(GitHubAPIImpl.class);
        testGitHubAPIImpl.setBaseURL("");
        testGitHubAPIImpl.setClient(wsClient);
    }


    /**
     * Performs clean up activities after all tests are performed
     * @author Pradnya Kandarkar
     * @throws Exception If the call cannot be completed due to an error
     */
    @AfterClass
    public static void tearDown() throws IOException{
        try {
            wsClient.close();
        } finally {
            server.stop();
        }
        Helpers.stop(testApp);
    }

    // Tests for the responses returned by the controller that are related to the user profile feature

    /**
     * Checks if HTTP response OK (200) is received for a valid GET request
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String sampleUserProfileURL = "/user-profile/sample-username";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(sampleUserProfileURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

    /**
     * Checks if HTTP response NOT_FOUND (404) is received for a request type that is not implemented for the URL
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnNOT_FOUND_when_NotGETRequest() {
        String sampleUserProfileURL = "/user-profile/sample-username";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(sampleUserProfileURL);
        Result result = route(testApp, request);

        assertEquals(NOT_FOUND, result.status());
    }

    // Tests for the methods implemented for GitHubAPI that are related to the user profile feature

    /**
     * Checks if a valid response is returned when <code>getUserProfileByUsername</code> is called for an existing
     * GitHub username
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnProfileInfo_when_GitHubUser() throws Exception {
        routePattern = "/users/:username";
        testResourceName = "userprofile/validGitHubUserProfile.json";
        JsonNode testUserProfile = testGitHubAPIImpl.getUserProfileByUsername("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("pradnya-git-dev", testUserProfile.get("login").textValue());
    }

    /**
     * Checks if a "Not Found" response is returned when <code>getUserProfileByUsername</code> is called for a username
     * which is not registered with GitHub
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnNotFoundResponse_when_NotGitHubUser() throws Exception {
        routePattern = "/users/:username";
        testResourceName = "userprofile/noGitHubUserProfile.json";
        JsonNode testUserProfile = testGitHubAPIImpl.getUserProfileByUsername("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testUserProfile.get("message").textValue());
    }

    /**
     * Checks if a valid non-empty JSON array node is returned when <code>getUserRepositories</code> is called for an existing
     * GitHub username having at least one public repository
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnReposArray_when_GitHubUserWithPublicRepos() throws Exception {
        routePattern = "/users/:username/repos";
        testResourceName = "userprofile/GitHubUserWithPublicRepos.json";
        JsonNode testUserRepos = testGitHubAPIImpl.getUserRepositories("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertTrue(testUserRepos.isArray());
        assertTrue(testUserRepos.size() > 0);
        assertEquals("testRepoForPlayProject", testUserRepos.get(0).textValue());
        assertEquals("testRepositoryForPlayProject2", testUserRepos.get(1).textValue());
    }

    /**
     * Checks if an empty JSON array node is returned when <code>getUserRepositories</code> is called for an existing
     * GitHub username having no public repositories
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnEmptyArray_when_GitHubUserWithNoPublicRepos() throws Exception {
        routePattern = "/users/:username/repos";
        testResourceName = "userprofile/GitHubUserWithNoPublicRepos.json";
        JsonNode testUserRepos = testGitHubAPIImpl.getUserRepositories("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertTrue(testUserRepos.isArray());
        assertTrue(testUserRepos.size() == 0);
        assertTrue(testUserRepos.isEmpty());
    }

    /**
     * Checks if a "Not Found" response is returned when <code>getUserRepositories</code> is called for a username
     * which is not registered with GitHub
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnReposNotFoundResponse_when_NotGitHubUser() throws Exception {
        routePattern = "/users/:username/repos";
        testResourceName = "userprofile/userRepositoriesNotGitHubUser.json";
        JsonNode testUserRepos = testGitHubAPIImpl.getUserRepositories("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertFalse(testUserRepos.isArray());
        assertEquals("Not Found", testUserRepos.get("message").textValue());
    }

    // Tests for the view template(s) related to the user profile feature

    /**
     * Checks if available public repositories are displayed when the username represents a user registered with GitHub
     * and has public repositories associated with it
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_DisplayAvailableRepos_when_GitHubUserWithPublicRepos() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userProfile = mapper.readTree(new File("test/resources/userprofile/validGitHubUserProfile.json"));
        JsonNode userRepositories = mapper.readTree(new File("test/resources/userprofile/GitHubUserWithPublicRepos.json"));

        Content html = views.html.userprofile.userprofile.render("test_username", userProfile, userRepositories, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("2 repositories available for this user."));
    }

    /**
     * Checks if "No public repositories available for this user." is displayed when the username represents a user
     * registered with GitHub but has no public repositories associated with it
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_DisplayNoPublicRepos_when_GitHubUserWithNoPublicRepos() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userProfile = mapper.readTree(new File("test/resources/userprofile/validGitHubUserProfile.json"));
        JsonNode userRepositories = mapper.readTree(new File("test/resources/userprofile/GitHubUserWithNoPublicRepos.json"));

        Content html = views.html.userprofile.userprofile.render("test_username", userProfile, userRepositories, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No public repositories available for this user."));
    }

    /**
     * Checks if "No repositories available for this username." is displayed when the username does not represents a
     * user registered with GitHub
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_DisplayNoReposAvailable_when_NotGitHubUser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userProfile = mapper.readTree(new File("test/resources/userprofile/noGitHubUserProfile.json"));
        JsonNode userRepositories = mapper.readTree(new File("test/resources/userprofile/userRepositoriesNotGitHubUser.json"));

        Content html = views.html.userprofile.userprofile.render("test_username", userProfile, userRepositories, assetsFinder);

        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("Not Found"));
        assertTrue(contentAsString(html).contains("No repositories available for this username."));
    }
}


