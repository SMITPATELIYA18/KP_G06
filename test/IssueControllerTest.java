import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static play.inject.Bindings.bind;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import controllers.GitterificController;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import static play.test.Helpers.contentAsString;

import resources.TestResources;
import services.GitHubAPIMock;
import play.mvc.*;
import services.github.GitHubAPI;

import org.apache.http.HttpStatus;
import org.junit.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.IssueModel;

//@RunWith(PowerMockRunner.class)
//@PowerMockIgnore({"javax.management.*", "javax.crypto.*"})
/**
 * This class Tests Issue Controller in Controller Package.
 * @author Smit Pateliya
 *
 */
public class IssueControllerTest{
	private Application testApp;
	private GitHubAPI testGitHub;
	private GitterificController issueController;

//	@Override
//	protected Application provideApplication() {
//		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(MyAPIClientTest.class)).build();
//	}
	
	/**
	 * Overrides live API class to Mock API class and creates fake application.
	 */

	@Before
	public void setUp() {
		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
		testGitHub = testApp.injector().instanceOf(GitHubAPI.class);
		issueController = testApp.injector().instanceOf(GitterificController.class);
	}

//	@After
//	public void tearDown() {
//		Helpers.stop(testApp);
//	}
	
	/**
	 * Tests issue controller with the help of the Mockito
	 * 
	 */

	@Test
	public void testIssueController() {
		Helpers.running(testApp, () -> {
//			when(testGitHub.getRepositoryIssue("repoName")).thenReturn(mockIssueController());
			//GitHubAPI api = mock(GitHubAPI.class);
			//doReturn(mockIssueController()).when(testGitHub).getRepositoryIssue("repoName");
//			IssueController controller = mock(IssueController.class);
			CompletionStage<Result> issueStat = issueController.getIssueStat("repoName");
			Result result;
			try {
				result = issueStat.toCompletableFuture().get();
				// System.out.println(contentAsString(result));
				assertEquals(HttpStatus.SC_OK, result.status());
				assertEquals("text/html",result.contentType().get());
				// assertTrue(contentAsString(result).contains("TheAlgorithms/Java")); // ToDo: Smit - Verify this assert
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Mocks issue API fetching call
	 * @return Completion Stage IssueModel object
	 */
	
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
