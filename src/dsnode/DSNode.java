package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.Node;
import dsnode.net.ConnectionHandler;
import dsnode.net.SocketHandler;

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

    private void startNode() {

        logger.useLogger(true);

        Scanner scanner = new Scanner(System.in);

        String bServerIp = "127.0.0.1";
        System.out.print("Enter the BS IP (Press Enter to use 127.0.0.1) :");
        String tmpIP = scanner.nextLine();
        if(!tmpIP.isEmpty())
            bServerIp = tmpIP;

        int bServerPort = 55555;
        System.out.print("Enter the BS port number (Press Enter to use 55555) :");
        String tmpPort = scanner.nextLine();
        if(!tmpPort.isEmpty())
            bServerPort = Integer.valueOf(tmpPort);

        System.out.print("Enter the name of the node: ");
        String localName = scanner.next();

        ConnectionHandler connectionHandler = new SocketHandler(localName);

        Node localNode = connectionHandler.getLocalNode();
        Node serverNode = new Node(bServerIp, bServerPort);

        BSServer bServer = new BSServer(serverNode, localNode, connectionHandler);
        ArrayList<Node> nodeList = bServer.getNodeList();

        FileHandler fileHandler = new FileHandler();

        NeighbourTable neighbourTable = new NeighbourTable(localNode);

        RouteHandler routeHandler = new RouteHandler(neighbourTable, connectionHandler, localNode);
        routeHandler.start();

        MessageReceiver messageReceiver = new MessageReceiver(connectionHandler, neighbourTable, fileHandler);
        messageReceiver.start();

        ConsoleListener consoleListener = new ConsoleListener(neighbourTable, connectionHandler, fileHandler, serverNode);
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
                connectionHandler.sendMessage(joinMessage, node);

                node.setStatus(Node.INITIAL_STATUS);
                neighbourTable.addNeighbour(node);
            }
        }
    }
}
