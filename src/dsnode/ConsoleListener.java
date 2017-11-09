package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.SearchHandler;
import dsnode.model.data.Node;
import dsnode.model.data.SearchResultSet;
import dsnode.net.ConnectionHandler;

import java.util.ArrayList;
import java.util.Random;
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

            String command2;

            if (st.hasMoreTokens())
                command2 = st.nextToken();
            else return;

            if ("neighbours".equals(command2)) {

                System.out.println(String.format(" %-6s | %-12s | %-15s | %-6s | %-10s | %-6s","Number","Name","IP Address","Port","Last Seen","Status"));
                System.out.println("------------------------------------------------------------------------");
                int count = 1;
                for (Node node : neighbourTable.getNeighbourList()) {
                    System.out.println(String.format(" %-6d | %-12s | %-15s | %-6d | %-10d | %-6s", count, node.getNodeName(), node.getIp(), node.getPort(), node.getLastActive(), node.getStatus()));
                    count++;
                }
                System.out.println("------------------------------------------------------------------------");
                System.out.print("\n# ");
            } else if ("files".equals(command2)) {

                String fileList[] = fileHandler.getFileList();

                System.out.println();
                System.out.println(String.format(" %-6s | %-20s", "Number", "File Name"));
                System.out.println("----------------------------------");
                int count = 1;
                for (String file : fileList) {
                    System.out.println(String.format(" %-6d | %-20s", count, file));
                    count++;
                }
                System.out.print("----------------------------------\n\n#");
            } else if ("search".equals(command2)) {
                // Show the results of the latest search

                ArrayList<SearchResultSet> searchResultSets = searchHandler.getSearchResultSets();

                int count = 0;

                if(searchResultSets.size()==0){
                    System.out.print("No result for file name: " + searchHandler.getCurrentSearch() + "\n\n# ");
                    return;
                }

                System.out.println(String.format("Search Result for [%s]:\n", searchHandler.getCurrentSearch()));
                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.println(String.format(" %-6s | %-20s | %-36s | %-9s | %-8s","Number", "File Name", "          Owner details","Hop Count", "Time(ms)"));
                System.out.println(String.format(" %-6s | %-20s | %-10s | %-15s | %-5s | %-9s | %-8s"," ", " ","Name", "IP Address","Port", " ", " "));
                System.out.println("--------------------------------------------------------------------------------------------");
                for (SearchResultSet searchResultSet : searchResultSets) {
                    Node ownerNode = searchResultSet.getOwnerNode();
                    //System.out.println(String.format("\tFrom node [%s-%d] %s [%d nodes away - %dms]", ownerNode.getIp(), ownerNode.getPort(), ownerNode.getNodeName(), searchResultSet.getHopCount(), searchResultSet.getQueryTime()));
                    for (String searchResult : searchResultSet.getFileNames()) {
                        count++;
                        System.out.println(String.format(" %-6d | %-20s | %-10s | %-15s | %-5d | %-9d | %-8d", count,searchResult,ownerNode.getNodeName(),ownerNode.getIp(), ownerNode.getPort(),searchResultSet.getHopCount(),searchResultSet.getQueryTime()));
                    }
                    //System.out.println();
                }
                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.print("\n# ");

            }

        } else if ("leave".equals(command1)) {

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

            System.exit(0);
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

            Random random = new Random();

            long searchId = random.nextLong();
            fileHandler.addSearchId(String.valueOf(searchId));

            // Broadcast search message for all neighbours

            int hops = 0;

            String searchRequest = String.format("SER %s %d %d %s %d", localNode.getIp(), localNode.getPort(), searchId, fileName, hops);
            searchRequest = String.format("%04d %s", (searchRequest.length() + 5), searchRequest);

            for (Node node : neighbourTable.getActiveNeighbourList()) {
                connectionHandler.sendMessage(searchRequest, node);
                logger.log(String.format("SER request sent to [%s]", node.getNodeName()));
            }

        }

    }
}
