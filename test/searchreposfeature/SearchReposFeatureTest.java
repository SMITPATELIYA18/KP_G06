package searchreposfeature;

import controllers.AssetsFinder;
import models.SearchRepository;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;

import play.Application;
import play.cache.AsyncCacheApi;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Holds tests related to the search repositories feature
 * @author 
 */
public class SearchReposFeatureTest {

    private static Application testApp;
    private static AssetsFinder assetsFinder;
    private static AsyncCacheApi asyncCacheApi;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern;                 /* For holding the route pattern. Changes for every test depending on the test case. */
    private static String testResourceName;             /* For returning the resources */

    /**
     * Overrides the binding to use mock implementation instead of the actual implementation and creates a fake
     * application. Sets up an embedded server for testing.
     * @author 
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        assetsFinder = testApp.injector().instanceOf(AssetsFinder.class);
        asyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);
        server =
                Server.forRouter(
                        (components) ->
                                RoutingDsl.fromComponents(components)
                                        .GET(routePattern)
                                        .routingTo(request -> ok().sendResource(testResourceName))
                                        .build());
        wsClient = play.test.WSTestClient.newClient(server.httpPort());
        testGitHubAPIImpl = testApp.injector().instanceOf(GitHubAPIImpl.class);
        testGitHubAPIImpl.setBaseURL("");
        testGitHubAPIImpl.setClient(wsClient);
    }


    /**
     * Performs clean up activities after all tests are performed
     * @author Pradnya Kandarkar
     * @throws Exception If the call cannot be completed due to an error
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

    // Tests for the responses returned by the controller that are related to the search repository feature

    /**
     * Validates if HTTP response OK (200) is received for valid GET request(s)
     * @author
     */
    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String testHomePageURL = "/";
        String testSearchURL = "/?search=github";

        Http.RequestBuilder homePageRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(testHomePageURL);
        Http.RequestBuilder testSearchRequest = new Http.RequestBuilder()
                .method(GET)
                .uri(testSearchURL);
        Result homePageResult = route(testApp, homePageRequest);
        Result testSearchResult = route(testApp, testSearchRequest);

        assertEquals(OK, homePageResult.status());
        assertEquals(OK, testSearchResult.status());


    }

    /**
     * Validates if HTTP response NOT_FOUND (404) is received for a request type that is not implemented for the URL
     * @author 
     */
    @Test
    public void should_ReturnNOT_FOUND_when_NotGETRequest() {
        String sampleSearchURL = "/";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(sampleSearchURL);
        Result result = route(testApp, request);

        assertEquals(NOT_FOUND, result.status());
    }

    // ToDo: Pradnya
    @Test
    public void testCachedData() {
        String requestURL1 = "/?search=github";
        String requestURL2 = "/?search=github";

        long start = System.currentTimeMillis();
        Http.RequestBuilder request1 = new Http.RequestBuilder()
                .method(GET)
                .uri(requestURL1);
        Result result1 = route(testApp, request1);
        long end = System.currentTimeMillis();
        long timeRequest1 = end - start;

        start = System.currentTimeMillis();
        Http.RequestBuilder request2 = new Http.RequestBuilder()
                .method(GET)
                .uri(requestURL2);
        Result result2 = route(testApp, request1);
        end = System.currentTimeMillis();
        long timeRequest2 = end - start;

        assertEquals(OK, result1.status());
        assertEquals(OK, result2.status());
        assertTrue(timeRequest1 > timeRequest2 * 5);
    }

    // Tests for the methods implemented for GitHubAPI that are related to the search repository feature

    /**
     * Checks if 10 search results are returned for a single valid search request
     * @throws Exception If the call cannot be completed due to an error
     * @author Pradnya Kandarkar
     */
    @Test
    public void should_Return10ReposList_when_ValidSearchRequest() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "searchreposfeature/sampleSearchResult.json";

        SearchRepository testSearchResult1 = testGitHubAPIImpl.getRepositoryFromSearchBar("test_query")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        SearchRepository testSearchResult2 = testGitHubAPIImpl.getRepositoryFromSearchBar("multiple words query")
                        .toCompletableFuture().get(10, TimeUnit.SECONDS);

        assertAll(
                () -> assertEquals(10, testSearchResult1.getRepositoryList().size()),
                () -> assertEquals(10, testSearchResult2.getRepositoryList().size()),
                () -> assertThat(testSearchResult1.getRepositoryList(), everyItem(hasProperty("repositoryName", is(notNullValue())))),
                () -> assertThat(testSearchResult1.getRepositoryList(), everyItem(hasProperty("ownerName", is(notNullValue())))),
                () -> assertThat(testSearchResult1.getRepositoryList(), everyItem(hasProperty("topics"))),
                () -> assertThat(testSearchResult2.getRepositoryList(), everyItem(hasProperty("repositoryName", is(notNullValue())))),
                () -> assertThat(testSearchResult2.getRepositoryList(), everyItem(hasProperty("ownerName", is(notNullValue())))),
                () -> assertThat(testSearchResult2.getRepositoryList(), everyItem(hasProperty("topics")))
        );
    }
}


