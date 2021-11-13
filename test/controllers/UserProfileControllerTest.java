package controllers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import play.mvc.Result;
import play.twirl.api.Content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static play.inject.Bindings.bind;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import static play.test.Helpers.contentAsString;

import resources.TestResources;
import services.GitHubAPIMock;
import play.mvc.*;
import services.github.GitHubAPI;

import org.apache.http.HttpStatus;
import org.junit.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @AfterClass
    public static void tearDown() {
        Helpers.stop(testApp);
    }

    @Test
    public void testGetUserProfile() {

    }
}
