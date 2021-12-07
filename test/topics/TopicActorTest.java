package topics;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import actors.SupervisorActor;
import actors.TopicActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.testkit.EventFilter;
import akka.testkit.javadsl.TestKit;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

/**
 * Contains cases to test TopicActor functionality
 * 
 * @author Indraneel Rachakonda
 */
public class TopicActorTest {
	private static ActorSystem actorSystem;
	private static TestKit testProbe;
	private static Application testApp;
	private static GitHubAPI testGitHubAPI;
	private static AsyncCacheApi testAsyncCacheApi;

	/**
	 * Binds the interface to the mock implementation of GitHub API. Initialization
	 * of Testkit
	 * 
	 * @author Indraneel Rachakonda
	 */
	@BeforeClass
	public static void setup() {
		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
		testGitHubAPI = testApp.injector().instanceOf(GitHubAPI.class);
		testAsyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);

		actorSystem = ActorSystem.create();
		testProbe = new TestKit(actorSystem);
	}

	/**
	 * Cleans up after all the test cases are executed
	 * 
	 * @author Indraneel Rachakonda
	 */
	@AfterClass
	public static void tearDown() {
		TestKit.shutdownActorSystem(actorSystem);
		actorSystem = null;
		Helpers.stop(testApp);
	}

	/**
	 * Checks if the Topic Actor handles repository query request
	 * 
	 * @throws IOException Exception thrown by Mapper class in case of any issue
	 *                     while reading the file
	 * @author Indraneel Rachakonda
	 */
	@Test
	public void testTopicActorQuery() throws IOException {
		// Supervisor actor required as it is the entrypoint
		final ActorRef supervisorActor = actorSystem
				.actorOf(SupervisorActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode topicQuery = mapper.readTree(new File("test/resources/topicfeature/validTopicRepositoryDetails.json"));

		supervisorActor.tell(topicQuery, testProbe.getRef());
		supervisorActor.tell(topicQuery, testProbe.getRef());
		JsonNode topicInfo = testProbe.expectMsgClass(JsonNode.class);
        assertEquals("topicInfo", topicInfo.get("responseType").asText());
        assertEquals("java", topicInfo.get("query").asText());
        assertEquals(2, topicInfo.get("repositoryList").size());
	}

	/**
	 * Checks if the Topic Actor handles unknown query request
	 * 
	 * @throws IOException Exception thrown by Mapper class in case of any issue
	 *                     while reading the file
	 * @author Indraneel Rachakonda
	 */
	@Test
	public void testTopicActorReceivesRandomQuery() throws IOException {
		final ActorRef topicActor = actorSystem
				.actorOf(TopicActor.props(testProbe.getRef(), testGitHubAPI, testAsyncCacheApi));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode arbitrarySearchQuery = mapper
				.readTree(new File("test/resources/topicfeature/arbitraryTopicQuery.json"));

		final String unknownMessageStatus = new EventFilter(1) {
			@Override
			public boolean matches(Logging.LogEvent event) {
				return true;
			}
		}.intercept(() -> {
			topicActor.tell(arbitrarySearchQuery, testProbe.getRef());
			return "Unknown Message type intercepted";
		}, actorSystem);

		assertEquals("Unknown Message type intercepted", unknownMessageStatus);
	}

}
