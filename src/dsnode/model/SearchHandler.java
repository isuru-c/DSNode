package dsnode.model;

import dsnode.model.data.SearchResultSet;

import java.util.ArrayList;

/**
 * @author Isuru Chandima
 */
public class SearchHandler {

    private String currentSearch;
    private int searchCount;

    private ArrayList<SearchResultSet> searchResultSets;

    public void newSearch(String searchName) {
        this.currentSearch = searchName;
        searchResultSets = new ArrayList<>();
        searchCount = 0;
    }

    public void addSearchResult(SearchResultSet searchResultSet, String searchName) {
        if (this.currentSearch.equals(searchName)) {
            searchResultSets.add(searchResultSet);
            searchCount++;
        }
    }

    public ArrayList<SearchResultSet> getSearchResultSets() {
        return searchResultSets;
    }
}
