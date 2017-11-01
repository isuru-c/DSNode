package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.Message;
import dsnode.model.NeighbourTable;
import dsnode.model.Node;
import dsnode.net.ConnectionHandler;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Isuru Chandima
 */
@SuppressWarnings("InfiniteLoopStatement")
class MessageProcessor extends Thread {

    private static Logger logger = new Logger();

    private ConnectionHandler connectionHandler;
    private NeighbourTable neighbourTable;
    private FileHandler fileHandler;

    private Node localNode;

    MessageProcessor(ConnectionHandler connectionHandler, NeighbourTable neighbourTable, FileHandler fileHandler, Node serverNode) {
        this.connectionHandler = connectionHandler;
        this.neighbourTable = neighbourTable;
        this.fileHandler = fileHandler;

        this.localNode = connectionHandler.getLocalNode();

        String regMessage = String.format("REG %s %d %s", localNode.getIp(), localNode.getPort(), localNode.getNodeName());
        regMessage = String.format("%04d %s", (regMessage.length() + 5), regMessage);

        logger.log(String.format("Register request message to BS [%s]", regMessage));
        connectionHandler.sendMessage(regMessage, serverNode);
        logger.log("Register request message is sent to BS");
    }

    @Override
    public void run() {

        while (true) {
            try {

                Message messageObject = connectionHandler.receiveMessage();

                String message = messageObject.getMessage();
                Node sourceNode = messageObject.getSourceNode();

                logger.log(String.format("Message received [%s]", message));

                StringTokenizer st = new StringTokenizer(message, " ");

                st.nextToken(); // Length of the message
                String command = st.nextToken();

                if (st.countTokens() == 0) {
                    continue;
                }

                if ("REGOK".equals(command)) {
                    handleREGOK(st);
                } else if ("UNROK".equals(command)) {
                    handleUNROK(st);
                } else if ("LEAVEOK".equals(command)) {
                    handleLEAVEOK(st, sourceNode);
                } else if ("LEAVE".equals(command)) {
                    handleLEAVE(st);
                } else if ("JOIN".equals(command)) {
                    handleJOIN(st);
                } else if ("JOINOK".equals(command)) {
                    handleJOINOK(st, sourceNode);
                } else if ("NAME".equals(command)) {
                    handleNAME(st, sourceNode);
                } else if ("NAMEOK".equals(command)) {
                    handleNAMEOK(st);
                } else if ("HELLO".equals(command)) {
                    handleHELLO(st);
                } else if ("HELLOOK".equals(command)) {
                    handleHELLOOK(st);
                } else if ("SER".equals(command)) {
                    handleSER(st);
                } else if ("SEROK".equals(command)) {
                    handleSEROK(st);
                } else if ("ERROR".equals(command)) {
                    handleERROR(st);
                } else {
                    // Do something in here
                    logger.log(String.format("Unrecognized command found : [%s]", command));
                }

                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log("MessageProcessor thread interrupted.");
            }
        }

    }

    private void handleREGOK(StringTokenizer tokenizeMessage) {

        String no_nodes = tokenizeMessage.nextToken();

        if ("9999".equals(no_nodes)) {
            // 9999 – failed, there is some error in the command
            logger.log("Error:9999 – failed, there is some error in the command");

        } else if ("9998".equals(no_nodes)) {
            // 9998 – failed, already registered to you, unregister first
            logger.log("Error:9998 – failed, already registered to you, unregister first");

        } else if ("9997".equals(no_nodes)) {
            // 9997 – failed, registered to another user, try a different IP and port
            logger.log("Error:9997 – failed, registered to another user, try a different IP and port");

        } else if ("9996".equals(no_nodes)) {
            // 9996 – failed, can’t register. BS full.
            logger.log("Error:9996 – failed, can’t register. BS full.");

        } else {
            // No error, no_nodes indicate the number of nodes given by the BS

            int numOfNodes = Integer.valueOf(no_nodes);

            if (numOfNodes == 0) {
                // No node is given, this is the first node in the network.

                logger.log("No detail about node is received from BS. This is the first node in the network.");

            } else {
                // numOfNodes number of nodes given by the server. Select random 2 nodes
                // and return details of those nodes.

                logger.log(numOfNodes + " nodes received from BS");

                int count = 1;

                Node localNode = connectionHandler.getLocalNode();

                String joinMessage = String.format("JOIN %s %d", localNode.getIp(), localNode.getPort());
                joinMessage = String.format("%04d %s", (joinMessage.length() + 5), joinMessage);
                logger.log(String.format("JOIN request created [%s]", joinMessage));

                while (count <= numOfNodes) {
                    String nodeIp = tokenizeMessage.nextToken();
                    int nodePort = Integer.valueOf(tokenizeMessage.nextToken());

                    Node node = new Node(nodeIp, nodePort);

                    logger.log(String.format("Joining request sent to node [%s-%d]", nodeIp, nodePort));
                    connectionHandler.sendMessage(joinMessage, node);

                    node.setStatus(Node.INITIAL_STATUS);
                    neighbourTable.addNeighbour(node);

                    count++;
                }
            }
        }

    }

