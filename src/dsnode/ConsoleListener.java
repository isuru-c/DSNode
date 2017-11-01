package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.Node;
import dsnode.net.ConnectionHandler;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author Isuru Chandima
 */
@SuppressWarnings("InfiniteLoopStatement")
public class ConsoleListener extends Thread {

    private static Logger logger = new Logger();

    private NeighbourTable neighbourTable;
    private ConnectionHandler connectionHandler;
    private FileHandler fileHandler;
    private Node bsServer;

    ConsoleListener(NeighbourTable neighbourTable, ConnectionHandler connectionHandler, FileHandler fileHandler, Node bsServer) {
        this.neighbourTable = neighbourTable;
        this.connectionHandler = connectionHandler;
        this.fileHandler = fileHandler;
        this.bsServer = bsServer;
    }

    @Override
    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            String command = scanner.nextLine();
            if (command.isEmpty()) {
                System.out.print("# ");
            } else {
                processCommand(command);
            }
        }

    }

    private void processCommand(String consoleCommand) {

        StringTokenizer st = new StringTokenizer(consoleCommand.toLowerCase(), " ");

        String command1 = st.nextToken();

        if ("show".equals(command1)) {

            String command2 = st.nextToken();

            if ("neighbours".equals(command2)) {

                System.out.println("Number\tName\t\tIP Address\t\tPort\tLast Seen\tStatus");
                System.out.println("--------------------------------------------------------------");
                int count = 1;
                for (Node node : neighbourTable.getNeighbourList()) {
                    System.out.println(String.format("%d\t\t%s\t\t%s\t%d\t%d\t\t%s", count, node.getNodeName(), node.getIp(), node.getPort(), node.getLastActive(), node.getStatus()));
                    count++;
                }
                System.out.println();
                System.out.print("# ");
            } else if ("files".equals(command2)) {

                String fileList[] = fileHandler.getFileList();

                System.out.println();
                System.out.println("Number\t| File Name");
                System.out.println("----------------------------------");
                int count = 1;
                for (String file : fileList) {
                    System.out.println(String.format("  %d\t\t| %s", count, file));
                    count++;
                }
                System.out.print("----------------------------------\n\n#");
            }

        } else if ("leave".equals(command1)) {

            Node localNode = connectionHandler.getLocalNode();

            // Unregister from the Bootstrap Server

            String unregRequest = String.format("UNREG %s %d %s", localNode.getIp(), localNode.getPort(), localNode.getNodeName());
            unregRequest = String.format("%04d %s", (unregRequest.length() + 5), unregRequest);

            connectionHandler.sendMessage(unregRequest, bsServer);
            logger.log("UNREG from the BS Server");


            // Leave from all the neighbours

            for (Node node : neighbourTable.getNeighbourList()) {

                String leaveRequest = String.format("LEAVE %s %d", localNode.getIp(), localNode.getPort());
                leaveRequest = String.format("%04d %s", (leaveRequest.length() + 5), leaveRequest);

                connectionHandler.sendMessage(leaveRequest, node);
                logger.log(String.format("LEAVE from the node [%s]", localNode.getNodeName()));
            }
        } else if ("search".equals(command1)) {

            String fileName = consoleCommand.substring(command1.length() + 1);

            // First search locally for the file name

            ArrayList<String> localFileList = fileHandler.searchFiles(fileName);

            if (!localFileList.isEmpty()) {

                System.out.println("Local search result for file " + fileName);
                for (String file : localFileList) {
                    System.out.println("\t\t" + file);
                }
            }

            // Broadcast search message for all neighbours

            Node localNode = connectionHandler.getLocalNode();

            int hops = 0;

            String searchRequest = String.format("SER %s %d %s %d", localNode.getIp(), localNode.getPort(), fileName, hops);
            searchRequest = String.format("%04d %s", (searchRequest.length() + 5), searchRequest);

            for (Node node : neighbourTable.getActiveNeighbourList()) {
                connectionHandler.sendMessage(searchRequest, node);
                logger.log(String.format("SER request sent to [%s]", node.getNodeName()));
            }

        }

    }
}
