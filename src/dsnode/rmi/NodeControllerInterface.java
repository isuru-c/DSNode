package dsnode.rmi;

import dsnode.model.data.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 * @author Sugeesh Chandraweera
 */
public interface NodeControllerInterface extends Remote{
//    public ItemController getItemController () throws RemoteException,SQLException , ClassNotFoundException;
        public Node getNode() throws ClassNotFoundException, RemoteException;
        public boolean setNode(Node node) throws ClassNotFoundException, RemoteException;

        public String searchItem(String message, Node senderNode) throws ClassNotFoundException, RemoteException;
        public String seachFound(String message, Node senderNode) throws ClassNotFoundException, RemoteException;

}
