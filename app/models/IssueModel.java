package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.*;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This model class is for displaying issues statistics 
 * @author smitpateliya
 *
 */
public class IssueModel {
	private String repoFullName;
	private List<String> issueTitles = new ArrayList<>();
	private LinkedHashMap<String, Long> worldLevelData = new LinkedHashMap<>();

	public IssueModel(String repoFullName, JsonNode data) {
		this.repoFullName = repoFullName;
		if(!data.has("message")) {
		java.util.Iterator<JsonNode> iteratorItems = data.elements() != null ? data.elements()
				: Collections.emptyIterator();
		System.out.println(iteratorItems.hasNext());
		iteratorItems.forEachRemaining(issue -> issueTitles.add(issue.get("title").asText()));
		Map<String, Long> unsortedData = issueTitles.stream().flatMap(title -> getIndividualWord(title))
				.collect(groupingBy(Function.identity(), counting()));
		unsortedData.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(result -> worldLevelData.put(result.getKey(), result.getValue()));
		System.out.println(worldLevelData); 
		}
		else {
			issueTitles.add("Error! Repository does not present!");
		}
	}

	private Stream<String> getIndividualWord(String title) {
		return Arrays.asList(title.split(" ")).stream();
	}

	public String getRepoFullName() {
		return repoFullName;
	}

	public List<String> getIssueTitles() {
		return issueTitles;
	}
	
	public LinkedHashMap<String,Long> getWordLevelData() {
		return this.worldLevelData;
	}

}
