package models;

import java.util.ArrayList;
import java.util.List;

/**
 * This model class stores different search result in the cache.
 * @author smitp
 *
 */

public class SearchCacheStore {
	private List<SearchRepository> searches;
	
	public SearchCacheStore() {
		searches = new ArrayList<>();
	}
	
	public void addNewSearch(SearchRepository searchRepository) {
		this.searches.add(searchRepository);
	}

	public List<SearchRepository> getSearches() {
		return searches;
	}

	public void setSearches(List<SearchRepository> searches) {
		this.searches = searches;
	}

}
