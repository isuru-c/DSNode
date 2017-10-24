package dsnode;

import dsnode.model.Message;
import dsnode.model.NeighbourTable;
import dsnode.model.Node;

import java.util.StringTokenizer;

/**
 * @author Isuru Chandima
 */
@SuppressWarnings("InfiniteLoopStatement")
class MessageReceiver extends Thread {

    private static Logger logger = new Logger();

    private SocketController socketController;
    private NeighbourTable neighbourTable;

    MessageReceiver(SocketController socketController, NeighbourTable neighbourTable) {
        this.socketController = socketController;
        this.neighbourTable = neighbourTable;
    }

    @Override
    public void run() {

        while (true) {
            try {

                Message messageObject = socketController.receiveMessage();

                String message = messageObject.getMessage();
                Node sourceNode = messageObject.getSourceNode();

                logger.log(String.format("Message received [%s]", message));

                StringTokenizer st = new StringTokenizer(message, " ");

                st.nextToken(); // Length of the message
                String command = st.nextToken();

                if (st.countTokens() == 0) {
                    Thread.sleep(100);
                    continue;
                }

                if ("REGOK".equals(command)) {
                    handleREGOK(st);
                } else if ("UNROK".equals(command)) {
                    handleUNROK(st);
                } else if ("LEAVEOK".equals(command)) {
                    handleLEAVEOK(st);
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
                logger.log("MessageReceiver thread interrupted.");
            }
        }

    }

    private void handleREGOK(StringTokenizer tokenizeMessage) {

    }

    private void handleUNROK(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log(String.format("Incomplete message for JOIN request []"));
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
            logger.log(String.format("Neighbour [] leave the system", neighbourTable.getNeighbourNode(neighbour).getNodeName()));
            neighbourTable.removeNeighbour(neighbour);
        } else {
            value = 9999;
            logger.log(String.format("LEAVE request from non-existing neighbours [%s-%d]", nodeIp, nodePort));
        }

        // Render and send LEAVEOK message for the leaving neighbour
        String leaveResponseMessage = String.format("LEAVEOK %d", value);
        leaveResponseMessage = String.format("%04d %s", (leaveResponseMessage.length() + 5), leaveResponseMessage);

        socketController.sendMessage(leaveResponseMessage, neighbour);
    }

    private void handleLEAVEOK(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for LEAVEOK response...!");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful leaving in remote node, remove neighbour node from the routing table

        } else if ("9999".equals(value)) {
            // Error while leaving from neighbour node
            logger.log("Error in LEAVEOK message response...!");
        }

    }

    private void handleJOIN(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 2) {
            logger.log(String.format("Incomplete message for JOIN request []"));
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

        socketController.sendMessage(responseMessage, newNeighbour);

        if (value == 0) {
            newNeighbour.setStatus(Node.ACTIVE_STATUS);
            newNeighbour.restLastSeen();
            neighbourTable.addNeighbour(newNeighbour);
            logger.log(String.format("New neighbour added [%s-%d]", newNodeIp, newNodePort));

            // Send name request for the new node
            String nameRequest = String.format("NAME %s %d", newNodeIp, newNodePort);
            nameRequest = String.format("%04d %s", (nameRequest.length() + 5), nameRequest);

            socketController.sendMessage(nameRequest, newNeighbour);
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
                node.restLastSeen();
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

        socketController.sendMessage(nameRequest, sourceNode);
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
        String nameResponse = String.format("NAMEOK %s %d %s", nodeIp, nodePort, socketController.getLocalNode().getNodeName());
        nameResponse = String.format("%04d %s", (nameResponse.length() + 5), nameResponse);

        socketController.sendMessage(nameResponse, sourceNode);
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

    private void handleSER(StringTokenizer tokenizeMessage) {

    }

    private void handleSEROK(StringTokenizer tokenizeMessage) {

    }

    private void handleERROR(StringTokenizer tokenizeMessage) {

    }
}
