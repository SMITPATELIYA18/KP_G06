package services.github;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import models.IssueModel;
import models.SearchRepository;

/**
 * Declares methods used for interaction with GitHub REST API
 * @author Smit Pateliya, Pradnya Kandarkar
 */
public interface GitHubAPI {
	CompletionStage<SearchRepository> getRepositoryFromSearchBar(String query) throws Exception;
	CompletionStage<JsonNode> getUserProfileByUsername(String username) throws Exception;
	CompletionStage<JsonNode> getUserRepositories(String username) throws Exception;
	CompletionStage<JsonNode> getRepositoryProfile(String ownerName, String repositoryName);
	CompletionStage<IssueModel> getRepositoryIssue(String repoFullName);
	CompletionStage<SearchRepository> getTopicRepository(String topic);
}
