package dsnode.model.data;

/**
 *
 * @author Isuru Chandima
 */
public class SearchResultSet {

    private Node ownerNode;
    private String[] fileNames;
    private int hopCount;
    private long queryTime;

    public SearchResultSet(Node ownerNode, String[] fileNames, int hopCount) {
        this.ownerNode = ownerNode;
        this.fileNames = fileNames;
        this.hopCount = hopCount;
    }

    public Node getOwnerNode() {
        return ownerNode;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }

    public long getQueryTime() {
        return queryTime;
    }
}
