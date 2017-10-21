package dsnode;

import java.util.ArrayList;

/**
 *
 * @author Isuru Chandima
 */
public class DSNode {

    private static Logger logger = new Logger();

    public static void main(String[] args) {

        logger.useLogger(true);

        String BServerIp = "127.0.0.1";
        int BServerPort = 55555;

        String localName = "ic2";

        SocketController socketController = new SocketController(localName);
        Node localNode = socketController.getLocalNode();
        Node serverNode = new Node(BServerIp, BServerPort);

        BSServer bServer = new BSServer(serverNode, localNode, socketController);
        ArrayList<Node> nodeList = bServer.getNodeList();

        ArrayList<Node> neighbourList = new ArrayList<>();

        if (nodeList.size() == 0) {
            // No other node, this is the first node of the network
            logger.log("No node given by the BS. This is the first node in system.");

        } else {
            // There are other nodes in the network, join with them

            for (Node node : nodeList) {

                String joinMessage = String.format("JOIN %s %d", localNode.getIp(), localNode.getPort());
                joinMessage = String.format("%04d %s", (joinMessage.length() + 5), joinMessage);

                logger.log(String.format("Joining to a node [%s]", joinMessage));
                socketController.sendMessage(joinMessage, node);

            }
        }


    }
}