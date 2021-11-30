package searchreposfeature;

import models.SearchCacheStore;
import models.SearchRepository;
import org.junit.Test;

import static org.junit.Assert.*;
import static play.inject.Bindings.bind;
import static play.mvc.Results.ok;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.routing.RoutingDsl;
import play.server.Server;
import play.test.Helpers;

import services.GitHubAPIImpl;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

import org.junit.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Holds tests related to the search repositories feature
 * @author Pradnya Kandarkar
 */

public class SearchReposImplTest {

    private static Application testApp;
    private static GitHubAPIImpl testGitHubAPIImpl;
    private static WSClient wsClient;
    private static Server server;
    private static String routePattern; /* For holding the route pattern. Changes for every test depending on the test case. */
    private static String testResourceName; /* For returning the resources */



    /**
     * Overrides the binding to use mock implementation instead of the actual implementation and creates a fake
     * application. Sets up an embedded server for testing.
     * @author Pradnya Kandarkar, Indraneel Rachakonda
     */


    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
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
     * @throws IOException If the call cannot be completed due to an error
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

    /**
     * Checks if 10 search results are returned for a single valid search request when <code>getRepositoryFromSearchBar</code>
     * is calledtestApp
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

        assertEquals(10, testSearchResult1.getRepositoryList().size());
        assertEquals(10, testSearchResult2.getRepositoryList().size());
        assertThat(testSearchResult1.getRepositoryList(), everyItem(hasProperty("repositoryName", is(notNullValue()))));
        assertThat(testSearchResult1.getRepositoryList(), everyItem(hasProperty("ownerName", is(notNullValue()))));
        assertThat(testSearchResult1.getRepositoryList(), everyItem(hasProperty("topics")));
        assertThat(testSearchResult2.getRepositoryList(), everyItem(hasProperty("repositoryName", is(notNullValue()))));
        assertThat(testSearchResult2.getRepositoryList(), everyItem(hasProperty("ownerName", is(notNullValue()))));
        assertThat(testSearchResult2.getRepositoryList(), everyItem(hasProperty("topics")));
    }

    /**
     * Checks if maximum 10 search results are returned for any number of queries
     * is called
     * @throws Exception If the call cannot be completed due to an error
     * @author Indraneel Rachakonda
     */
    @Test
    public void should_ReturnMax10Results_when_AnyNumberOfQuerys() throws Exception {
        routePattern = "/search/repositories";
        testResourceName = "searchreposfeature/sampleSearchResult.json";
        SearchCacheStore testSearchStore = new SearchCacheStore();

        for(int i = 1; i <= 11; i++) {
            SearchRepository testSearchResult = testGitHubAPIImpl.getRepositoryFromSearchBar("test_query")
                    .toCompletableFuture().get(10, TimeUnit.SECONDS);
            testSearchStore.addNewSearch(testSearchResult);
            assertTrue(testSearchStore.getSearches().size() <= 10);
        }
    }
}


