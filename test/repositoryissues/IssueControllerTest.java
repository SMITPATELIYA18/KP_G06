/*
package repositoryissues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static play.inject.Bindings.bind;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import controllers.AssetsFinder;
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
import static play.mvc.Results.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.IssueModel;

//@RunWith(PowerMockRunner.class)
//@PowerMockIgnore({"javax.management.*", "javax.crypto.*"})
*/
/**
 * This class Tests Issue Controller in Controller Package.
 * 
 * @author Smit Pateliya
 *
 *//*

public class IssueControllerTest {
	private Application testApp;
	private GitHubAPI testGitHub;
	private GitterificController issueController;

//	@Override
//	protected Application provideApplication() {
//		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(MyAPIClientTest.class)).build();
//	}

	*/
/**
	 * Overrides live API class to Mock API class and creates fake application.
	 *//*


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

	*/
/**
	 * Tests issue controller with the help of the Mockito
	 * 
	 *//*


	@Test
	public void testIssueController() throws Exception {
		Helpers.running(testApp, () -> {
			GitterificController gitterificController = mock(GitterificController.class);
//			when(gitterificController.getIssueStat("repoName")).thenReturn(mockIssueController());
			try {
				doReturn(mockIssueController()).when(gitterificController).getIssueStat("repoName");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			CompletionStage<Result> issueStat = issueController.getIssueStat("repoName");
			Result result;
			try {
				result = issueStat.toCompletableFuture().get();
				// System.out.println(contentAsString(result));
				assertEquals(HttpStatus.SC_OK, result.status());
				assertEquals("text/html", result.contentType().get());
				assertTrue(contentAsString(result).contains("repoName")); // ToDo: Smit - Verify this assert
				//assertEquals(result, gitterificController.getIssueStat("repoName").toCompletableFuture().get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	*/
/**
	 * Mocks issue API fetching call
	 * 
	 * @return Completion Stage IssueModel object
	 *//*


	private CompletionStage<Result> mockIssueController() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = null;
		//try {
			data = mapper.readTree(TestResources.issueData);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		CompletableFuture<Result> futureModel = new CompletableFuture<>();
		IssueModel modelData = new IssueModel("TheAlgorithms/Java", data);
		AssetsFinder asset = mock(AssetsFinder.class);
		futureModel.complete(ok(views.html.issues.render(modelData, asset)));
//		System.out.println(modelData.getWordLevelData());
		return futureModel;
	}
}
*/
