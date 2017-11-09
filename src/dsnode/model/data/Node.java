package dsnode.model.data;

import dsnode.rmi.NodeController;
import dsnode.rmi.NodeControllerInterface;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * @author Isuru Chandima
 */
public class Node implements Serializable {

    private String ip;
    private int port;
    private String nodeName = "-ny-";
    private int lastActive;
    private int lastHello;
    private String status;

    // RMI Node Controller
    private NodeControllerInterface nodeController;


    public static String INITIAL_STATUS = "Initial";
    public static String ACTIVE_STATUS = "Active";
    public static String INACTIVE_STATUS = "Inactive";
    public static String DEAD_STATUS = "Dead";
    private static String TEMPORARY_STATUS = "Temporary";

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.nodeName = "-";
        this.lastActive = 99999;
        this.lastHello = 0;
        this.status = INITIAL_STATUS;
    }

    public Node(String ip, int port, String userName) {
        this.ip = ip;
        this.port = port;
        this.nodeName = userName;
        this.lastActive = 99999;
        this.lastHello = 0;
        this.status = INITIAL_STATUS;
    }

    public void restLastActive() {
        lastActive = 1;
    }

    public void resetLastHello() {
        lastHello = 1;
    }

    public void increaseTime() {
        lastActive++;
        lastHello++;
    }

    public void setStatus(String status) {

        if (!this.status.equals(status)) {
            if (!((status.equals(ACTIVE_STATUS) && this.status.equals(INACTIVE_STATUS)) || (status.equals(INACTIVE_STATUS) && this.status.equals(ACTIVE_STATUS))))
                System.out.print(String.format("Neighbour [%s:%d %s] goes to %s from %s \n# ", getIp(), getPort(), getNodeName(), status, this.status));
        }

        this.status = status;
        if (getStatus().equals(ACTIVE_STATUS)) {
            restLastActive();
            resetLastHello();
        }
    }

//    public NodeControllerInterface getNodeControlerForNode(String ip, int port){
//        return getNodeControlerFromServer(ip,port);
//    }

    public NodeControllerInterface getNodeControlerForNode(){
        try {
            NodeControllerInterface nodeControlerForNode = getNodeControlerFromServer(ip, port);
            nodeControlerForNode.setNode(this);
            return nodeControlerForNode;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    private NodeControllerInterface getNodeControlerFromServer(String ip, int port){
        try {
            String lookupString = "rmi://"+ip+":"+(port+1)+"/SWISServer";
            NodeControllerInterface lookup = (NodeControllerInterface) Naming.lookup(lookupString);
            return lookup;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }





    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getLastActive() {
        return lastActive;
    }

    public int getLastHello() {
        return lastHello;
    }

    public String getStatus() {
        return status;
    }

    public NodeControllerInterface getNodeController() {
        return nodeController;
    }

    public void setNodeController(NodeControllerInterface nodeController) {
        this.nodeController = nodeController;
    }

}
