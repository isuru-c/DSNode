package dsnode.model;

import dsnode.model.data.Node;
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
            Node ownerNode = searchResultSet.getOwnerNode();
            System.out.println(String.format("Search Result for \"%s\": From node [%s-%d] %s", currentSearch,ownerNode.getIp(),ownerNode.getPort(),ownerNode.getNodeName()));
            for (String searchResult : searchResultSet.getFileNames()) {
                searchCount++;
                System.out.println(String.format("\t\t\t%d - %s", searchCount, searchResult));
            }
            System.out.print("\n# ");
        }
    }

    public ArrayList<SearchResultSet> getSearchResultSets() {
        return searchResultSets;
    }

    public String getCurrentSearch() {
        return currentSearch;
    }
}
