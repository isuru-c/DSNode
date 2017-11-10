package dsnode.rmi;

import dsnode.Logger;
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
@SuppressWarnings("Duplicates")
public class NodeController extends UnicastRemoteObject implements NodeControllerInterface {
    private Node localNode;
    private NeighbourTable neighbourTable;
    private FileHandler fileHandler;
    private int maxNumOfHops;
    private SearchHandler searchHandler;
    private static Logger logger = new Logger();

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
    public void searchItem(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);
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
            return;
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
            requestNode.setNodeController(requestNode.getNodeControlerForNode());
            requestNode.getNodeController().searchFound(searchResponse,localNode);

//            sourceNode.setNodeController(sourceNode.getNodeControlerForNode());
//            sourceNode.getNodeController().searchFound(searchResponse,requestNode);
//            connectionHandler.sendMessage(searchResponse, requestNode);
        }

        // Forward the search request using random walk

        Node randomNode = neighbourTable.getRandomNeighbour(sourceNode);

        if (randomNode == null)
            return;

        String searchRequest = String.format("SER %s %d %s %s %d", requestIp, requestPort, searchId, fileName, hops);
        searchRequest = String.format("%04d %s", (searchRequest.length() + 5), searchRequest);

        randomNode.getNodeController().searchItem(searchRequest,localNode);
//        connectionHandler.sendMessage(searchRequest, randomNode);
        return;
    }

    @Override
    public void searchFound(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

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
        return;
    }

    @Override
    public void joinAsNeighbour(String message) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 2) {
            logger.log("Incomplete message for JOIN request");
            return;
        }

        // Create Node object for the new neighbour
        String newNodeIp = tokenizeMessage.nextToken();
        int newNodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node newNeighbour = new Node(newNodeIp, newNodePort);

        int value = 0;

        if (neighbourTable.isExistingNeighbour(newNeighbour)) {
            value = 9999;
            logger.log(String.format("JOIN request from existing neighbours [%s-%d]", newNodeIp, newNodePort));
        }

        // Render and send JOINOK message for the new neighbour
        String responseMessage = String.format("JOINOK %d", value);
        responseMessage = String.format("%04d %s", (responseMessage.length() + 5), responseMessage);

        newNeighbour.setNodeController(newNeighbour.getNodeControlerForNode());
        newNeighbour.getNodeController().joinResponse(responseMessage,localNode);
