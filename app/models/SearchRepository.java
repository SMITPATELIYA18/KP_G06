package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This is model class to handle Repositories' search.
 * 
 * @author SmitPateliya, Farheen Jamadar
 *
 */

public class SearchRepository {
	private String query;
	private CompletableFuture<List<RepositoryModel>> repositoryList;
	/**
	 * 
	 * @param data  Gets data from API
	 * @param query Search query of user
	 */

	public SearchRepository(JsonNode data, String query) {
		this.query = query;

		this.repositoryList = CompletableFuture.supplyAsync(() -> {
			ArrayNode items = (ArrayNode) data.get("items");
			List<RepositoryModel> list = new ArrayList<>();
			java.util.Iterator<JsonNode> iteratorItems = items != null ? items.elements() : Collections.emptyIterator();
			while (iteratorItems.hasNext()) {
				JsonNode item = iteratorItems.next();
				list.add(new RepositoryModel(item));
			}
			return list.stream().limit(10).collect(toList());
		});
	}

	public List<RepositoryModel> getRepositoryList() throws ExecutionException, InterruptedException {
		return repositoryList.get();
	}

	public String getQuery() {
		return query;
	}
}
