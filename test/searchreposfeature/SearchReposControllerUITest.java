package searchreposfeature;

import controllers.AssetsFinder;
import models.SearchCacheStore;
import models.SearchRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.Helpers;
import play.test.WithServer;
import play.twirl.api.Content;
import play.twirl.api.Html;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;

/**
 * Holds tests for Controller and UI for search repositories feature
 * @author Farheen Jamadar, Indraneel Rachakonda
 */

public class SearchReposControllerUITest extends WithServer {

    private static Application testApp;
    private static AssetsFinder assetsFinder;
    private static AsyncCacheApi asyncCacheApi;
    private static GitHubAPI testGitHubAPI;
    private static Server server;
    private static String routePattern; /* For holding the route pattern. Changes for every test depending on the test case. */
    private static String testResourceName; /* For returning the resources */

    /**
     * Overrides the binding to use mock implementation instead of the actual implementation and creates a fake
     * application. Sets up an embedded server for testing.
     * @author Farheen Jamadar
     */
    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
        testGitHubAPI = testApp.injector().instanceOf(GitHubAPI.class);
        assetsFinder = testApp.injector().instanceOf(AssetsFinder.class);
        asyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);

        server =
                Server.forRouter(
                        (components) ->
                                RoutingDsl.fromComponents(components)
                                        .GET(routePattern)
                                        .routingTo(request -> ok().sendResource(testResourceName))
                                        .build());
    }


    /**
     * Performs clean up activities after all tests are performed
     * @author Farheen Jamadar
     * @throws IOException If the call cannot be completed due to an error
     */
    @AfterClass
    public static void tearDown() throws IOException{
        Helpers.stop(testApp);
    }

    /**
     * Validates if HTTP response OK (200) is received for valid GET request(s)
     * @author Pradnya Kandarkar, Farheen Jamadar
     */
    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String testRepositoryProfileURL = "/";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(testRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

    /**
     * Validates if HTTP response NOT_FOUND (404) is received for a request type that is not implemented for the URL
     * @author Pradnya Kandarkar
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


    /**
     * Checks if home page results are displayed as expected via index.scala.html
     * @throws Exception If the call cannot be completed due to an error
     * @author Indraneel Rachakonda, Farheen Jamadar
     */
    @Test
    public void homePageDisplayTest_index() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "searchreposfeature/sampleSearchResult.json";
        SearchRepository testSearchResult = testGitHubAPI.getRepositoryFromSearchBar("test_query")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        SearchCacheStore testSearchStore = new SearchCacheStore();
        testSearchStore.addNewSearch(testSearchResult);

        Http.Request request = mock(Http.Request.class);
        Content homePageBeforeSearch = views.html.index.render(request, null, assetsFinder);

        assertEquals("text/html", homePageBeforeSearch.contentType());
        assertTrue(contentAsString(homePageBeforeSearch).contains("all-search-results"));
    }


    /**
     * Checks if home page results are displayed as expected via main.scala.html
     * @throws Exception If the call cannot be completed due to an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void homePageDisplayTest_main() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "searchreposfeature/sampleSearchResult.json";
        SearchRepository testSearchResult = testGitHubAPI.getRepositoryFromSearchBar("test_query")
                .toCompletableFuture().get(10, TimeUnit.SECONDS);
        SearchCacheStore testSearchStore = new SearchCacheStore();
        testSearchStore.addNewSearch(testSearchResult);

        Http.Request request = mock(Http.Request.class);
        Content homePageBeforeSearch = views.html.main.render(request, "", new Html("hello"),assetsFinder);

        assertEquals("text/html", homePageBeforeSearch.contentType());
        assertTrue(contentAsString(homePageBeforeSearch).contains("all-search-results"));
    }
}