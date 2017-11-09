package dsnode.net;

import dsnode.model.data.Message;
import dsnode.model.data.Node;

/**
 * @author Sugeesh Chandraweera
 */
public class RMIHandler  extends ConnectionHandler{


    @Override
    public void sendMessage(String message, Node receiver) {

    }

    @Override
    public Message receiveMessage() {
        return null;
    }
}
