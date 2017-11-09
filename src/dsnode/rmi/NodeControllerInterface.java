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
        public String searchFound(String message, Node senderNode) throws ClassNotFoundException, RemoteException;

        public void joinAsNeighbour(String message) throws ClassNotFoundException, RemoteException;
        public void joinResponse(String message, Node senderNode) throws ClassNotFoundException, RemoteException;

        public void getName(String message, Node senderNode) throws ClassNotFoundException, RemoteException;
        public void nameResponse(String message, Node senderNode) throws ClassNotFoundException, RemoteException;

        public void leaveAsNeighbour(String message) throws ClassNotFoundException, RemoteException;
        public void leaveResponse(String message, Node senderNode) throws ClassNotFoundException, RemoteException;

        public void hello(String message) throws ClassNotFoundException, RemoteException;
        public void helloResponse(String message) throws ClassNotFoundException, RemoteException;




}
