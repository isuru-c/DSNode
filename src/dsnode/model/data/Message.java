package dsnode.model.data;

public class Message {

    private String message;
    private Node sourceNode;

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
