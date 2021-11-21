package topics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.SearchRepository;
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

/**
 * Tests for the topics feature
 * @author Indraneel Rachakonda
 */
public class TopicsTest {
    private static AssetsFinder assetsFinder;
    private static Application testApp;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern;
    private static String testResourceName;

    /**
     * Sets up mock server for testing
     * @author Indraneel Rachakonda
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
                                        .routingTo((request) -> ok().sendResource(testResourceName))
                                        .build());
        wsClient = play.test.WSTestClient.newClient(server.httpPort());

        testGitHubAPIImpl = testApp.injector().instanceOf(GitHubAPIImpl.class);
        testGitHubAPIImpl.setBaseURL("");
        testGitHubAPIImpl.setClient(wsClient);
    }

    /**
     * Cleans up after all test cases are executed
     * @throws IOException In case of an error
     * @author Indraneel Rachakonda
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

    //Controller

    /**
     * Checks if OK (200) status is returned for valid GET requests
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String sampleTopicRepositoryListURL = "/topics/python";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(sampleTopicRepositoryListURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

    /**
     * Checks if NOT_FOUND (404) status is returned when the request is not a GET request
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_ReturnNOT_FOUND_when_NotGETRequest() {
        String sampleRepositoryProfileURL = "/topics/python";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(sampleRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(NOT_FOUND, result.status());
    }


    //Testing Actual Implementations
    /**
     * Checks if repository list is returned for provided topic
     * @throws Exception In case of an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_ReturnTopicRepositoryList_provided_Topic() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "topicfeature/validTopicRepositoryList.json";
        SearchRepository testRepositoryProfile = testGitHubAPIImpl.getTopicRepository("sampleTopic")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals(Arrays.asList("goofing", "play", "test"), testRepositoryProfile.getRepositoryList().get(0).getTopics());
    }

    /**
     * Checks if empty repository list is returned when no repositories are available for a topic name
     * @throws Exception In case of an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_ReturnEmptyTopicList_provided_InvalidTopic() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "topicfeature/invalidTopicRepositoryList.json";
        SearchRepository testRepositoryProfile = testGitHubAPIImpl.getTopicRepository("sampleTopic")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);

        assertEquals(new ArrayList<>(), testRepositoryProfile.getRepositoryList());
    }


    //UI
    /**
     * Checks if repository list is displayed for provided topic via topics.scala.html
     * @throws Exception In case of an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_DisplayTopicRepositoryList_provided_Topic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode topicRepositoryList = mapper.readTree(new File("test/resources/topicfeature/validTopicRepositoryList.json"));

        Content html = views.html.topics.topics.render(new SearchRepository(topicRepositoryList, "sampleTopic"), assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("iamstillal"));
    }

    /**
     * Checks if repository list is displayed for provided topic via topicsDisplay.scala.html
     * @throws Exception In case of an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_DisplayTopicRepositoryList_provided_Topic_TopicsDisplay() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode topicRepositoryList = mapper.readTree(new File("test/resources/topicfeature/validTopicRepositoryList.json"));

        Content html = views.html.topics.topicsDisplay.render(new SearchRepository(topicRepositoryList, "sampleTopic") , "java");
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("iamstillal"));
    }

    /**
     * Checks if "No Results found" is displayed when no repositories are available for a topic name
     * via topics.scala.html
     * @throws Exception In case of an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_NoResultFound_provided_InvalidTopic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode topicRepositoryList = mapper.readTree(new File("test/resources/topicfeature/invalidTopicRepositoryList.json"));

        Content html = views.html.topics.topics.render(new SearchRepository(topicRepositoryList, "sampleTopic"), assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Results found"));
    }

    /**
     * Checks if "No Results found" is displayed when no repositories are available for a topic name
     * via topicsDisplay.scala.html
     * @throws Exception In case of an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_NoResultFound_provided_InvalidTopic_TopicsDisplay() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode topicRepositoryList = mapper.readTree(new File("test/resources/topicfeature/invalidTopicRepositoryList.json"));

        Content html = views.html.topics.topicsDisplay.render(new SearchRepository(topicRepositoryList, "sampleTopic"), "java");
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Results found"));
    }
}
