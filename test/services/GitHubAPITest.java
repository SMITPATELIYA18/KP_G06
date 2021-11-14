package services;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import services.github.GitHubAPI;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;

public class GitHubAPITest {
    private static Application testApp;
    private static GitHubAPI testGitHubAPI;

    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPI = testApp.injector().instanceOf(GitHubAPI.class);
    }

    @AfterClass
    public static void tearDown() {
        Helpers.stop(testApp);
    }

    @Test
    public void testGetUserProfileByUsername() {

    }
}
