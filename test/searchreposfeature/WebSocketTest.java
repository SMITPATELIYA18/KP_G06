package searchreposfeature;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import controllers.AssetsFinder;
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
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.netty.ws.NettyWebSocket;
import play.test.Helpers;
import play.test.TestServer;
import play.test.WithServer;
import play.twirl.api.Content;
import services.GitHubAPIMock;
import services.GitterificService;
import services.github.GitHubAPI;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
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

public class WebSocketTest extends WithServer {

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



    /**
     * Validates if HTTP response OK (200) is received for valid GET request(s)
     * @author Pradnya Kandarkar, Farheen Jamadar
     */

    @Test
    public void websocket(){
        TestServer server = testServer(9000);
        running(server, () -> {
            try {
                AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build();
                AsyncHttpClient client = new DefaultAsyncHttpClient(config);
                WebSocketClient webSocketClient = new WebSocketClient(client);

                try {
                    String serverURL = "ws://localhost:9000/ws";
                    WebSocketClient.LoggingListener listener = new WebSocketClient.LoggingListener(message -> {});
                    CompletableFuture<NettyWebSocket> completionStage = webSocketClient.call(serverURL, serverURL, listener);
                    await().until(completionStage::isDone);
                    System.out.println("Print: " + completionStage.get());
                   assertThat(completionStage.get()).isNotNull();
                } finally {
                    client.close();
                }
            } catch (Exception e) {
                fail("Unexpected exception", e);
            }
        });
        //assertEquals(true, new GitterificController( assetsFinder, httpExecutionContext, asyncCacheApi, gitHubAPIInst, gitterificService, actorSystem, materializer).ws().toString().contains("play.mvc.WebSocket"));
        //TODO: Farheen Improvement

    }

}


