package services.github;

import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;

import models.IssueModel;
import models.SearchRepository;

public interface GitHubAPI {
	CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) throws Exception;
	CompletionStage<JsonNode> getUserProfileByUsername(String username) throws Exception;
	CompletionStage<JsonNode> getUserRepositories(String username) throws Exception;
	CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) throws Exception;
	CompletionStage<IssueModel> getRepositoryIssue(String repoFullName) throws Exception;
	CompletionStage<SearchRepository> getTopicRepository(String topic) throws Exception;
}
