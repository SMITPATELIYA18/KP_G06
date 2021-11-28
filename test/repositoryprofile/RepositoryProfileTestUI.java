package repositoryprofile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import views.html.repositoryprofile.repositoryProfile;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

/**
 * Contains cases to test the view in Repository profile feature
 * @author Farheen Jamadar
 */
public class RepositoryProfileTestUI {
    private static AssetsFinder assetsFinder;
    private static Application testApp;

    /**
     * Binds the interface to the mock implementation of GitHub API
     * @author Farheen Jamadar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        assetsFinder = testApp.injector().instanceOf(AssetsFinder.class);
    }

    /**
     * Cleans up after all the test cases are executed
     * @author Farheen Jamadar
     */
    @AfterClass
    public static void tearDown() {
        Helpers.stop(testApp);
    }

    /**
     * Checks if the view displays repository profile details provided a valid username,
     * repository name and issues list
     * @author Farheen Jamadar
     */
   /* @Test
    public void should_DisplayRepositoryProfileDetails_provided_UserRepositoryIssuesList() throws Exception {
        String username = "sampleUsername";
        String repositoryName = "sampleRepositoryName";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(
                                new File("test/resources/repositoryprofile/validRepositoryProfileDetails.json"));
        ArrayNode issuesList = (ArrayNode) mapper.readTree(
                                new File("test/resources/repositoryprofile/validIssueListDetails.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issuesList, assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("List to top 5 issues:"));
    }

    *//**
     * Checks if the view displays not found message provided an invalid username
     * and valid repository name and issues list
     * @author Farheen Jamadar
     *//*
    @Test
    public void should_DisplayNotFound_provided_InvalidUsername() throws Exception {
        String username = "invalidUsername";
        String repositoryName = "sampleRepositoryName";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(
                                      new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));
        ArrayNode issuesList = (ArrayNode) mapper.readTree(
                                                new File("test/resources/repositoryprofile/validIssueListDetails.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issuesList, assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("Not Found"));
    }

    *//**
     * Checks if the view displays not found response provided an invalid repository name
     * and valid username and issues list
     * @author Farheen Jamadar
     *//*
    @Test
    public void should_DisplayNotFound_provided_InvalidRepository() throws Exception {
        String username = "sampleUsername";
        String repositoryName = "invalidRepositoryName";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(
                                    new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));
        ArrayNode issuesList = (ArrayNode) mapper.readTree(
                                    new File("test/resources/repositoryprofile/validIssueListDetails.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issuesList, assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("Not Found"));
    }

    *//**
     * Checks if the view displays no issues found response provided invalid username and valid repository name.
     * Response received from Repository issues server is "Issue does not Present!"
     * @author Farheen Jamadar
     *//*
    @Test
    public void should_returnNoIssuesFoundAlongWithProfileDetails_when_NoIssuesFound_1() throws Exception {
        String username = "invalidUsername";
        String repositoryName = "sampleRepositoryName";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(
                                    new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));
        ArrayNode issuesList = (ArrayNode) mapper.readTree(
                                    new File("test/resources/repositoryprofile/noIssueListResponse1.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issuesList, assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Issues Found"));
    }

    *//**
     * Checks if the view displays no issues found response provided invalid repository name and valid username.
     * Response received from Repository issues server is "Error! Repository does not present!"
     * @author Farheen Jamadar
     *//*
    @Test
    public void should_returnNoIssuesFoundAlongWithProfileDetails_when_NoIssuesFound_2() throws Exception {
        String username = "sampleUsername";
        String repositoryName = "invalidRepositoryName";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(
                                    new File("test/resources/repositoryprofile/invalidRepositoryProfileDetails.json"));
        ArrayNode issuesList = (ArrayNode) mapper.readTree(
                                    new File("test/resources/repositoryprofile/noIssueListResponse2.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issuesList, assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Issues Found"));
    }

    *//**
     * Checks if the view displays no issues found response provided valid username and
     * repository name. Repository issues server returns an empty list.
     * @author Farheen Jamadar
     *//*
    @Test
    public void should_DisplayNoIssuesFound_provided_emptyIssueList() throws Exception {
        String username = "sampleUsername";
        String repositoryName = "sampleRepositoryName";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repositoryProfileDetails = mapper.readTree(
                                    new File("test/resources/repositoryprofile/validRepositoryProfileDetails.json"));
        ArrayNode issuesList = (ArrayNode) mapper.readTree(
                                    new File("test/resources/repositoryprofile/emptyIssueList.json"));

        Content html = repositoryProfile.render(username, repositoryName, repositoryProfileDetails, issuesList, assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Issues Found"));
    }*/

}
