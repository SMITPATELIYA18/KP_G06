package modules;

import com.google.inject.AbstractModule;

import services.GitHubAPIImpl;
import services.github.GitHubAPI;

/**
 * This class helps to change endpoint of API for testing purpose. 
 * @author Smit Pateliya
 *
 */
public class GitHubModule extends AbstractModule {
	protected final void configure() {
		bind(GitHubAPI.class).to(GitHubAPIImpl.class);
	}
}
