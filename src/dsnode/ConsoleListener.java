package dsnode;

import dsnode.model.NeighbourTable;
import dsnode.model.Node;

import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author Isuru Chandima
 */
@SuppressWarnings("InfiniteLoopStatement")
public class ConsoleListener extends Thread {

    private static Logger logger = new Logger();

    private NeighbourTable neighbourTable;
    private SocketController socketController;
    private Node bsServer;

    ConsoleListener(NeighbourTable neighbourTable, SocketController socketController, Node bsServer) {
        this.neighbourTable = neighbourTable;
        this.socketController = socketController;
        this.bsServer = bsServer;
    }

    @Override
    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            String command = scanner.nextLine();
            if (command.isEmpty()) {
                System.out.print("# ");
            }else {
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
                    System.out.println(String.format("%d\t\t%s\t\t%s\t%d\t%d\t\t%s", count, node.getNodeName(), node.getIp(), node.getPort(), node.getLastSeen(), node.getStatus()));
                    count++;
                }
                System.out.println();
                System.out.print("# ");
            }

        }else if("leave".equals(command1)){

            Node localNode = socketController.getLocalNode();

            // Unregister from the Bootstrap Server

            String unregRequest = String.format("UNREG %s %d %s", localNode.getIp(), localNode.getPort(),localNode.getNodeName());
            unregRequest = String.format("%04d %s", (unregRequest.length() + 5), unregRequest);

            socketController.sendMessage(unregRequest, bsServer);
            logger.log(String.format("UNREG from the BS Server"));


            // Leave from all the neighbours

            for (Node node : neighbourTable.getNeighbourList()){

                String leaveRequest = String.format("LEAVE %s %d", localNode.getIp(), localNode.getPort());
                leaveRequest = String.format("%04d %s", (leaveRequest.length() + 5), leaveRequest);

                socketController.sendMessage(leaveRequest, node);
                logger.log(String.format("LEAVE from the node [%s]", localNode.getNodeName()));
            }
        }

    }
}
