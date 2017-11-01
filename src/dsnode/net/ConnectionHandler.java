package dsnode.net;

import dsnode.Logger;
import dsnode.model.Message;
import dsnode.model.Node;

/**
 *
 * @author Isuru Chandima
 */
public abstract class ConnectionHandler {

    static Logger logger = new Logger();

    private String localIp;
    private int localPort;
    private String localName;

    /**
     * This function is used to set the parameters of the local node
     *
     * @param localIp ip used by the local node
     * @param localPort port number used by the local node
     * @param localName name given by the user to the local node
     */
    void setLocalNode(String localIp, int localPort, String localName){
        this.localName = localName;
        this.localIp = localIp;
        this.localPort = localPort;
    }

    /**
     * @param message  message needs to send to the remote node
     * @param receiver receiver of the message
     */
    public abstract void sendMessage(String message, Node receiver);

    /**
     * @return Message object containing the message sent by remote node and its address
     */
    public abstract Message receiveMessage();

    /**
     * @return a Node object containing the details of current node.
     */
    public Node getLocalNode() {
        return new Node(localIp, localPort, localName);
    }
}
