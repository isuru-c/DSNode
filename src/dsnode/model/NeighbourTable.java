package dsnode.model;

import dsnode.model.data.Node;

import java.util.LinkedList;
import java.util.Random;

public class NeighbourTable {

    private LinkedList<Node> neighbourList;
    private Node localNode;

    public NeighbourTable(Node localNode) {
        this.neighbourList = new LinkedList<>();
        this.localNode = localNode;
    }

    public void addNeighbour(Node newNode) {
        neighbourList.add(newNode);
    }

    public LinkedList<Node> getNeighbourList() {
        return neighbourList;
    }

    public Node getNeighbourNode(Node sourceNode) {

        for (Node node : neighbourList) {
            if (node.getIp().equals(sourceNode.getIp()) && node.getPort() == sourceNode.getPort()) {
                return node;
            }
        }

        return null;
    }

    public LinkedList<Node> getActiveNeighbourList() {
        LinkedList<Node> activeNeighbourList = new LinkedList<>();

        for (Node node : neighbourList) {
            if (node.getStatus().equals(Node.ACTIVE_STATUS))
                activeNeighbourList.add(node);
        }

        return activeNeighbourList;
    }

    public boolean isExistingNeighbour(Node sourceNode) {

        for (Node node : neighbourList) {
            if (node.getIp().equals(sourceNode.getIp()) && node.getPort() == sourceNode.getPort())
                return true;
        }

        return false;
    }

    public boolean isLocalNode(Node sourceNode) {
        return sourceNode.getIp().equals(localNode.getIp()) && sourceNode.getPort() == localNode.getPort();
    }

    public void removeNeighbour(Node neighbour) {

        for (Node node : neighbourList) {
            if (node.getIp().equals(neighbour.getIp()) && node.getPort() == neighbour.getPort()) {
                neighbourList.removeFirstOccurrence(node);
                break;
            }
        }
    }

    private Node getRandomNeighbour() {

        LinkedList<Node> activeNeighbourList = getActiveNeighbourList();

        int neighbourCount = activeNeighbourList.size();

        if (neighbourCount == 0)
            return null;

        Random rand = new Random();
        int randomNumber = rand.nextInt(neighbourCount);

        return activeNeighbourList.get(randomNumber);
    }

    public Node getRandomNeighbour(Node node) {

        Node randomNode = null;
        boolean randomNodeFound = false;

        while (!randomNodeFound) {
            randomNode = getRandomNeighbour();

            if (randomNode == null)
                return null;

            if (!(node.getIp().equals(randomNode.getIp()) && node.getPort() == randomNode.getPort()))
                randomNodeFound = true;
        }

        return randomNode;
    }
}
