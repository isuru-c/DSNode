package dsnode;

import dsnode.model.FileHandler;
import dsnode.model.SearchHandler;
import dsnode.model.data.Message;
import dsnode.model.NeighbourTable;
import dsnode.model.data.Node;
import dsnode.model.data.SearchResultSet;
import dsnode.net.ConnectionHandler;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Isuru Chandima
 */
@SuppressWarnings("InfiniteLoopStatement")
class MessageProcessor extends Thread {

    private static Logger logger = new Logger();

    private ConnectionHandler connectionHandler;
    private NeighbourTable neighbourTable;
    private FileHandler fileHandler;
    private SearchHandler searchHandler;

    private Node localNode;

    private int maxNumOfHops;

    MessageProcessor(ConnectionHandler connectionHandler, NeighbourTable neighbourTable, FileHandler fileHandler, SearchHandler searchHandler, Node serverNode) {

        this.connectionHandler = connectionHandler;
        this.neighbourTable = neighbourTable;
        this.fileHandler = fileHandler;
        this.searchHandler = searchHandler;

        this.localNode = connectionHandler.getLocalNode();

        this.maxNumOfHops = 7;

        // To start as a node in the distributed system, send the register request to the Bootstrap Server
        String regMessage = String.format("REG %s %d %s", localNode.getIp(), localNode.getPort(), localNode.getNodeName());
        regMessage = String.format("%04d %s", (regMessage.length() + 5), regMessage);

        logger.log(String.format("Register request message to BS [%s]", regMessage));
        connectionHandler.sendMessage(regMessage, serverNode);
        logger.log("Register request message is sent to BS");
        System.out.print("Waiting for respond from the Bootstrap Server...\n# ");
    }

    @Override
    public void run() {

        while (true) {

            Message messageObject = connectionHandler.receiveMessage();

            String message = messageObject.getMessage();
            Node sourceNode = messageObject.getSourceNode();

            logger.log(String.format("Message received [%s]", message));

            StringTokenizer st = new StringTokenizer(message, " ");

            st.nextToken(); // Length of the message, can be assigned to a variable if it is needed
            String command = st.nextToken();

            if (!st.hasMoreTokens()) {
                continue;
            }

            if ("REGOK".equals(command)) {
                handleREGOK(st);
            } else if ("UNROK".equals(command)) {
                handleUNROK(st);
            } else if ("LEAVEOK".equals(command)) {
                handleLEAVEOK(st, sourceNode);
            } else if ("LEAVE".equals(command)) {
                handleLEAVE(st);
            } else if ("JOIN".equals(command)) {
                handleJOIN(st);
            } else if ("JOINOK".equals(command)) {
                handleJOINOK(st, sourceNode);
            } else if ("NAME".equals(command)) {
                handleNAME(st, sourceNode);
            } else if ("NAMEOK".equals(command)) {
                handleNAMEOK(st);
            } else if ("HELLO".equals(command)) {
                handleHELLO(st);
            } else if ("HELLOOK".equals(command)) {
                handleHELLOOK(st);
            } else if ("SER".equals(command)) {
                handleSER(st, sourceNode);
            } else if ("SEROK".equals(command)) {
                handleSEROK(st, sourceNode);
            } else {
                // Do something in here to handle the unrecognized message
                logger.log(String.format("Unrecognized command found : [%s]", command));
            }
        }

    }

    private void handleREGOK(StringTokenizer tokenizeMessage) {

        String no_nodes = tokenizeMessage.nextToken();

        if ("9999".equals(no_nodes)) {
            // 9999 – failed, there is some error in the command
            logger.log("Error:9999 – failed, there is some error in the command");

        } else if ("9998".equals(no_nodes)) {
            // 9998 – failed, already registered to you, unregister first
            logger.log("Error:9998 – failed, already registered to you, unregister first");

        } else if ("9997".equals(no_nodes)) {
            // 9997 – failed, registered to another user, try a different IP and port
            logger.log("Error:9997 – failed, registered to another user, try a different IP and port");

        } else if ("9996".equals(no_nodes)) {
            // 9996 – failed, can’t register. BS full.
            logger.log("Error:9996 – failed, can’t register. BS full.");

        } else {
            // No error, no_nodes indicate the number of nodes given by the BS

            int numOfNodes = Integer.valueOf(no_nodes);

            if (numOfNodes == 0) {
                // No node is given, this is the first node in the network.

                logger.log("No detail about node is received from BS. This is the first node in the network.");
                System.out.print("Bootstrap Server responded. No other node in the network\n# ");

            } else {
                // numOfNodes number of nodes given by the server. Select random 2 nodes
                // and return details of those nodes.

                logger.log(numOfNodes + " nodes received from BS");

                System.out.print(String.format("Bootstrap Server responded. Connecting to %d nodes...\n# ", numOfNodes));

                int count = 1;

                String joinMessage = String.format("JOIN %s %d", localNode.getIp(), localNode.getPort());
                joinMessage = String.format("%04d %s", (joinMessage.length() + 5), joinMessage);
                logger.log(String.format("JOIN request created [%s]", joinMessage));

                while (count <= numOfNodes) {
                    String nodeIp = tokenizeMessage.nextToken();
                    int nodePort = Integer.valueOf(tokenizeMessage.nextToken());

                    Node node = new Node(nodeIp, nodePort);

                    logger.log(String.format("Joining request sent to node [%s-%d]", nodeIp, nodePort));
                    System.out.print(String.format("New neighbour added [%s-%d]\n# ", node.getIp(),node.getPort()));
                    connectionHandler.sendMessage(joinMessage, node);

                    node.setStatus(Node.INITIAL_STATUS);
                    neighbourTable.addNeighbour(node);

                    count++;
                }
            }
        }

    }

