package models;

import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class RepositoryModel {
	private String ownerName;
	private String repositoryName;
	private List<String> topics = new ArrayList<>();
	
	public RepositoryModel(JsonNode data) {
		//System.out.println(data);
		this.ownerName = data.get("owner").get("login").asText();
		this.repositoryName = data.get("name").asText();
		ArrayNode items = (ArrayNode) data.get("topics");
		java.util.Iterator<JsonNode> iteratorItems = items.elements();
		while(iteratorItems.hasNext()) {
			JsonNode item = iteratorItems.next();
			topics.add(item.asText());
		}
		topics = topics.stream().limit(5).collect(toList());
	}
	
	public List<String> getTopics() {
		return topics;
	}
	public void setTopics(List<String> topics) {
		this.topics = topics;
	}
	public String getRepositoryName() {
		return repositoryName;
	}
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public String toString() {
		return ownerName + repositoryName + topics;
	}
	
}
