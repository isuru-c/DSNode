package dsnode.rmi;

import dsnode.MessageProcessor;
import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.SearchHandler;
import dsnode.model.data.Node;
import dsnode.model.data.SearchResultSet;
import dsnode.net.ConnectionHandler;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Sugeesh Chandraweera
 */
public class NodeController extends UnicastRemoteObject implements NodeControllerInterface {
    private Node localNode;
    private NeighbourTable neighbourTable;
    private FileHandler fileHandler;
    private int maxNumOfHops;
    private SearchHandler searchHandler;

    public NodeController() throws RemoteException {
        super();
    }

    public NodeController(NeighbourTable neighbourTable, FileHandler fileHandler, SearchHandler searchHandler) throws RemoteException {
        super();
        this.neighbourTable = neighbourTable;
        this.fileHandler = fileHandler;
        this.searchHandler = searchHandler;
        this.maxNumOfHops = MessageProcessor.maxNumOfHops;
    }

    @Override
    public Node getNode() throws ClassNotFoundException, RemoteException {
        return this.localNode;
    }

    @Override
    public boolean setNode(Node node) throws ClassNotFoundException, RemoteException {
        this.localNode = node;
        return true;
    }

    @Override
    public String searchItem(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = new StringTokenizer(message, " ");

        tokenizeMessage.nextToken(); // Length of the message, can be assigned to a variable if it is needed
        String command = tokenizeMessage.nextToken();


        if (neighbourTable.isExistingNeighbour(sourceNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(sourceNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);
        }

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        String searchId = tokenizeMessage.nextToken();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tokenizeMessage.nextToken());

        while (tokenizeMessage.countTokens() > 1) {
            stringBuilder.append(" ");
            stringBuilder.append(tokenizeMessage.nextToken());
        }

        String fileName = stringBuilder.toString();

        int hops = Integer.valueOf(tokenizeMessage.nextToken()) + 1;

        if (hops > maxNumOfHops) {
            // SER message has reached the maximum number of hops it can be forwarded.
            return null;
        }

        if (fileHandler.isNewSearchId(searchId)) {
            ArrayList<String> localFileList = fileHandler.searchFiles(fileName);

            int nof = localFileList.size();

            String searchResponse = String.format("SEROK %s %d %s %d %d", fileName.replace(' ', '_'), nof, localNode.getIp(), localNode.getPort(), hops);
            for (String file : localFileList) {
                searchResponse = String.format("%s %s", searchResponse, file);
            }
            searchResponse = String.format("%04d %s", (searchResponse.length() + 5), searchResponse);

            // create connection to server
            sourceNode.setNodeController(sourceNode.getNodeControlerForNode());
            sourceNode.getNodeController().searchFound(searchResponse,requestNode);
//            connectionHandler.sendMessage(searchResponse, requestNode);
        }

        // Forward the search request using random walk

        Node randomNode = neighbourTable.getRandomNeighbour(sourceNode);

        if (randomNode == null)
            return null;

        String searchRequest = String.format("SER %s %d %s %s %d", requestIp, requestPort, searchId, fileName, hops);
        searchRequest = String.format("%04d %s", (searchRequest.length() + 5), searchRequest);

        randomNode.getNodeController().searchFound(searchRequest,randomNode);
//        connectionHandler.sendMessage(searchRequest, randomNode);
        return null;
    }

    @Override
    public String searchFound(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = new StringTokenizer(message, " ");

        tokenizeMessage.nextToken(); // Length of the message, can be assigned to a variable if it is needed
        String command = tokenizeMessage.nextToken();


        if (neighbourTable.isExistingNeighbour(sourceNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(sourceNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);
        }

        String searchFileName = tokenizeMessage.nextToken();

        int nof = Integer.valueOf(tokenizeMessage.nextToken());

        if (nof > 0) {
            String responseIp = tokenizeMessage.nextToken();
            int responsePort = Integer.valueOf(tokenizeMessage.nextToken());

            Node responseNode = new Node(responseIp, responsePort);
            if (neighbourTable.isExistingNeighbour(responseNode))
                responseNode = neighbourTable.getNeighbourNode(responseNode);

            int hopCount = Integer.valueOf(tokenizeMessage.nextToken());

            String[] fileNames = new String[nof];
            int count = 0;

            while (tokenizeMessage.hasMoreElements()) {
                fileNames[count] = tokenizeMessage.nextToken();
                count++;
            }
            searchHandler.addSearchResult(new SearchResultSet(responseNode, fileNames, hopCount), searchFileName);
        }
        return null;
    }


}
