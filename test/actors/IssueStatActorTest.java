package actors;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;

import org.junit.BeforeClass;
import org.junit.ClassRule;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import akka.actor.typed.ActorRef;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import services.GitHubAPIMock;
import services.github.GitHubAPI;

public class IssueStatActorTest {
	private static ActorSystem actorSystem;
    private static TestKit testProbe;
	private static Application testApp;
	private static GitHubAPI testGitHub;
	
	@BeforeClass
	public static void setUp() {
		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
		testGitHub = testApp.injector().instanceOf(GitHubAPI.class);
		
		actorSystem = ActorSystem.create();
        testProbe = new TestKit(actorSystem);
	}
	
	@Test
	public void testIssueStatActor() {
		TestProbe<Messages.IssueStatInfo> issueStatInfo  = testKit.createTestProbe(Messages.IssueStatInfo.class);
		
		ActorRef<IssueStatActor> issueActor = testKit.spawn(IssueStatActor.props(testProbe.getRef(), testGitHub));
		
		issueActor.tell(new Messages.GetRepositoryIssueActor("TheAlgorithms/Java"), issueStatInfo.getRef());
		//assertEquals("TheAlgorithms/Java", issueStatInfo.issueModel);
		assertEquals("issueStatInfo", issueStatInfo.issueModel.get("responseType").asText());
	}
}
