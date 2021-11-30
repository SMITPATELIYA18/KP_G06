package actors;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import akka.actor.ActorRef;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

public class IssueStatActorTest {
	private static ActorSystem actorSystem;
    private static TestKit testProbe;
	private static Application testApp;
	private static GitHubAPI testGitHub;
	private static AsyncCacheApi testAsyncCacheApi;
	
	@BeforeClass
	public static void setUp() {
		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
		testGitHub = testApp.injector().instanceOf(GitHubAPI.class);
		testAsyncCacheApi = testApp.injector().instanceOf(AsyncCacheApi.class);
		
		actorSystem = ActorSystem.create();
        testProbe = new TestKit(actorSystem);
	}
	
	@Test
	public void testIssueStatActor() {
		final ActorRef supervisorActor = actorSystem.actorOf(
				SupervisorActor.props(testProbe.getRef(), testGitHub, testAsyncCacheApi));
		
		//ActorRef<IssueStatActor> issueActor = testKit.spawn(IssueStatActor.props(testProbe.getRef(), testGitHub));
		
		supervisorActor.tell(new Messages.GetRepositoryIssueActor("TheAlgorithms/Java"), testProbe.getRef());
		JsonNode answer = testProbe.expectMsgClass(JsonNode.class);
		//assertEquals("TheAlgorithms/Java", issueStatInfo.issueModel);
		System.out.println(answer);
		//assertEquals("issueStatInfo", issueStatInfo.issueModel.get("responseType").asText());
	}
}
