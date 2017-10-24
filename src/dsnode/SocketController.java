package dsnode;

import dsnode.model.Message;
import dsnode.model.Node;

import java.io.IOException;
import java.net.*;

/**
 * @author Isuru Chandima
 */
class SocketController {

    private static Logger logger = new Logger();

    private String localIp;
    private int localPort;
    private String localName;

    private DatagramSocket datagramSocket;

    /**
     *
     * @param localName Name of this node given by the user
     */
    SocketController(String localName) {
        try {
            datagramSocket = new DatagramSocket();

            this.localIp = Inet4Address.getLocalHost().getHostAddress();
            this.localPort = datagramSocket.getLocalPort();
            this.localName = localName;

            logger.log("Socket open at the port " + datagramSocket.getLocalPort());

        } catch (UnknownHostException e) {
            logger.log("Error while getting local IP..!");
        } catch (SocketException e) {
            logger.log("Error while opening socket connection..!");
        }
    }

    /**
     *
     * @param message message needs to send to the remote node
     * @param receiver receiver of the message
     */
    void sendMessage(String message, Node receiver) {
        try {
            DatagramPacket reqMessagePacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(receiver.getIp()), receiver.getPort());

            datagramSocket.send(reqMessagePacket);
        } catch (UnknownHostException e) {
            logger.log(String.format("Receiver IP address error [%s]", receiver.getIp()));
        } catch (IOException e) {
            logger.log(String.format("Message sending failed. [%s]", message));
        }
    }

    /**
     *
     * @return Message object containing the message sent by remote node and its address
     */
    Message receiveMessage(){

        byte[] buffer = new byte[65536];
        DatagramPacket replyPacket = new DatagramPacket(buffer, buffer.length);
        String remoteIp = "";
        int remotePort = 0;

        try {
            datagramSocket.receive(replyPacket);

            remotePort  = replyPacket.getPort();
            remoteIp = replyPacket.getAddress().getHostAddress();
        }catch (IOException e){
            logger.log("Receiving message error..!");
        }

        byte[] data = replyPacket.getData();
        return new Message(new String(data, 0, replyPacket.getLength()), new Node(remoteIp, remotePort));
    }

    /**
     *
     * @return a Node object containing the details of current node.
     */
    Node getLocalNode(){
        return new Node(localIp, localPort, localName);
    }
}
