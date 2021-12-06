package models;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This is model class to handle Repositories' search.
 * @author SmitPateliya, Farheen Jamadar
 */


public class RepositoryModel {
	private String ownerName;
	private String repositoryName;
	private List<String> topics;

	// This constructor is created to help during the development process. Will be removed later if not required.
	public RepositoryModel(String ownerName, String repositoryName, List<String> topics) {
		this.ownerName = ownerName;
		this.repositoryName = repositoryName;
		this.topics = topics;
	}

	public RepositoryModel(JsonNode data) {
		this.ownerName = data.get("owner").get("login").asText();
		this.repositoryName = data.get("name").asText();

		ArrayNode items = (ArrayNode) data.get("topics");
		Stream<JsonNode> elementStream = StreamSupport.stream(Spliterators
										    .spliteratorUnknownSize(items.elements(),
													Spliterator.ORDERED),false);

		this.topics = elementStream
				.map(topic -> topic.asText())
				.limit(10)
				.collect(toList());
	}
	
	public List<String> getTopics(){
		return this.topics;
	}

	public String getRepositoryName(){
		return repositoryName;
	}

	public String getOwnerName(){
		return ownerName;
	}

	@Override
	public boolean equals(Object obj) {
		if(this.getClass() != obj.getClass()) {
			return false;
		} else {
			if(this.repositoryName.equals(((RepositoryModel) obj).repositoryName) && this.ownerName.equals(((RepositoryModel) obj).ownerName)){
				return true;
			} else {
				return false;
			}
		}
	}
	
}
