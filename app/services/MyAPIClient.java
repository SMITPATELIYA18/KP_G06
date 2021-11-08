package services;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

import models.IssueModel;
import models.SearchRepository;
import play.libs.ws.*;
import services.github.GitHubAPI;

/**
 * This class handles all API of GitHub.
 * @author SmitPateliya
 *
 */

public class MyAPIClient implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	private final WSClient client;
	private String baseURL = "https://api.github.com";
	
	/**
	 *  
	 * @param client Constructor gets data from the Controller class.
	 */

	@Inject
	public MyAPIClient(WSClient client) {
		this.client = client;
	}
	
	/**
	 * This method is getting information from user and returns
	 * all the information regarding the query.
	 * 
	 * @param query Gets the query from User to search repositories.
	 * @return Returns SearchRepository Model containing all Repository information.
	 */

	public CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) {
		String finalURL = baseURL + "/search/repositories";
		CompletionStage<SearchRepository> searchResult = client.url(finalURL).addQueryParameter("q", query)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new SearchRepository(result.asJson(), query));
		return searchResult;
	}
	
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName){
		String finalURL = baseURL + "/repos/"+repoFullName+"/issues";
		CompletionStage<IssueModel> searchResult = client.url(finalURL)
				.addHeader("accept", "application/vnd.github.v3+json").get()
				.thenApplyAsync(result -> new IssueModel(repoFullName,result.asJson()));
		return searchResult;
	}
	
	public void setBaseURL(String URL) {
		this.baseURL = URL;
	}
	
	public String getBaseURL() {
		return this.baseURL;
	}

}