    private void handleUNROK(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for JOIN request");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful unregister from the BS server
            logger.log("Successfully unregistered from the BS server");
            System.out.print("Unregistered from the Bootstrap Server\n# ");
        } else if ("9999".equals(value)) {
            // Error while unregister from BS server
            logger.log("Error in UNROK message response...!");
        }

    }

    private void handleLEAVE(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 2) {
            logger.log("Incomplete message for LEAVE request...!");
            return;
        }

        // Create Node object for the leaving node
        String nodeIp = tokenizeMessage.nextToken();
        int nodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node neighbour = new Node(nodeIp, nodePort);

        int value = 0;

        if (neighbourTable.isExistingNeighbour(neighbour)) {
            logger.log(String.format("Neighbour [%s] leave the system", neighbourTable.getNeighbourNode(neighbour).getNodeName()));
            neighbourTable.removeNeighbour(neighbour);
        } else {
            value = 9999;
            logger.log(String.format("LEAVE request from non-existing neighbours [%s-%d]", nodeIp, nodePort));
        }

        // Render and send LEAVEOK message for the leaving neighbour
        String leaveResponseMessage = String.format("LEAVEOK %d", value);
        leaveResponseMessage = String.format("%04d %s", (leaveResponseMessage.length() + 5), leaveResponseMessage);

        connectionHandler.sendMessage(leaveResponseMessage, neighbour);
    }

    private void handleLEAVEOK(StringTokenizer tokenizeMessage, Node sourceNode) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for LEAVEOK response...!");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful leaving in remote node, remove neighbour node from the routing table
            neighbourTable.removeNeighbour(sourceNode);

        } else if ("9999".equals(value)) {
            // Error while leaving from neighbour node
            logger.log("Error in LEAVEOK message response...!");
        }

    }

    private void handleJOIN(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 2) {
            logger.log("Incomplete message for JOIN request");
            return;
        }

        // Create Node object for the new neighbour
        String newNodeIp = tokenizeMessage.nextToken();
        int newNodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node newNeighbour = new Node(newNodeIp, newNodePort);

        int value = 0;

        if (neighbourTable.isExistingNeighbour(newNeighbour)) {
            value = 9999;
            logger.log(String.format("JOIN request from existing neighbours [%s-%d]", newNodeIp, newNodePort));
        }

        // Render and send JOINOK message for the new neighbour
        String responseMessage = String.format("JOINOK %d", value);
        responseMessage = String.format("%04d %s", (responseMessage.length() + 5), responseMessage);

        connectionHandler.sendMessage(responseMessage, newNeighbour);

        if (value == 0) {
            newNeighbour.setStatus(Node.ACTIVE_STATUS);
            newNeighbour.restLastActive();
            neighbourTable.addNeighbour(newNeighbour);
            logger.log(String.format("New neighbour added [%s-%d]", newNodeIp, newNodePort));
            System.out.print(String.format("New neighbour added [%s-%d]\n# ", newNeighbour.getIp(),newNeighbour.getPort()));

            // Send name request for the new node
            String nameRequest = String.format("NAME %s %d", newNodeIp, newNodePort);
            nameRequest = String.format("%04d %s", (nameRequest.length() + 5), nameRequest);

            connectionHandler.sendMessage(nameRequest, newNeighbour);
            logger.log(String.format("NAME request sent [%s-%d]", newNodeIp, newNodePort));
        }
    }

    private void handleJOINOK(StringTokenizer tokenizeMessage, Node sourceNode) {

        if (tokenizeMessage.countTokens() < 1) {
            logger.log("Incomplete message for JOINOK respond...!");
            return;
        }

        String value = tokenizeMessage.nextToken();

        if ("0".equals(value)) {
            // Successful neighbour adding in remote node, add node to the routing table
            Node node = neighbourTable.getNeighbourNode(sourceNode);

            if (node != null) {
                node.setStatus(Node.ACTIVE_STATUS);
                node.restLastActive();
            } else {
                logger.log(String.format("Different IP/Port parameters in message and data packet [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));
                return;
            }

        } else if ("9999".equals(value)) {
            // Error while adding new node to routing table
            logger.log("Error in JOINOK message response...!");
            return;
        }

        // Send name request for the new node
        String nameRequest = String.format("NAME %s %d", sourceNode.getIp(), sourceNode.getPort());
        nameRequest = String.format("%04d %s", (nameRequest.length() + 5), nameRequest);

        connectionHandler.sendMessage(nameRequest, sourceNode);
        logger.log(String.format("NAME request sent [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));
    }

    private void handleNAME(StringTokenizer tokenizeMessage, Node sourceNode) {

        if (tokenizeMessage.countTokens() < 2) {
            logger.log("Incomplete message for NAME request...!");
            return;
        }

        String nodeIp = tokenizeMessage.nextToken();
        int nodePort = Integer.valueOf(tokenizeMessage.nextToken());
        Node node = new Node(nodeIp, nodePort);

        // Check whether NAME request came from knowing neighbour
        if (!neighbourTable.isLocalNode(node))
            return;

        // Send NAMEOK respond to the NAME requester
        String nameResponse = String.format("NAMEOK %s %d %s", nodeIp, nodePort, localNode.getNodeName());
        nameResponse = String.format("%04d %s", (nameResponse.length() + 5), nameResponse);

        connectionHandler.sendMessage(nameResponse, sourceNode);
        logger.log(String.format("NAMEOK respond sent to [%s-%d]", sourceNode.getIp(), sourceNode.getPort()));

    }

    private void handleNAMEOK(StringTokenizer tokenizeMessage) {

        if (tokenizeMessage.countTokens() < 3) {
            logger.log("Incomplete message for NAMEOK response...!");
            return;
        }

        String nodeIp = tokenizeMessage.nextToken();
        int nodePort = Integer.valueOf(tokenizeMessage.nextToken());
        String nodeName = tokenizeMessage.nextToken();
        Node node = new Node(nodeIp, nodePort, nodeName);

        if (neighbourTable.isExistingNeighbour(node)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(node);
            neighbourNode.setNodeName(nodeName);
            logger.log(String.format("Name updated in the node [%s-%d]", nodeIp, nodePort));
        }
    }

    private void handleHELLO(StringTokenizer tokenizeMessage) {

        /*
         * HELLO message format
         *
         * length HELLO IP_address1 port_no1 IP_address2 port_no2
         *
         * length - Length of the entire message including 4 characters used to indicate the length. In xxxx format
         * HELLO - Hello request
         *
         * IP_address1 - IP address of the node which the hello request message is sending to
         * port_no1 - Port number of the node which the hello request message is sending to
         *
         * IP_address2 - IP address of the node which the hello request message is sending from
         * port_no2 - Port number of the node which the hello request message is sending from
         *
         */

        if (tokenizeMessage.countTokens() < 4) {
            logger.log("Incomplete message for HELLO request...!");
            return;
        }

        String targetIp = tokenizeMessage.nextToken();
        int targetPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node targetNode = new Node(targetIp, targetPort);

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        if (neighbourTable.isLocalNode(targetNode) && neighbourTable.isExistingNeighbour(requestNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(requestNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);

            String helloOkMessage = String.format("HELLOOK %s %d %s %d", neighbourNode.getIp(), neighbourNode.getPort(), targetNode.getIp(), targetNode.getPort());
            helloOkMessage = String.format("%04d %s", (helloOkMessage.length() + 5), helloOkMessage);

            connectionHandler.sendMessage(helloOkMessage, neighbourNode);
        }
    }

    private void handleHELLOOK(StringTokenizer tokenizeMessage) {

        /*
         * HELLOOK message format
         *
         * length HELLOOK IP_address1 port_no1 IP_address2 port_no2
         *
         * length - Length of the entire message including 4 characters used to indicate the length. In xxxx format
         * HELLOOK - Hello Ok response
         *
         * IP_address1 - IP address of the node which the hello ok response message is sending to
         * port_no1 - Port number of the node which the hello ok response message is sending to
         *
         * IP_address2 - IP address of the node which the hello ok response message is sending from
         * port_no2 - Port number of the node which the hello ok response message is sending from
         *
         */


        if (tokenizeMessage.countTokens() < 4) {
            logger.log("Incomplete message for HELLOOK response...!");
            return;
        }

        String targetIp = tokenizeMessage.nextToken();
        int targetPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node targetNode = new Node(targetIp, targetPort);

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        if (neighbourTable.isLocalNode(targetNode) && neighbourTable.isExistingNeighbour(requestNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(requestNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);
        }

    }

    private void handleSER(StringTokenizer tokenizeMessage, Node sourceNode) {

        /*
         * SER message format
         *
         * length SER IP port searchId file_name hops
         *
         * length – Length of the entire message including 4 characters used to indicate the length. In xxxx format
         * SER – Locate a file with this name
         *
         * IP – IP address of the node that is searching for the file. May be useful depending your design
         * port – port number of the node that is searching for the file. May be useful depending your design
         *
         * searchId - random long int value use as search id
         * file_name – File name being searched
         * hops – A hop count. May be of use for cost calculations
         *
         */

        // If the node which sends the SER request to this node is neighbour of this node, make it active and reset timers
        if (neighbourTable.isExistingNeighbour(sourceNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(sourceNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);
        }

        String requestIp = tokenizeMessage.nextToken();
        int requestPort = Integer.valueOf(tokenizeMessage.nextToken());
        Node requestNode = new Node(requestIp, requestPort);

        String searchId = tokenizeMessage.nextToken();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tokenizeMessage.nextToken());

        while (tokenizeMessage.countTokens() > 1) {
            stringBuilder.append(" ");
            stringBuilder.append(tokenizeMessage.nextToken());
        }

        String fileName = stringBuilder.toString();

        int hops = Integer.valueOf(tokenizeMessage.nextToken()) + 1;

        if (hops > maxNumOfHops) {
            // SER message has reached the maximum number of hops it can be forwarded.
            return;
        }

        if (fileHandler.isNewSearchId(searchId)) {
            ArrayList<String> localFileList = fileHandler.searchFiles(fileName);

            int nof = localFileList.size();

            String searchResponse = String.format("SEROK %s %d %s %d %d", fileName.replace(' ', '_'), nof, localNode.getIp(), localNode.getPort(), hops);
            for (String file : localFileList) {
                searchResponse = String.format("%s %s", searchResponse, file);
            }
            searchResponse = String.format("%04d %s", (searchResponse.length() + 5), searchResponse);

            connectionHandler.sendMessage(searchResponse, requestNode);
        }

        // Forward the search request using random walk

        Node randomNode = neighbourTable.getRandomNeighbour(sourceNode);

        if (randomNode == null)
            return;

        String searchRequest = String.format("SER %s %d %s %s %d", requestIp, requestPort, searchId, fileName, hops);
        searchRequest = String.format("%04d %s", (searchRequest.length() + 5), searchRequest);

        connectionHandler.sendMessage(searchRequest, randomNode);

    }

    private void handleSEROK(StringTokenizer tokenizeMessage, Node sourceNode) {

        /*
         * SEROK message format
         *
         * length SEROK file_name no_files IP port hops filename1 filename2 ... ...
         *
         * length – Length of the entire message including 4 characters used to indicate the length. In xxxx format
         * SEROK – Sends the result for search. The node that sends this message is the one that actually stored the (key, value) pair, i.e., node that index the file information
         *
         * file_name - File name being searched
         *
         * no_files – Number of results returned
         *      ≥ 1 – Successful
         *      0 – no matching results. Searched key is not in key table
         *      9999 – failure due to node unreachable
         *      9998 – some other error
         *
         * IP – IP address of the node having (stored) the file
         * port – Port number of the node having (stored) the file
         *
         * hops – Hops required to find the file(s)
         * filename – Actual name of the file
         *
         */

        // If the node which sends the SEROK response to this node is neighbour of this node, make it active and reset timers
        if (neighbourTable.isExistingNeighbour(sourceNode)) {
            Node neighbourNode = neighbourTable.getNeighbourNode(sourceNode);
            neighbourNode.setStatus(Node.ACTIVE_STATUS);
        }

        String searchFileName = tokenizeMessage.nextToken();

        int nof = Integer.valueOf(tokenizeMessage.nextToken());

        if (nof > 0) {
            String responseIp = tokenizeMessage.nextToken();
            int responsePort = Integer.valueOf(tokenizeMessage.nextToken());

            Node responseNode = new Node(responseIp, responsePort);
            if (neighbourTable.isExistingNeighbour(responseNode))
                responseNode = neighbourTable.getNeighbourNode(responseNode);

            int hopCount = Integer.valueOf(tokenizeMessage.nextToken());

            String[] fileNames = new String[nof];
            int count = 0;

            while (tokenizeMessage.hasMoreElements()) {
                fileNames[count] = tokenizeMessage.nextToken();
                count++;
            }
            searchHandler.addSearchResult(new SearchResultSet(responseNode, fileNames, hopCount), searchFileName);
        }
    }

}
