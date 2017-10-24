package dsnode.model;

import java.util.LinkedList;

public class NeighbourTable {

    private LinkedList<Node> neighbourList;

    public NeighbourTable() {
        neighbourList = new LinkedList<>();
    }

    public void addNeighbour(Node newNode) {
        neighbourList.add(newNode);
    }

    public LinkedList<Node> getNeighbourList() {
        return neighbourList;
    }

    public Node getNeighbourNode(Node sourceNode) {

        for (Node node : neighbourList) {
            if (node.getIp().equals(sourceNode.getIp()) && node.getPort() == sourceNode.getPort())
                return node;
        }

        return null;
    }
}
