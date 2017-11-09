package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.SearchHandler;
import dsnode.model.data.Node;
import dsnode.net.ConnectionHandler;
import dsnode.net.SocketHandler;
import dsnode.rmi.RMIServer;

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

        String localName = "node-x";
        System.out.print("Enter the name of the node: ");
        String tmpName = scanner.next();
        if(!tmpName.isEmpty())
            localName = tmpName;


        ConnectionHandler connectionHandler = new SocketHandler(localName);

        Node localNode = connectionHandler.getLocalNode();
        Node serverNode = new Node(bServerIp, bServerPort);



        FileHandler fileHandler = new FileHandler();

        NeighbourTable neighbourTable = new NeighbourTable(localNode);

        SearchHandler searchHandler = new SearchHandler();

        // Started RMI Server for local node
        RMIServer.startRMIServer(localNode,neighbourTable,fileHandler,searchHandler);

        RouteHandler routeHandler = new RouteHandler(connectionHandler, neighbourTable, localNode);
        routeHandler.start();

        MessageProcessor messageReceiver = new MessageProcessor(connectionHandler, neighbourTable, fileHandler, searchHandler, serverNode);
        messageReceiver.start();

        ConsoleListener consoleListener = new ConsoleListener(connectionHandler, neighbourTable, fileHandler, searchHandler, serverNode);
        consoleListener.start();

    }
}
