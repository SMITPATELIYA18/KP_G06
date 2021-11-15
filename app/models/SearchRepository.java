package models;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This is model class contains to handle search query.
 * @author SmitPateliya, Farheen Jamadar
 *
 */

public class SearchRepository {
	private String query;
	private List<RepositoryModel> repositoryList;
	/**
	 * 
	 * @param data  Gets data from API
	 * @param query Search query of user
	 */

	public SearchRepository(JsonNode data, String query) {
		this.query = query;

		ArrayNode items = (ArrayNode) data.get("items");

		Stream<JsonNode> stream = StreamSupport.stream(Spliterators
				.spliteratorUnknownSize(items.elements(),
						Spliterator.ORDERED),false);

		this.repositoryList = stream
								.map(repository -> new RepositoryModel(repository))
								.limit(10)
								.collect(toList());
	}

	public List<RepositoryModel> getRepositoryList(){
		return repositoryList;
	}

	public String getQuery() {
		return query;
	}
}
