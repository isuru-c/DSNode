package dsnode.model.data;

/**
 * @author Isuru Chandima
 */
public class Node {

    private String ip;
    private int port;
    private String nodeName = "-ny-";
    private int lastActive;
    private int lastHello;
    private String status;

    public static String INITIAL_STATUS = "Initial";
    public static String ACTIVE_STATUS = "Active";
    public static String INACTIVE_STATUS = "Inactive";
    public static String DEAD_STATUS = "Dead";
    private static String TEMPORARY_STATUS = "Temporary";

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.nodeName = "-ny-";
        this.lastActive = 99999;
        this.lastHello = 0;
        this.status = TEMPORARY_STATUS;
    }

    public Node(String ip, int port, String userName) {
        this.ip = ip;
        this.port = port;
        this.nodeName = userName;
        this.lastActive = 99999;
        this.lastHello = 0;
        this.status = TEMPORARY_STATUS;
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
        this.status = status;
        if(getStatus().equals(ACTIVE_STATUS)){
            restLastActive();
            resetLastHello();
        }
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
}
