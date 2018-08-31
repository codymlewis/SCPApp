import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
/**
 * ChatServer.java
 * A socket based half duplex chat server
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class ChatServer extends Chat {
    private ServerSocket serverSocket;
    private String username;
    private String welcomeMessage;
    private static final int BACKLOG = 1; // Max length for queue of messages
    public ChatServer(String title) { super(title); }
    /**
     * The main thread
     * @param args command line arguments
     */
    public static void main(String args[]) {
        ChatServer cs = new ChatServer("SCP Server");
        int exitVal = cs.run(args);
        System.exit(exitVal);
    }
    /**
     * The main flow of the program
     * @param args The arguments sent in with the program
     * @return an end status int
     */
    public int run(String args[]) {
        try {
            address = args.length > 0 ? InetAddress.getByName(args[0]) : InetAddress.getLocalHost();
            port = args.length > 1 ? Integer.parseInt(args[1]) : 3400;
            if(port < 1024) {
                throw new SCPException("Using a port number 1023 or lower may interrupt system operations");
            }
            welcomeMessage = args.length > 2 ? args[2] : "Welcome to SCP";
            msgArea.append(String.format("Starting server on %s:%d\n", address.getHostAddress(), port));
            startSocket();
            msgArea.append("Started server\n");
            while(true) {
                hostConnection();
            }
        } catch(SCPException SCPe) {
            System.err.println("Error: " + SCPe.getMessage());
            return errorCodes.SCPERROR.value();
        } catch(IOException ioe)  {
            System.err.println("Error: " + ioe.getMessage());
            return errorCodes.IOERROR.value();
        }
    }
    /**
     * Host a client connection
     */
    private void hostConnection() throws SCPException, IOException {
        try {
            msgArea.append("Waiting for client to connect\n");
            acceptClient();
            msgArea.append("Client successfully connected\n");
            msgArea.append("Waiting for client to SCP connect\n");
            username = clientConnect();
            username = username.substring(1, username.length() - 1); // remove quotes
            if(username == "") { // Can't have a blank username anyway
                cliSocket.close();
                msgArea.append("Rejected client for time differential greater than 5, trying again");
            } else {
                SCPAccept();
                if(acknowledged()) {
                    msgArea.append(String.format("User %s has connected to SCP\n\n", username));
                    String message = welcomeMessage + "\n"; // send welcome message + chat rules to client
                    msgArea.append("Waiting for message to send...\n");
                    out.println(SCP.message(address.getHostAddress(), port, message));
                    msgArea.append("Message Sent: " + message + "\n");
                    while(!disconnect) {
                        messageLoop();
                        while(recvMsg.isAlive() || !disconnect);
                    }
                }
            }
        } catch(NullPointerException npe) {
            msgArea.append("\nError: unexpected cut-off from client, looking for new client\n");
        }
    }
    /**
     * Start a server socket
     * @param hostAddress The ip address for the host server
     * @param port The port to run the server on
     * @return True on successful start
     */
    private boolean startSocket() throws IOException {
        serverSocket = new ServerSocket(port, BACKLOG, address);
        return true;
    }
    /**
     * Accept a client into the server
     * @return True on client connection
     */
    private boolean acceptClient() throws IOException {
        cliSocket = serverSocket.accept();
        out = new PrintWriter(cliSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));
        return true;
    }
    /**
     * Recieve a SCP connection
     * @return Client's username
     */
    private String clientConnect() throws SCPException, IOException {
        String inLine, result = "", packet = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            packet += inLine + "\n";
        }
        try {
            result = SCP.parseConnect(packet, address.getHostAddress(), port);
        } catch(TimeDiffException tde) {
            reject(tde.getTimeDiff());
        }
        return result;
    }
    /**
     * Reject client for taking too long
     * @param timeDiff Difference in time of client request to server processing it
     */
    private void reject(int timeDiff) {
        out.println(SCP.reject(timeDiff, cliSocket.getLocalAddress().getHostAddress()));
    }
    /**
     * Send a SCP connect message to the client
     * @param username user specified name
     */
    private void SCPAccept() {
        out.println(SCP.accept(username, address.getHostAddress(), port));
    }
    /**
     * Check if the client has been acknowledged by the server
     */
    private boolean acknowledged() throws SCPException, IOException {
        String inLine, packet = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            packet += inLine + "\n";
        }
        boolean result = SCP.parseAcknowledge(packet, address.getHostAddress(), port, username);
        if(result) {
            return true;
        } else {
            throw new SCPException("SCP ACKNOWLEDGE", packet);
        }
    }
}
