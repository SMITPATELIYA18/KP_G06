package modules;

import com.google.inject.AbstractModule;

import services.MyAPIClient;
import services.github.GitHubAPI;

public class GitHubModule extends AbstractModule {
	protected final void configure() {
		bind(GitHubAPI.class).to(MyAPIClient.class);
	}
}
