package services.github;

import java.util.concurrent.CompletionStage;

import models.IssueModel;

public interface GitHubAPI {
<<<<<<< Updated upstream
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName);
=======
	CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) throws Exception;
	CompletionStage<JsonNode> getUserProfileByUsername(String username) throws Exception;
	CompletionStage<JsonNode> getUserRepositories(String username) throws Exception;
	CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName) throws Exception;
	CompletionStage<JsonNode> getRepositoryIssue(String repoFullName);
	CompletionStage<SearchRepository> getTopicRepository(String topic) throws Exception;
>>>>>>> Stashed changes
}
