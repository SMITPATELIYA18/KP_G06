package services.github;

import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;
import models.IssueModel;
import models.SearchRepository;

/**
 * Declares methods used for interaction with GitHub REST API
 * @author Smit Pateliya, Pradnya Kandarkar
 */
public interface GitHubAPI {
	CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query);
	CompletionStage<JsonNode> getUserProfileByUsername(String username);
	CompletionStage<JsonNode> getUserRepositories(String username);
	CompletionStage<JsonNode> getRepositoryProfile(String ownerName, String repositoryName) throws Exception;
	CompletionStage<IssueModel> getRepositoryIssue(String repoFullName);
	CompletionStage<SearchRepository> getTopicRepository(String topic);
}
