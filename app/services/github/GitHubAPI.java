package services.github;

import java.util.concurrent.CompletionStage;

import models.IssueModel;

public interface GitHubAPI {
	public CompletionStage<IssueModel> getRepositoryIssue(String repoFullName);
}
