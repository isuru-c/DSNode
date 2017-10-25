package dsnode.model;

import java.util.LinkedList;

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
            if (node.getIp().equals(sourceNode.getIp()) && node.getPort() == sourceNode.getPort())
                return node;
        }

        return null;
    }

    public LinkedList<Node> getActiveNeighbourList(){
        LinkedList<Node> activeNeighbourList = new LinkedList<>();

        for(Node node:neighbourList){
            if(node.getStatus().equals(Node.ACTIVE_STATUS))
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
            if (node.getIp().equals(neighbour.getIp()) && node.getPort() == neighbour.getPort()){
                neighbourList.removeFirstOccurrence(node);

                break;
            }

        }
    }
}
