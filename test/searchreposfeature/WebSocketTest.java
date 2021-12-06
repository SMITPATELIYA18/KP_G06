package searchreposfeature;

import org.junit.Test;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.netty.ws.NettyWebSocket;
import play.test.TestServer;
import play.test.WithServer;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static play.test.Helpers.*;

/**
 * Holds test for websocket
 * @author Farheen Jamadar
 */

public class WebSocketTest extends WithServer {

    /**
     * Websocket test case
     * @author Farheen Jamadar
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
    }

}