//        connectionHandler.sendMessage(responseMessage, newNeighbour);

        if (value == 0) {
            newNeighbour.setStatus(Node.ACTIVE_STATUS);
            newNeighbour.restLastActive();
            neighbourTable.addNeighbour(newNeighbour);
            logger.log(String.format("New neighbour added [%s-%d]", newNodeIp, newNodePort));
            System.out.print(String.format("New neighbour added [%s-%d]\n# ", newNeighbour.getIp(),newNeighbour.getPort()));

            // Send name request for the new node
            String nameRequest = String.format("NAME %s %d", newNodeIp, newNodePort);
            nameRequest = String.format("%04d %s", (nameRequest.length() + 5), nameRequest);

            newNeighbour.getNodeController().getName(nameRequest,localNode);
//            connectionHandler.sendMessage(nameRequest, newNeighbour);
            logger.log(String.format("NAME request sent [%s-%d]", newNodeIp, newNodePort));
        }
    }

    @Override
    public void joinResponse(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for JOINOK respond...!");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful neighbour adding in remote node, add node to the routing table
            Node node = neighbourTable.getNeighbourNode(sourceNode);

            if (node != null) {
                node.setStatus(Node.ACTIVE_STATUS);
                node.restLastActive();
            } else {
                logger.log(String.format("Different IP/Port parameters in message and data packet [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));
                return;
            }

        } else if ("9999".equals(value)) {
            // Error while adding new node to routing table
            logger.log("Error in JOINOK message response...!");
            return;
        }

        // Send name request for the new node
        String nameRequest = String.format("NAME %s %d", sourceNode.getIp(), sourceNode.getPort());
        nameRequest = String.format("%04d %s", (nameRequest.length() + 5), nameRequest);


        sourceNode.setNodeController(sourceNode.getNodeControlerForNode());
        sourceNode.getNodeController().getName(nameRequest,localNode);

//        connectionHandler.sendMessage(nameRequest, sourceNode);
        logger.log(String.format("NAME request sent [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));
    }

    private StringTokenizer getCorrectToken(String message){
        StringTokenizer tokenizeMessage = new StringTokenizer(message, " ");

        tokenizeMessage.nextToken(); // Length of the message, can be assigned to a variable if it is needed
        String command = tokenizeMessage.nextToken();
        return tokenizeMessage;

    }

    @Override
    public void getName(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 2) {
            logger.log("Incomplete message for NAME request...!");
            return;
        }

        String nodeIp = tokenizeMessage.nextToken();
        int nodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node node = new Node(nodeIp, nodePort);

        // Check whether NAME request came from knowing neighbour
        if (!neighbourTable.isLocalNode(node))
            return;

        // Send NAMEOK respond to the NAME requester
        String nameResponse = String.format("NAMEOK %s %d %s", nodeIp, nodePort, neighbourTable.getLocalNode().getNodeName());
        nameResponse = String.format("%04d %s", (nameResponse.length() + 5), nameResponse);

        sourceNode.setNodeController(sourceNode.getNodeControlerForNode());
        sourceNode.getNodeController().nameResponse(nameResponse,localNode);
//        connectionHandler.sendMessage(nameResponse, sourceNode);
        logger.log(String.format("NAMEOK respond sent to [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));

    }

    @Override
    public void nameResponse(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 3) {
            logger.log("Incomplete message for NAMEOK response...!");
            return;
        }

        String nodeIp = tokenizeMessage.nextToken();
        int nodePort = Integer.valueOf(tokenizeMessage.nextToken());
        String nodeName = tokenizeMessage.nextToken();
        Node node = new Node(nodeIp, nodePort, nodeName);

        if (neighbourTable.isExistingNeighbour(node)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(node);
            neighbourNode.setNodeName(nodeName);
            logger.log(String.format("Name updated in the node [%s-%d]", nodeIp, nodePort));
        }
    }

    @Override
    public void leaveAsNeighbour(String message) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 2) {
            logger.log("Incomplete message for LEAVE request...!");
            return;
        }

        // Create Node object for the leaving node
        String nodeIp = tokenizeMessage.nextToken();
        int nodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node neighbour = new Node(nodeIp, nodePort);

        int value = 0;

        if (neighbourTable.isExistingNeighbour(neighbour)) {
            logger.log(String.format("Neighbour [%s] leave the system", neighbourTable.getNeighbourNode(neighbour).getNodeName()));
            neighbourTable.removeNeighbour(neighbour);
        } else {
            value = 9999;
            logger.log(String.format("LEAVE request from non-existing neighbours [%s-%d]", nodeIp, nodePort));
        }

        // Render and send LEAVEOK message for the leaving neighbour
        String leaveResponseMessage = String.format("LEAVEOK %d", value);
        leaveResponseMessage = String.format("%04d %s", (leaveResponseMessage.length() + 5), leaveResponseMessage);

        neighbour.setNodeController(neighbour.getNodeControlerForNode());
        neighbour.getNodeController().leaveResponse(leaveResponseMessage,localNode);

//        connectionHandler.sendMessage(leaveResponseMessage, neighbour);
    }

    @Override
    public void leaveResponse(String message, Node sourceNode) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for LEAVEOK response...!");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful leaving in remote node, remove neighbour node from the routing table
            neighbourTable.removeNeighbour(sourceNode);

        } else if ("9999".equals(value)) {
            // Error while leaving from neighbour node
            logger.log("Error in LEAVEOK message response...!");
        }

    }

    @Override
    public void hello(String message) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 4) {
            logger.log("Incomplete message for HELLO request...!");
            return;
        }

        String targetIp = tokenizeMessage.nextToken();
        int targetPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node targetNode = new Node(targetIp, targetPort);

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        if (neighbourTable.isLocalNode(targetNode) && neighbourTable.isExistingNeighbour(requestNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(requestNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);

            String helloOkMessage = String.format("HELLOOK %s %d %s %d", neighbourNode.getIp(), neighbourNode.getPort(), targetNode.getIp(), targetNode.getPort());
            helloOkMessage = String.format("%04d %s", (helloOkMessage.length() + 5), helloOkMessage);

            neighbourNode.getNodeController().helloResponse(helloOkMessage);
//            connectionHandler.sendMessage(helloOkMessage, neighbourNode);
        }
    }

    @Override
    public void helloResponse(String message) throws ClassNotFoundException, RemoteException {
        StringTokenizer tokenizeMessage = getCorrectToken(message);

        if (tokenizeMessage.countTokens() < 4) {
            logger.log("Incomplete message for HELLOOK response...!");
            return;
        }

        String targetIp = tokenizeMessage.nextToken();
        int targetPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node targetNode = new Node(targetIp, targetPort);

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        if (neighbourTable.isLocalNode(targetNode) && neighbourTable.isExistingNeighbour(requestNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(requestNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);
        }
    }


}
