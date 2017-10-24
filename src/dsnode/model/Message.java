package dsnode.model;

public class Message {

    String message;
    Node sourceNode;

    public Message(String message, Node sourceNode) {
        this.message = message;
        this.sourceNode = sourceNode;
    }

    public String getMessage() {
        return message;
    }

    public Node getSourceNode() {
        return sourceNode;
    }
}
