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

    public ConsoleListener(NeighbourTable neighbourTable) {
        this.neighbourTable = neighbourTable;
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

        StringTokenizer st = new StringTokenizer(consoleCommand, " ");

        String command1 = st.nextToken();

        if ("show".equals(command1)) {

            String command2 = st.nextToken();

            if ("neighbours".equals(command2)) {

                System.out.println("Number\tName\t\tIP Address\t\tPort\tLast Seen\tStatus");
                System.out.println("--------------------------------------------------------------");
                int count = 1;
                for (Node node : neighbourTable.getNeighbourList()) {
                    System.out.println(String.format("%d\t\t%s\t\t%s\t%d\t%d\t\t\t%s", count, node.getNodeName(), node.getIp(), node.getPort(), node.getLastSeen(), node.getStatus()));
                    count++;
                }
                System.out.println();
                System.out.print("# ");
            }

        }

    }
}
