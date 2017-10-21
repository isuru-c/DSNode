package dsnode;

class Node {

    private String ip;
    private int port;
    private String userName = "";

    Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    Node(String ip, int port, String userName) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
    }

    String getIp() {
        return ip;
    }

    int getPort() {
        return port;
    }

    String getUserName() {
        return userName;
    }
}
