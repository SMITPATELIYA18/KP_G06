package services;

import resources.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.IssueModel;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import resources.TestResources;
import services.github.GitHubAPI;

public class MyAPIClientTest implements WSBodyReadables, WSBodyWritables, GitHubAPI {
	private WSClient client;
	@Inject
	public MyAPIClientTest(WSClient client) {
		this.client = client;
	}
	
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName){
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = null;
		try {
			data = mapper.readTree(TestResources.issueData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompletableFuture<IssueModel> futureModel = new CompletableFuture<>();
		IssueModel modelData = new IssueModel(repoFullName, data);
		futureModel.complete(modelData);
		System.out.println(modelData.getWordLevelData());
		return futureModel;
	}
}
