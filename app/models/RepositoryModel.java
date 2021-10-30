package models;

import java.util.ArrayList;

public class RepositoryModel {
	private String ownerName;
	private String repositoryName;
	private ArrayList<String> topics;
	
	public RepositoryModel(String ownerName,String repositoryName, ArrayList<String> topics) {
		this.ownerName = ownerName;
		this.repositoryName = repositoryName;
		this.topics = topics;
	}
	
	public ArrayList<String> getTopics() {
		return topics;
	}
	public void setTopics(ArrayList<String> topics) {
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
	
}
