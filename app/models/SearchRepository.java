package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This is model class to handle Repositories' search.
 * 
 * @author SmitPateliya
 *
 */

public class SearchRepository {
	private String query;
	private List<RepositoryModel> repositoryList = new ArrayList<>();
	/**
	 * 
	 * @param data  Gets data from API
	 * @param query Search query of user
	 */

	public SearchRepository(JsonNode data, String query) {
		this.setQuery(query);
		ArrayNode items = (ArrayNode) data.get("items");
		java.util.Iterator<JsonNode> iteratorItems = items != null ? items.elements() : Collections.emptyIterator();
		while (iteratorItems.hasNext()) {
			JsonNode item = iteratorItems.next();
			repositoryList.add(new RepositoryModel(item));
		}
		System.out.println("Repository: " + repositoryList);
		repositoryList = repositoryList.stream().limit(10).collect(toList());
	}

	public List<RepositoryModel> getRepositoryList() {
		return repositoryList;
	}

	public void setRepositoryList(List<RepositoryModel> repositoryList) {
		this.repositoryList = repositoryList;
	}

	public void clearRepository() {
		this.repositoryList.clear();
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
