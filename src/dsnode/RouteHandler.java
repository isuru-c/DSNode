package dsnode;

import dsnode.model.NeighbourTable;
import dsnode.model.data.Node;
import dsnode.net.ConnectionHandler;

/**
 * @author Isuru Chandima
 */
@SuppressWarnings("InfiniteLoopStatement")
public class RouteHandler extends Thread {

    private static Logger logger = new Logger();

    private NeighbourTable neighbourTable;
    private ConnectionHandler connectionHandler;
    private Node localNode;
    private int activeTimeLength;
    private int inactiveTimeLength;
    private int inactiveHelloPeriod;

    RouteHandler(ConnectionHandler connectionHandler, NeighbourTable neighbourTable, Node localNode) {
        this.connectionHandler = connectionHandler;
        this.neighbourTable = neighbourTable;
        this.localNode = localNode;
        this.activeTimeLength = 60;
        this.inactiveTimeLength = this.activeTimeLength * 2;
        this.inactiveHelloPeriod = 10;
    }

    @Override
    public void run() {

        while (true) {

            try {

                for (Node node : neighbourTable.getNeighbourList()) {

                    if (node.getLastActive() > inactiveTimeLength) {
                        node.setStatus(Node.DEAD_STATUS);
                        continue;
                    }

                    node.increaseTime();

                    if (node.getLastActive() > activeTimeLength && node.getStatus().equals(Node.ACTIVE_STATUS)) {
                        // Active time of a neighbour expires, verify the existence of the neighbour

                        node.setStatus(Node.INACTIVE_STATUS);

                        String helloMessage = String.format("HELLO %s %d %s %d", node.getIp(), node.getPort(), localNode.getIp(), localNode.getPort());
                        helloMessage = String.format("%04d %s", (helloMessage.length() + 5), helloMessage);

                        connectionHandler.sendMessage(helloMessage, node);

                        node.resetLastHello();

                    } else if (node.getLastActive() > activeTimeLength && node.getStatus().equals(Node.INACTIVE_STATUS)) {
                        // Inactive node but still within checking time period

                        if (node.getLastHello() > inactiveHelloPeriod) {
                            String helloMessage = String.format("HELLO %s %d %s %d", node.getIp(), node.getPort(), localNode.getIp(), localNode.getPort());
                            helloMessage = String.format("%04d %s", (helloMessage.length() + 5), helloMessage);

                            connectionHandler.sendMessage(helloMessage, node);

                            node.resetLastHello();
                        }

                    }
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.log(String.format("Route Handler interrupted. [%s]", e.toString()));
            }
        }

    }
}
