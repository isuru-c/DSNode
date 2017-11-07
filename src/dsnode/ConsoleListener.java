package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.SearchHandler;
import dsnode.model.data.Node;
import dsnode.model.data.SearchResultSet;
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
    private SearchHandler searchHandler;

    private Node bsServer;
    private Node localNode;

    ConsoleListener(ConnectionHandler connectionHandler, NeighbourTable neighbourTable, FileHandler fileHandler, SearchHandler searchHandler, Node bsServer) {
        this.connectionHandler = connectionHandler;
        this.neighbourTable = neighbourTable;
        this.fileHandler = fileHandler;
        this.searchHandler = searchHandler;
        this.bsServer = bsServer;
        this.localNode = connectionHandler.getLocalNode();
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
            } else if ("search".equals(command2)) {
                // Show the results of the latest search

                ArrayList<SearchResultSet> searchResultSets = searchHandler.getSearchResultSets();

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
            searchHandler.newSearch(fileName.replace(' ', '_'));

            // First search locally for the file name

            ArrayList<String> localFileList = fileHandler.searchFiles(fileName);

            if (!localFileList.isEmpty()) {

                String[] fileNames = new String[localFileList.size()];
                int count = 0;

                for (String file : localFileList) {
                    fileNames[count++] = file;
                }

                searchHandler.addSearchResult(new SearchResultSet(localNode, fileNames, 0), fileName);
            }

            // Broadcast search message for all neighbours

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
