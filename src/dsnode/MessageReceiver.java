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
                } else if ("JOIN".equals(command)) {
                    handleJOIN(st);
                } else if ("JOINOK".equals(command)) {
                    handleJOINOK(st, sourceNode);
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

    }

    private void handleLEAVEOK(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for JOIN request...!");
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

        String newNodeIp = tokenizeMessage.nextToken();
        int newNodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node newNeighbour = new Node(newNodeIp, newNodePort);

        int value = 0;

        String responseMessage = String.format("JOINOK %d", value);
        responseMessage = String.format("%04d %s", (responseMessage.length() + 5), responseMessage);

        socketController.sendMessage(responseMessage, newNeighbour);

        newNeighbour.setStatus(Node.ACTIVE_STATUS);
        neighbourTable.addNeighbour(newNeighbour);
        logger.log(String.format("New neighbour added [%s-%d]", newNodeIp, newNodePort));

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
            node.setStatus(Node.ACTIVE_STATUS);
        } else if ("9999".equals(value)) {
            // Error while adding new node to routing table
            logger.log("Error in JOINOK message response...!");
        }
    }

    private void handleSER(StringTokenizer tokenizeMessage) {

    }

    private void handleSEROK(StringTokenizer tokenizeMessage) {

    }

    private void handleERROR(StringTokenizer tokenizeMessage) {

    }

    private boolean validateSource(Node sourceNode) {
        return true;
    }
}
