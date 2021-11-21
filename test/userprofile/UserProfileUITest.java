package userprofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.AssetsFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import play.twirl.api.Content;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;
import static play.test.Helpers.contentAsString;

/**
 * Holds tests for the view template(s) related to the user profile feature
 * @author Pradnya Kandarkar
 */
public class UserProfileUITest {
    private static Application testApp;
    private static AssetsFinder assetsFinder;

    /**
     * Creates an application for testing purpose and overrides the binding to use mock implementation instead of the
     * actual implementation. Injects an assetFinder in the application for testing purpose.
     * @author Pradnya Kandarkar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        assetsFinder = testApp.injector().instanceOf(AssetsFinder.class);
    }

    /**
     * Performs clean up activities after all tests are performed
     * @author Pradnya Kandarkar
     */
    @AfterClass
    public static void tearDown() {
        Helpers.stop(testApp);
    }

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