    private void handleUNROK(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for JOIN request");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful unregister from the BS server
            logger.log("Successfully unregistered from the BS server");

        } else if ("9999".equals(value)) {
            // Error while unregister from BS server
            logger.log("Error in JOINOK message response...!");
        }

    }

    private void handleLEAVE(StringTokenizer tokenizeMessage) {

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

        connectionHandler.sendMessage(leaveResponseMessage, neighbour);
    }

    private void handleLEAVEOK(StringTokenizer tokenizeMessage, Node sourceNode) {

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

    private void handleJOIN(StringTokenizer tokenizeMessage) {

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

        connectionHandler.sendMessage(responseMessage, newNeighbour);

        if (value == 0) {
            newNeighbour.setStatus(Node.ACTIVE_STATUS);
            newNeighbour.restLastActive();
            neighbourTable.addNeighbour(newNeighbour);
            logger.log(String.format("New neighbour added [%s-%d]", newNodeIp, newNodePort));

            // Send name request for the new node
            String nameRequest = String.format("NAME %s %d", newNodeIp, newNodePort);
            nameRequest = String.format("%04d %s", (nameRequest.length() + 5), nameRequest);

            connectionHandler.sendMessage(nameRequest, newNeighbour);
            logger.log(String.format("NAME request sent [%s-%d]", newNodeIp, newNodePort));
        }
    }

    private void handleJOINOK(StringTokenizer tokenizeMessage, Node sourceNode) {

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

        connectionHandler.sendMessage(nameRequest, sourceNode);
        logger.log(String.format("NAME request sent [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));
    }

    private void handleNAME(StringTokenizer tokenizeMessage, Node sourceNode) {

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
        String nameResponse = String.format("NAMEOK %s %d %s", nodeIp, nodePort, connectionHandler.getLocalNode().getNodeName());
        nameResponse = String.format("%04d %s", (nameResponse.length() + 5), nameResponse);

        connectionHandler.sendMessage(nameResponse, sourceNode);
        logger.log(String.format("NAMEOK respond sent to [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));

    }

    private void handleNAMEOK(StringTokenizer tokenizeMessage) {

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

    private void handleHELLO(StringTokenizer tokenizeMessage) {

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

            String helloOkMessage = String.format("HELLOOK %s %d %s %d", targetNode.getIp(), targetNode.getPort(), neighbourNode.getIp(), neighbourNode.getPort());
            helloOkMessage = String.format("%04d %s", (helloOkMessage.length() + 5), helloOkMessage);

            connectionHandler.sendMessage(helloOkMessage, neighbourNode);
        }
    }

    private void handleHELLOOK(StringTokenizer tokenizeMessage) {

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

    private void handleSER(StringTokenizer tokenizeMessage) {

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        String fileName = tokenizeMessage.nextToken();

        while (tokenizeMessage.countTokens() > 1) {
            fileName = fileName + " " + tokenizeMessage.nextToken();
        }

        int hops = Integer.valueOf(tokenizeMessage.nextToken()) + 1;

        ArrayList<String> localFileList = fileHandler.searchFiles(fileName);
        Node localNode = connectionHandler.getLocalNode();

        int nof = localFileList.size();

        String searchResponse = String.format("SEROK %d %s %d %d", nof, localNode.getIp(), localNode.getPort(), hops);
        for (String file : localFileList) {
            searchResponse = String.format("%s %s", searchResponse, file);
        }
        searchResponse = String.format("%04d %s", (searchResponse.length() + 5), searchResponse);

        connectionHandler.sendMessage(searchResponse, requestNode);

        // Forward the search request using random walk


    }

    private void handleSEROK(StringTokenizer tokenizeMessage) {

        int nof = Integer.valueOf(tokenizeMessage.nextToken());

        if (nof > 0) {
            String responseIp = tokenizeMessage.nextToken();
            int responsePort = Integer.valueOf(tokenizeMessage.nextToken());
            int hops = Integer.valueOf(tokenizeMessage.nextToken());

            System.out.println("Local search result for file ------ ");
            while (tokenizeMessage.hasMoreElements()) {
                System.out.println(String.format("\t\t[%s-%d]\t%s", responseIp, responsePort, tokenizeMessage.nextToken()));
            }
        }
    }

    private void handleERROR(StringTokenizer tokenizeMessage) {

    }
}
