package dsnode.net;

import dsnode.model.data.Message;
import dsnode.model.data.Node;

import java.io.IOException;
import java.net.*;

/**
 * @author Isuru Chandima
 */
public class SocketHandler extends ConnectionHandler {

    private DatagramSocket datagramSocket;

    /**
     * @param localName Name of this node given by the user
     */
    public SocketHandler(String localName) {

        String localIp = "";
        int localPort = 0;

        try {
            DatagramSocket ds = new DatagramSocket();

            ds.connect(InetAddress.getByName("8.8.8.8"), 10002);
            localIp = ds.getLocalAddress().getHostAddress();

            logger.log("Socket open at the port " + ds.getLocalPort());
            System.out.print(String.format("Socket open at the port [%d]\n# ", ds.getLocalPort()));
            ds.close();

            datagramSocket = new DatagramSocket();
            localPort = datagramSocket.getLocalPort();

        } catch (UnknownHostException e) {
            logger.log("Error while getting local IP..!");
        } catch (SocketException e) {
            logger.log("Error while opening socket connection..!");
        }

        setLocalNode(localIp, localPort, localName);
    }

    @Override
    public void sendMessage(String message, Node receiver) {
        try {
            DatagramPacket reqMessagePacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(receiver.getIp()), receiver.getPort());

            datagramSocket.send(reqMessagePacket);
        } catch (UnknownHostException e) {
            logger.log(String.format("Receiver IP address error [%s]", receiver.getIp()));
        } catch (IOException e) {
            logger.log(String.format("Message sending failed. [%s]", message));
        }
    }

    @Override
    public Message receiveMessage() {

        byte[] buffer = new byte[65536];
        DatagramPacket replyPacket = new DatagramPacket(buffer, buffer.length);
        String remoteIp = "";
        int remotePort = 0;

        try {
            datagramSocket.receive(replyPacket);

            remotePort = replyPacket.getPort();
            remoteIp = replyPacket.getAddress().getHostAddress();
        } catch (IOException e) {
            logger.log("Receiving message error..!");
        }

        byte[] data = replyPacket.getData();
        return new Message(new String(data, 0, replyPacket.getLength()), new Node(remoteIp, remotePort));
    }

}
