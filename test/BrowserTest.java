/*
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import play.test.TestBrowser;
import play.test.WithBrowser;
import services.GitHubAPIMock;
import services.github.GitHubAPI;
//TODO: Farheen: remove?
import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;

*/
/**
 * Holds additional functional tests for the application
 * @author Pradnya Kandarkar
 *//*

public class BrowserTest extends WithBrowser {

    protected Application provideApplication() {
        return new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
    }

    protected TestBrowser provideBrowser(int port) {
        return Helpers.testBrowser(port);
    }

    */
/**
     * Checks if the application home page loads as expected
     *//*

    @Test
    public void testApplicationStart() {
        browser.goTo("http://localhost:" + play.api.test.Helpers.testServerPort());
        assertTrue(browser.pageSource().contains("Gitterific"));
    }

}
*/
