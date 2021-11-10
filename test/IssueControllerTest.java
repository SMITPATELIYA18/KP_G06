import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.typedmap.TypedKey;
import play.mvc.Http.Request;
import play.mvc.Http.RequestBuilder;
import play.routing.RoutingDsl;
import play.test.Helpers;
import play.test.WithApplication;
import resources.TestResources;
import services.MyAPIClientTest;
import play.mvc.*;
import services.github.GitHubAPI;

import org.apache.http.HttpStatus;
import org.junit.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Inject;
import static org.mockito.ArgumentMatchers.anyString;

import controllers.IssueController;
import controllers.routes;
import models.IssueModel;

//@RunWith(PowerMockRunner.class)
//@PowerMockIgnore({"javax.management.*", "javax.crypto.*"})
public class IssueControllerTest{
	private Application testApp;
	private GitHubAPI testGitHub;
	private IssueController issueController;

//	@Override
//	protected Application provideApplication() {
//		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(MyAPIClientTest.class)).build();
//	}

	@Before
	public void setUp() {
		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(MyAPIClientTest.class)).build();
		testGitHub = testApp.injector().instanceOf(GitHubAPI.class);
		issueController = testApp.injector().instanceOf(IssueController.class);
	}

//	@After
//	public void tearDown() {
//		Helpers.stop(testApp);
//	}

	@Test
	public void testIssueController() {
		Helpers.running(testApp, () -> {
			when(testGitHub.getRepositoryIssue("repoName")).thenReturn(mockIssueController());
//			IssueController controller = mock(IssueController.class);
			CompletionStage<Result> issueStat = issueController.getIssueStat("repoName");
			Result result;
			try {
				result = issueStat.toCompletableFuture().get();
				assertEquals(HttpStatus.SC_OK, result.status());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	private CompletionStage<IssueModel> mockIssueController() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = null;
		try {
			data = mapper.readTree(TestResources.issueData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompletableFuture<IssueModel> futureModel = new CompletableFuture<>();
		IssueModel modelData = new IssueModel("TheAlgorithms/Java", data);
		futureModel.complete(modelData);
//		System.out.println(modelData.getWordLevelData());
		return futureModel;
	}
}
