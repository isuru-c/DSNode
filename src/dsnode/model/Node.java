package dsnode.model;

/**
 *
 * @author Isuru Chandima
 */
public class Node {

    private String ip;
    private int port;
    private String userName = "";
    private int lastSeen;
    private String status;

    public static String INITIAL_STATUS = "Initial";
    public static String ACTIVE_STATUS = "Active";
    public static String INACTIVE_STATUS = "Inactive";

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.lastSeen = 10000;
        this.status = "initial";
    }

    public Node(String ip, int port, String userName) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.lastSeen = 10000;
        this.status = "initial";
    }

    public void restLastSeen(){
        lastSeen = 1;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public int getLastSeen() {
        return lastSeen;
    }

    public String getStatus() {
        return status;
    }
}
