package repositoryprofile;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
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
import static play.mvc.Results.ok;

/**
 * Contains cases to test GitHub API Implementation in Repository profile feature
 * @author Farheen Jamadar
 */
public class RepositoryProfileImplTest {
    private static Application testApp;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String route;
    private static String testResourceName;

    /**
     * Binds the interface to the mock implementation of GitHub API.
     * Creates embedded Play server using RoutingDsl.
     * @author Farheen Jamadar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPIImpl = testApp.injector().instanceOf(GitHubAPIImpl.class);
        testGitHubAPIImpl.setBaseURL("");

        server =
                Server.forRouter(
                        (components) ->
                                RoutingDsl.fromComponents(components)
                                .GET(route)
                                .routingTo((request, username, repositoryName) -> ok().sendResource(testResourceName))
                                .build());

        wsClient = play.test.WSTestClient.newClient(server.httpPort());
        testGitHubAPIImpl.setClient(wsClient);
    }

    /**
     * Cleans up after all the test cases are executed
     * @author Farheen Jamadar
     */
    @AfterClass
    public static void tearDown() throws IOException {
        try {
            wsClient.close();
        } finally {
            server.stop();
        }
        Helpers.stop(testApp);
    }

    /**
     * Checks if the server returns repository profile details provided a valid username and repository name
     * @throws Exception Thrown in case of any error
     * @author Farheen Jamadar
     */
    @Test
    public void should_ReturnRepositoryProfileDetails_provided_UserNameRepositoryName() throws Exception {
        route = "/repos/:username/:repositoryName";
        testResourceName = "repositoryprofile/validRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl
                                                .getRepositoryProfile("sampleUsername", "sampleRepository")
                                                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("greyli/helloflask", testRepositoryProfile.get("full_name").textValue());
    }

    /**
     * Checks if the server returns a not found response provided an invalid username
     * @throws Exception Thrown in case of any error
     * @author Farheen Jamadar
     */
    @Test
    public void should_ReturnNotFound_provided_invalidUserName() throws Exception {
        route = "/repos/:invalidUsername/:sampleRepository";
        testResourceName = "repositoryprofile/invalidRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl
                                                .getRepositoryProfile("invalidUsername", "sampleRepository")
                                                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testRepositoryProfile.get("message").textValue());
    }

    /**
     * Checks if the server returns a not found response provided an invalid repository name
     * @throws Exception Thrown in case of any error
     * @author Farheen Jamadar
     */
    @Test
    public void should_ReturnNotFound_provided_invalidRepositoryName() throws Exception {
        route = "/repos/:sampleUsername/:invalidRepositoryName";
        testResourceName = "repositoryprofile/invalidRepositoryProfileDetails.json";
        JsonNode testRepositoryProfile = testGitHubAPIImpl
                                            .getRepositoryProfile("sampleUsername", "invalidRepositoryName")
                                            .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals("Not Found", testRepositoryProfile.get("message").textValue());
    }
}
