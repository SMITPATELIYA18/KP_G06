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

public class TopicsTest {
    private static AssetsFinder assetsFinder;
    private static Application testApp;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern;
    private static String testResourceName;

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
    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String sampleTopicRepositoryListURL = "/topics/python";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(sampleTopicRepositoryListURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

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
    @Test
    public void should_ReturnTopicRepositoryList_provided_Topic() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "topicfeature/validTopicRepositoryList.json";
        SearchRepository testRepositoryProfile = testGitHubAPIImpl.getTopicRepository("sampleTopic")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        assertEquals(Arrays.asList("goofing", "play", "test"), testRepositoryProfile.getRepositoryList().get(0).getTopics());
    }

    @Test
    public void should_ReturnEmptyTopicList_provided_InvalidTopic() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "topicfeature/invalidTopicRepositoryList.json";
        SearchRepository testRepositoryProfile = testGitHubAPIImpl.getTopicRepository("sampleTopic")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);

        assertEquals(new ArrayList<>(), testRepositoryProfile.getRepositoryList());
    }


    //UI
    @Test
    public void should_DisplayTopicRepositoryList_provided_Topic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode topicRepositoryList = mapper.readTree(new File("test/resources/topicfeature/validTopicRepositoryList.json"));

        Content html = views.html.topics.topics.render(new SearchRepository(topicRepositoryList, "sampleTopic"), assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("iamstillal"));
    }

    @Test
    public void should_NoResultFound_provided_InvalidTopic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode topicRepositoryList = mapper.readTree(new File("test/resources/topicfeature/invalidTopicRepositoryList.json"));

        Content html = views.html.topics.topics.render(new SearchRepository(topicRepositoryList, "sampleTopic"), assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Results found"));
    }

    @Test
    public void should_DisplayNoResultFound_when_GitHubResponseNull(){
        Content html = views.html.topics.topics.render(new SearchRepository(null, "sampleTopic"), assetsFinder);
        assertEquals("text/html", html.contentType());
        assertTrue(contentAsString(html).contains("No Results found"));
    }
}
