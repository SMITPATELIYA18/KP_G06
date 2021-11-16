package services.github;

import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;
import models.IssueModel;

/**
 * Declares methods used for interaction with GitHub REST API
 * @author Smit Pateliya, Pradnya Kandarkar
 */
public interface GitHubAPI {
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName);
	CompletionStage<JsonNode> getUserProfileByUsername(String username);
	CompletionStage<JsonNode> getUserRepositories(String username);
	CompletionStage<JsonNode> getRepositoryProfile(String username, String repositoryName);
}
