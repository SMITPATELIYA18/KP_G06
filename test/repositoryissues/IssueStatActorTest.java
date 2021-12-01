package repositoryissues;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import actors.Messages;
import actors.SupervisorActor;
import actors.Messages.GetRepositoryIssueActor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import akka.actor.ActorRef;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import resources.TestResources;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

public class IssueStatActorTest {
	private static ActorSystem actorSystem;
    private static TestKit testProbe;
	private static Application testApp;
	private static GitHubAPI testGitHub;
	private static AsyncCacheApi testAsyncCacheApi;
	
	/**
	 * Setting up a Testcases before run
	 * @auther Smit Pateliya
	 */
	@BeforeClass
	public static void setUp() {
		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
		testGitHub = testApp.injector().instanceOf(GitHubAPI.class);
		testAsyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);
		
		actorSystem = ActorSystem.create();
        testProbe = new TestKit(actorSystem);
	}
	
	/**
	 * Terminating setups after run testcases
	 * @author Smit Pateliya
	 */
	@AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
        Helpers.stop(testApp);
    }
	
	/**
	 * This Test case checks Issue Stat Actor
	 * @author Smit Pateliya
	 */
	@Test
	public void testIssueStatActor() throws IOException {
		final ActorRef supervisorActor = actorSystem.actorOf(
				SupervisorActor.props(testProbe.getRef(), testGitHub, testAsyncCacheApi));
		
		//ActorRef<IssueStatActor> issueActor = testKit.spawn(IssueStatActor.props(testProbe.getRef(), testGitHub));
		
		//supervisorActor.tell(new Messages.GetRepositoryIssueActor("TheAlgorithms/Java"), testProbe.getRef());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode issueData = mapper.readTree(new File("test/resources/repositoryissues/sampleSearchData.json"))
;		supervisorActor.tell(issueData, testProbe.getRef());
		JsonNode answer = testProbe.expectMsgClass(JsonNode.class);
		//assertEquals("TheAlgorithms/Java", issueStatInfo.issueModel);
		//System.out.println("hii");
		assertEquals("issueStatInfo", answer.get("responseType").asText());
		assertEquals("TheAlgorithms/Java", answer.get("result").get("repoFullName").asText());
	}
	
	/**
	 * This Test case checks Issue Stat Actor Null Characteristics
	 * @author Smit Pateliya
	 */
	@Test
	public void testIssueStatActorCheckNull() throws IOException {
		final ActorRef supervisorActor = actorSystem.actorOf(
				SupervisorActor.props(testProbe.getRef(), testGitHub, testAsyncCacheApi));
		
		//ActorRef<IssueStatActor> issueActor = testKit.spawn(IssueStatActor.props(testProbe.getRef(), testGitHub));
		
		//supervisorActor.tell(new Messages.GetRepositoryIssueActor("TheAlgorithms/Java"), testProbe.getRef());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode issueData = mapper.readTree(new File("test/resources/repositoryissues/sampleSearchNullData.json"))
;		supervisorActor.tell(issueData, testProbe.getRef());
		JsonNode answer = testProbe.expectMsgClass(JsonNode.class);
		//assertEquals("TheAlgorithms/Java", issueStatInfo.issueModel);
		//System.out.println("hii");
		assertEquals("issueStatInfo", answer.get("responseType").asText());
		assertEquals("sadasd/sadsad", answer.get("result").get("repoFullName").asText());
		assertEquals(true, answer.get("error").asBoolean());
	}
}
