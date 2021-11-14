package controllers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import services.GitHubAPIMock;
import services.github.GitHubAPI;

import org.junit.*;

/**
 * Tests for the UserProfileController class in controllers package
 * @author Pradnya Kandarkar
 */
public class UserProfileControllerTest {

    private static Application testApp;
    private static UserProfileController userProfileController;

    /**
     * Overrides the binding to use mock implementation instead of the actual implematation and creates a fake application
     * @author Pradnya Kandarkar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        userProfileController = testApp.injector().instanceOf(UserProfileController.class);
    }


    /**
     * Cleans up after running test cases
     * @author Pradnya Kandarkar
     */
    @AfterClass
    public static void tearDown() {
        Helpers.stop(testApp);
    }

    /**
     * Validates if HTTP response OK (200) is received for a valid GET request
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
     * Validates if HTTP response NOT_FOUND (404) is received for a request type that is not implemented for the URL
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
