package searchreposfeature;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import controllers.AssetsFinder;
import controllers.GitterificController;
import models.SearchCacheStore;
import models.SearchRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.Helpers;
import play.test.WithServer;
import play.twirl.api.Content;
import services.GitHubAPIMock;
import services.GitterificService;
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
 * Holds tests  for Controller and UI for search repositories feature
 * @author Farheen Jamadar, Indraneel Rachakonda
 */

public class SearchReposFeatureTest extends WithServer {

    private static Application testApp;
    private static AssetsFinder assetsFinder;
    private static AsyncCacheApi asyncCacheApi;
    private static GitHubAPI testGitHubAPI;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern; /* For holding the route pattern. Changes for every test depending on the test case. */
    private static String testResourceName; /* For returning the resources */

    private static HttpExecutionContext httpExecutionContext;
    private static GitHubAPI gitHubAPIInst;
    private static GitterificService gitterificService;
    private static ActorSystem actorSystem;
    private static Materializer materializer;


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


        httpExecutionContext = testApp.injector().instanceOf(HttpExecutionContext.class);
        gitHubAPIInst = testApp.injector().instanceOf(GitHubAPI.class);
        gitterificService = testApp.injector().instanceOf(GitterificService.class);
        actorSystem = testApp.injector().instanceOf(ActorSystem.class);
        materializer = testApp.injector().instanceOf(Materializer.class);

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
     * Checks if home page results are displayed as expected
     * is called
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

        System.out.println("homePageBeforeSearch: " + homePageBeforeSearch);
        assertEquals("text/html", homePageBeforeSearch.contentType());
        assertTrue(contentAsString(homePageBeforeSearch).contains("all-search-results"));
    }

    /**
     * WebSocket test
     * @throws Exception If the call cannot be completed due to an error
     * @author Farheen Jamadar
     */
    @Test
    public void websocket(){
        /*routePattern = "/ws";
        testResourceName = "searchreposfeature/sampleSearchResult.json";

        System.out.println("Port: " + testServer.server());
        System.out.println("Port123: " + testServer.getRunningHttpPort());

        String url = "ws://localhost:" + testServer.getRunningHttpPort() + routes.GitterificController.ws();

        WSClient ws = play.test.WSTestClient.newClient(testServer.getRunningHttpPort().getAsInt());

        System.out.println("Port1234: " + ws.url(url).get());


*/
        assertEquals(true, new GitterificController( assetsFinder, httpExecutionContext, asyncCacheApi, gitHubAPIInst, gitterificService, actorSystem, materializer).ws().toString().contains("play.mvc.WebSocket"));
        //TODO: Farheen Improvement

    }

}


