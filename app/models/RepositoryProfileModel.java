package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class RepositoryProfileModel {
	private String ownerName;
	private String repositoryName;
	private List<String> topics = new ArrayList<>();
	//Extra
	private String repositoryDescription;
	private String type;
	private String createDate;
	private String updatedDate;
	private String lastPushDate;
	private String cloneUrl;
	private String language;
	private boolean visibility;
	private int watcherCount;
	private boolean hasIssues;
	private LinkedHashMap<String, Long> issueList = new LinkedHashMap<>();

	//Optimize
	public RepositoryProfileModel(JsonNode data) {
		this.ownerName = data.get("owner").get("login").asText();
		this.repositoryName = data.get("name").asText();
		this.repositoryDescription = data.get("description").asText();
		this.type = data.get("owner").get("type").asText();
		this.createDate = data.get("created_at").asText();
		this.updatedDate = data.get("updated_at").asText();
		this.lastPushDate = data.get("pushed_at").asText();
		this.cloneUrl = data.get("clone_url").asText();
		this.language = data.get("language").asText();
		this.visibility = data.get("visibility").asBoolean();
		this.watcherCount = data.get("watchers_count").asInt();
		this.hasIssues = data.get("has_issues").asBoolean();

		//TODO: Optimize
		ArrayNode items = (ArrayNode) data.get("topics");
		java.util.Iterator<JsonNode> iteratorItems = items.elements();
		while(iteratorItems.hasNext()) {
			JsonNode item = iteratorItems.next();
			topics.add(item.asText());
		}
		topics = topics.stream().limit(5).collect(toList());
		/*if(this.hasIssues){
			setIssueList(this.repositoryName, data);
		}*/
	}

	private LinkedHashMap<String, Long> setIssueList(String repositoryName, JsonNode data){
		this.issueList = new IssueModel(repositoryName, data).getWordLevelData();
		return this.issueList;
	}
	public String getOwnerName(){
		return this.ownerName;
	}
	public String getRepositoryName(){
		return this.repositoryName;
	}
	public String getRepositoryDescription(){
		return this.repositoryDescription;
	}
	public String getType(){
		return this.type;
	}
	public String getCreateDate(){
		return this.createDate;
	}
	public String getUpdatedDate(){
		return this.updatedDate;
	}
	public String getLastPushDate(){
		return this.lastPushDate;
	}
	public String getCloneUrl(){
		return this.cloneUrl;
	}
	public String getLanguage(){
		return this.language;
	}
	public boolean getVisibility(){
		return this.visibility;
	}
	public int getWatcherCount(){
		return this.watcherCount;
	}
	public boolean getHasIssues(){
		return this.hasIssues;
	}

	public List<String> getTopics() {
		return topics;
	}
	public String toString() {
		String response = "Owner Name: " + this.ownerName
				+ "\n Repository Name: " + this.repositoryName
				+ "\n Repository Description: " + this.repositoryDescription
				+ "\n Type: " + this.type
				+ "\n Creation Date: " + this.createDate
				+ "\n Updating Date: " + this.updatedDate
				+ "\n Last Push Date: " + this.lastPushDate
				+ "\n Clone URL: " + this.cloneUrl
				+ "\n Language: " + this.language
				+ "\n Visibility: " + this.visibility
				+ "\n Watcher Count: " + this.watcherCount
				+ "\n Has Issues: " + this.hasIssues;
		return response;
	}
}
