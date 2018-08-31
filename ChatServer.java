import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
/**
 * ChatServer.java - Seng3400A1
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
    /**
     * The main thread
     * @param args command line arguments
     */
    public static void main(String args[]) {
        ChatServer cs = new ChatServer();
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
            System.out.println(String.format("Starting server on %s:%d", address.getHostAddress(), port));
            startSocket();
            System.out.println("Started server");
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
            System.out.println("Waiting for client to connect");
            acceptClient();
            System.out.println("Client successfully connected");
            System.out.println("Waiting for client to SCP connect");
            username = clientConnect();
            username = username.substring(1, username.length() - 1); // remove quotes
            if(username == "") { // Can't have a blank username anyway
                cliSocket.close();
                System.out.println("Rejected client for time differential greater than 5, trying again");
            } else {
                SCPAccept();
                if(acknowledged()) {
                    System.out.println(String.format("User %s has connected to SCP", username));
                    System.out.println();
                    messageLoop();
                }
            }
        } catch(NullPointerException npe) {
            System.out.println("\nError: unexpected cut-off from client, looking for new client");
        }
    }
    /**
     * Loop for sending a recieving messages
     */
    private void messageLoop() throws SCPException, IOException {
        String message = welcomeMessage + "\n" + rules() + "\n"; // send welcome message + chat rules to client
        String recievedMessage;
        boolean disconnect = false;
        System.out.println(rules());
        while(!disconnect) { // I assume this gets sigkilled to end
            System.out.println("Waiting for message to send");
            out.println(SCP.message(address.getHostAddress(), port, message));
            System.out.print(String.format("%s is typing...", username));
            recievedMessage = recieveMessage();
            System.out.println();
            if(recievedMessage == "DISCONNECT") {
                out.println(SCP.acknowledge());
                System.out.println("Client disconnected");
                disconnect = true;
                break;
            }
            System.out.print(String.format("%s: %s", username, recievedMessage));
            System.out.print("Send a message: ");
            message = textToMessage();
            if(message.compareTo("DISCONNECT") == 0) {
                out.println(SCP.disconnect());
                if(recieveMessage().compareTo("ACKNOWLEDGE") == 0) {
                    disconnect = true;
                    System.out.println("Successfully disconnected from Client");
                    break;
                } else {
                    throw new SCPException("Client did not acknowledge disconnect");
                }
            }
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
