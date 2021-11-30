//package repositoryissues;
//
//import play.Application;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.*;
//import play.inject.guice.GuiceApplicationBuilder;
//import play.test.Helpers;
//import services.GitHubAPIMock;
//import services.github.GitHubAPI;
//import static play.inject.Bindings.bind;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.concurrent.CompletionStage;
//import java.util.concurrent.ExecutionException;
//
//import org.assertj.core.util.Arrays;
//import org.junit.*;
//
//import models.IssueModel;
//
///**
// * This class tests Issue Controller API.
// * @author Smit Pateliya 40202779
// *
// */
//
//public class IssuesControllerAPITest {
//	private static Application testApp;
//	private static GitHubAPI testGitHub;
//	
//	/**
//	 * Changes the bind from Live API to fake API and injects the injector in to GitHubAPI.
//	 */
//
//	@BeforeClass
//	public static void setUp() {
//		testApp = new GuiceApplicationBuilder().overrides(bind(GitHubAPI.class).to(GitHubAPIMock.class)).build();
//		testGitHub = testApp.injector().instanceOf(GitHubAPI.class);
//	}
//	
//	/**
//	 * Stops the fake Application.
//	 * @throws Exception
//	 */
//
//	@AfterClass
//	public static void tearDown() throws Exception {
//		Helpers.stop(testApp);
//	}
//	
//	/**
//	 * Creates Mock API result and Calls the API.
//	 * After that asserts the result with the Mock data. 
//	 * @throws InterruptedException
//	 * @throws ExecutionException
//	 */
//
//	@Test
//	public void testIssues() throws InterruptedException, ExecutionException {
//		IssueModel issueModel = mock(IssueModel.class);
//		when(issueModel.getRepoFullName()).thenReturn("TheAlgorithms/Java");
//		LinkedHashMap<String, Long> mockMap =  new LinkedHashMap<>() {{
//			put("Fill", Long.valueOf(2));
//			put("Algorithm", Long.valueOf(2));
//			put("Added", Long.valueOf(1));
//			put("code", Long.valueOf(1));
//			put("Flood", Long.valueOf(1));
//			put("Boundary", Long.valueOf(1));
//		}};
//		when(issueModel.getWordLevelData()).thenReturn(mockMap);
//		CompletionStage<IssueModel> result = testGitHub.getRepositoryIssue("TheAlgorithms/Java");
//		IssueModel resultFromAPI =  result.toCompletableFuture().get();
//		assertEquals(resultFromAPI.getRepoFullName(), issueModel.getRepoFullName());
//		assertEquals(resultFromAPI.getWordLevelData(), issueModel.getWordLevelData());
//	}
//	
//	@Test
//	public void checkNullTestIssues() throws InterruptedException, ExecutionException {
//		IssueModel issueModel = mock(IssueModel.class);
//		when(issueModel.getRepoFullName()).thenReturn("sadasd/sadsad");
//		ArrayList<String> mockList = new ArrayList<>() {{
//			add("Error! Repository does not present!");
//		}};
//		when(issueModel.getIssueTitles()).thenReturn(mockList);
//		CompletionStage<IssueModel> result = testGitHub.getRepositoryIssue("sadasd/sadsad");
//		IssueModel resultFromAPI =  result.toCompletableFuture().get();
//		assertEquals(resultFromAPI.getRepoFullName(), issueModel.getRepoFullName());
//		assertEquals(resultFromAPI.getIssueTitles(), issueModel.getIssueTitles());
//	}
//}
