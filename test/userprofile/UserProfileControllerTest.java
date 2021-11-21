package userprofile;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.route;

/**
 * Holds tests for the application controller that are related to the user profile feature
 * @author Pradnya Kandarkar
 */
public class UserProfileControllerTest {
    private static Application testApp;

    /**
     * Creates an application for testing purpose and overrides the binding to use mock implementation instead of the
     * actual implementation
     * @author Pradnya Kandarkar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
    }

    /**
     * Performs clean up activities after all tests are performed
     * @author Pradnya Kandarkar
     */
    @AfterClass
    public static void tearDown(){
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
}
