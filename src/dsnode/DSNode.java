package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.Node;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Isuru Chandima
 */
public class DSNode {

    private static Logger logger = new Logger();

    public static void main(String[] args) {

        DSNode dsNode = new DSNode();
        dsNode.startNode();

    }

    void startNode() {
        logger.useLogger(true);

        Scanner scanner = new Scanner(System.in);

        String bServerIp = "127.0.0.1";
        int bServerPort = 55555;

        System.out.print("Enter the name of the node: ");
        String localName = scanner.next();

        SocketController socketController = new SocketController(localName);

        Node localNode = socketController.getLocalNode();
        Node serverNode = new Node(bServerIp, bServerPort);

        BSServer bServer = new BSServer(serverNode, localNode, socketController);
        ArrayList<Node> nodeList = bServer.getNodeList();

        FileHandler fileHandler = new FileHandler();

        NeighbourTable neighbourTable = new NeighbourTable(localNode);

        MessageReceiver messageReceiver = new MessageReceiver(socketController, neighbourTable);
        messageReceiver.start();

        ConsoleListener consoleListener = new ConsoleListener(neighbourTable, socketController, fileHandler, serverNode);
        consoleListener.start();

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

                node.setStatus(Node.INITIAL_STATUS);
                neighbourTable.addNeighbour(node);
            }
        }
    }
}
