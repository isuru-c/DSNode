package dsnode.net;

import dsnode.Logger;
import dsnode.model.Message;
import dsnode.model.Node;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author Isuru Chandima
 */
public abstract class ConnectionHandler {

    static Logger logger = new Logger();

    String localIp;
    int localPort;
    String localName;

    public ConnectionHandler(String localName){
        try {
            DatagramSocket datagramSocket = new DatagramSocket();

            this.localPort = datagramSocket.getLocalPort();
            this.localName = localName;

            datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.localIp = datagramSocket.getLocalAddress().getHostAddress();

            logger.log("Socket open at the port " + datagramSocket.getLocalPort());

        } catch (UnknownHostException e) {
            logger.log("Error while getting local IP..!");
        } catch (SocketException e) {
            logger.log("Error while opening socket connection..!");
        }
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
