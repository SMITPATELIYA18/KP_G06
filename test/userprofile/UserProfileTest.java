package userprofile;

import com.fasterxml.jackson.databind.JsonNode;
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

import services.GitHubAPIImpl;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import org.junit.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Holds tests related to the user profile feature
 * @author Pradnya Kandarkar
 */
public class UserProfileTest {

    private static Application testApp;
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
     * @throws IOException
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

    /**
     * Checks if a valid response is returned when <code>getUserProfileByUsername</code> is called for an existing
     * GitHub username
     * @throws Exception
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnProfileInfo_when_GitHubUser() throws Exception {
        routePattern = "/users/:username";
        testResourceName = "validGitHubUserProfile.json";
        JsonNode testUserProfile = testGitHubAPIImpl.getUserProfileByUsername("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("pradnya-git-dev", testUserProfile.get("login").textValue());
    }

    /**
     * Checks if a "Not Found" response is returned when <code>getUserProfileByUsername</code> is called for a username
     * which is not registered with GitHub
     * @throws Exception
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnNotFoundResponse_when_NotGitHubUser() throws Exception {
        routePattern = "/users/:username";
        testResourceName = "noGitHubUserProfile.json";
        JsonNode testUserProfile = testGitHubAPIImpl.getUserProfileByUsername("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testUserProfile.get("message").textValue());
    }

    /**
     * Checks if a valid non-empty JSON array node is returned when <code>getUserRepositories</code> is called for an existing
     * GitHub username having at least one public repository
     * @throws Exception
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnReposArray_when_GitHubUserWithPublicRepos() throws Exception {
        routePattern = "/users/:username/repos";
        testResourceName = "GitHubUserWithPublicRepos.json";
        JsonNode testUserRepos = testGitHubAPIImpl.getUserRepositories("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertTrue(testUserRepos.isArray());
        assertTrue(testUserRepos.size() > 0);
        assertEquals("testRepoForPlayProject", testUserRepos.get(0).textValue());
        assertEquals("testRepositoryForPlayProject2", testUserRepos.get(1).textValue());
    }

    /**
     * Checks if an empty JSON array node is returned when <code>getUserRepositories</code> is called for an existing
     * GitHub username having no public repositories
     * @throws Exception
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnEmptyArray_when_GitHubUserWithNoPublicRepos() throws Exception {
        routePattern = "/users/:username/repos";
        testResourceName = "GitHubUserWithNoPublicRepos.json";
        JsonNode testUserRepos = testGitHubAPIImpl.getUserRepositories("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertTrue(testUserRepos.isArray());
        assertTrue(testUserRepos.size() == 0);
        assertTrue(testUserRepos.isEmpty());
    }

    /**
     * Checks if a "Not Found" response is returned when <code>getUserRepositories</code> is called for a username
     * which is not registered with GitHub
     * @throws Exception
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_ReturnReposNotFoundResponse_when_NotGitHubUser() throws Exception {
        routePattern = "/users/:username/repos";
        testResourceName = "userRepositoriesNotGitHubUser.json";
        JsonNode testUserRepos = testGitHubAPIImpl.getUserRepositories("test_username").toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertFalse(testUserRepos.isArray());
        assertEquals("Not Found", testUserRepos.get("message").textValue());
    }
}


