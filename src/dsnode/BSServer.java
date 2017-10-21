package dsnode;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Isuru Chandima
 */
class BSServer {

    private static Logger logger = new Logger();

    private Node serverNode;
    private Node localNode;

    private SocketController socketController;

    /**
     * @param serverNode Node object containing the details of the Bootstrap Server
     * @param localNode Node object containing the details of this node given by the user
     * @param socketController SocketController object used to access socket connection
     */
    BSServer(Node serverNode, Node localNode, SocketController socketController) {

        this.serverNode = serverNode;
        this.localNode = localNode;

        this.socketController = socketController;
    }

    /**
     * This method is used to register new node with the Bootstrap Server.
     * First register request message is sent to the BS and get the register respond
     * message back from the BS.
     * After extracting details about nodes given by the BS, return the list of nodes
     *
     * @return arrayList of Node containing nodes given by the Bootstrap Server
     */
    ArrayList<Node> getNodeList() {

        ArrayList<Node> nodeList = new ArrayList<>();

        String regMessage = String.format("REG %s %d %s", localNode.getIp(), localNode.getPort(), localNode.getUserName());
        regMessage = String.format("%04d %s", (regMessage.length() + 5), regMessage);

        logger.log("Register request message to BS [" + regMessage + "]");
        socketController.sendMessage(regMessage, serverNode);
        logger.log("Register request message is sent to BS");

        String replyMsg = socketController.receiveMessage();
        logger.log("Register response message received from BS [" + replyMsg + "]");

        StringTokenizer st = new StringTokenizer(replyMsg, " ");

        // Get first two tokens to remove length and comman parts from the message
        // String length = st.nextToken();
        // String command = st.nextToken();

        st.nextToken();
        st.nextToken();
        String no_nodes = st.nextToken();

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

                return nodeList;
            } else {
                // numOfNodes number of nodes given by the server. Select random 2 nodes
                // and return details of those nodes.

                logger.log(numOfNodes + " nodes received from BS");

                int count = 1;

                while (count <= numOfNodes) {
                    String nodeIp = st.nextToken();
                    int nodePort = Integer.valueOf(st.nextToken());

                    nodeList.add(new Node(nodeIp, nodePort));

                    logger.log(String.format("Node %d : %s %d", count, nodeIp, nodePort));
                    count++;
                }

                return nodeList;
            }
        }

        return nodeList;
    }
}
