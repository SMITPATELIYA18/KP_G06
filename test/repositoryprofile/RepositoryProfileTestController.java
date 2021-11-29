/*
package repositoryprofile;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.GitHubAPIMock;
import services.github.GitHubAPI;
import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;


*/
/**
 * Contains cases to test Controller in Repository profile feature
 * @author Farheen Jamadar
 *//*

public class RepositoryProfileTestController {
    private static Application testApp;

    */
/**
     * Binds the interface to the mock implementation of GitHub API
     * @author Farheen Jamadar
     *//*

    @BeforeClass
    public static void setUp() {
        testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
    }

    */
/**
     * Cleans up after all the test cases are executed
     * @author Farheen Jamadar
     *//*

    @AfterClass
    public static void tearDown(){
        Helpers.stop(testApp);
    }

    */
/**
     * Checks if the HTTP GET request returns a successful response
     * @author Farheen Jamadar
     *//*

    @Test
    public void should_ReturnOK_when_ValidGETRequest() {
        String testRepositoryProfileURL = "/repositoryProfile/sampleUsername/sampleRepository";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri(testRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(OK, result.status());
    }

    */
/**
     * Checks if the HTTP POST request returns unsuccessful response
     * @author Farheen Jamadar
     *//*

   @Test
    public void should_ReturnNOT_FOUND_when_NotGETRequest() {
        String testRepositoryProfileURL = "/repositoryProfile/sampleUsername/sampleRepository";

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri(testRepositoryProfileURL);
        Result result = route(testApp, request);

        assertEquals(NOT_FOUND, result.status());
    }
}
*/
