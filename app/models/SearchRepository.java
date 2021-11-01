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
	private List<RepositoryModel> repositorys = new ArrayList<>();

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
			repositorys.add(new RepositoryModel(item));
		}
		repositorys = repositorys.stream().limit(10).collect(toList());
	}

	public List<RepositoryModel> getRepositorys() {
		return repositorys;
	}

	public void setRepositorys(List<RepositoryModel> repositorys) {
		this.repositorys = repositorys;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
