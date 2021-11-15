package models;

import java.util.ArrayList;
import java.util.List;

/**
 * This model class stores different search result in the cache.
 * @author smitpateliya
 *
 */

public class SearchCacheStore {
	private List<SearchRepository> searches;
	
	public SearchCacheStore() {
		searches = new ArrayList<>();
	}
	
	public void addNewSearch(SearchRepository searchRepository) {
		this.searches.add(0, searchRepository);
	}

	public List<SearchRepository> getSearches() {
		return searches;
	}
}
