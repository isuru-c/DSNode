package dsnode.rmi;

import com.sun.corba.se.impl.activation.ServerMain;
import dsnode.model.FileHandler;
import dsnode.model.NeighbourTable;
import dsnode.model.SearchHandler;
import dsnode.model.data.Node;
import dsnode.net.ConnectionHandler;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sugeesh Chandraweera
 */
public class RMIServer {

    public static void main(String[] args) {
//        startRMIServer();
    }

    public static void startRMIServer(Node localNode, NeighbourTable neighbourTable, FileHandler fileHandler, SearchHandler searchHandler){
        String ip = localNode.getIp();
        int port = localNode.getPort()+1;

        System.setProperty("java.rmi.server.hostname",ip);
        try {
            Registry customerRegistry = LocateRegistry.createRegistry(port);
            customerRegistry.rebind("SWISServer", new NodeController(neighbourTable,fileHandler,searchHandler));
            System.out.println("Server Started "+ip+" : "+port);
        } catch (RemoteException ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
