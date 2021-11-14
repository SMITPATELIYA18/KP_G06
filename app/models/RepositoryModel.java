package models;

import java.util.ArrayList;
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


public class RepositoryModel {
	private CompletableFuture<String> ownerName;
	private CompletableFuture<String> repositoryName;
	private CompletableFuture<List<String>> topics;
	
	public RepositoryModel(JsonNode data) {

		this.ownerName = CompletableFuture.supplyAsync(() -> data.get("owner").get("login").asText());
		this.repositoryName = CompletableFuture.supplyAsync(() -> data.get("name").asText());

		this.topics = CompletableFuture.supplyAsync(() -> {
			List<String> topicList = new ArrayList<>();
			ArrayNode items = (ArrayNode) data.get("topics");
			java.util.Iterator<JsonNode> iteratorItems = items.elements();
			while(iteratorItems.hasNext()) {
				JsonNode item = iteratorItems.next();
				topicList.add(item.asText());
			}
			return topicList.stream().limit(5).collect(toList());
		});

	}
	
	public List<String> getTopics() throws ExecutionException, InterruptedException {
		return topics.get();
	}

	public String getRepositoryName() throws ExecutionException, InterruptedException {
		return repositoryName.get();
	}

	public String getOwnerName() throws ExecutionException, InterruptedException {
		return ownerName.get();
	}

	public String toString() {
		String string = "";
		try {
			string = ownerName.get() + repositoryName.get() + topics.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return string;
	}
	
}
