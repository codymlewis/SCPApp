import java.util.Scanner;
import java.time.Instant;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * ChatServer.java - Seng3400A1
 * A socket based half duplex chat server
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class ChatServer {
    private SCP scp = new SCP();
    private ServerSocket serverSocket;
    private Socket cliSocket;
    private PrintWriter out;
    private BufferedReader in;
    private InetAddress hostInetAddress;
    private int port;
    private String username;
    private static final int BACKLOG = 1; // Max length for queue of messages
    public static void main(String args[]) {
        ChatServer cs = new ChatServer();
        cs.run(args);
        System.exit(0);
    }
    /**
     * The main flow of the program
     * @param args The arguments sent in with the program
     */
    public void run(String args[]) {
        try {
            hostInetAddress = args.length > 0 ? InetAddress.getLocalHost() : InetAddress.getLocalHost();
            port = args.length > 1 ? Integer.parseInt(args[1]) : 3400;
            String welcomeMessage = args.length > 2 ? args[2] : "Welcome to SCP";
            System.out.println(String.format("Starting server on %s:%d", hostInetAddress.getHostAddress(), port));
            startSocket();
            System.out.println("Started server");
            hostConnection(welcomeMessage);
        } catch(SCPException scpe) {
            System.err.println(scpe.getMessage());
        } catch(IOException ioe)  {
            System.err.println(ioe.getMessage());
        }
    }
    /**
     * Host a client connection
     * @param welcomeMessage the welcome message sent to the client
     */
    private void hostConnection(String welcomeMessage) throws SCPException, IOException {
        System.out.println("Waiting for client to connect");
        acceptClient();
        System.out.println("Client successfully connected");
        System.out.println("Waiting for client to SCP connect");
        username = clientConnect();
        username = username.substring(1, username.length() - 1); // remove quotes
        if(username == "") {
            cliSocket.close();
            System.out.println("Rejected client for time differential greater than 5");
        }
        scpAccept();
        if(acknowledged()) {
            System.out.println(String.format("User %s has connected to SCP", username));
            Scanner console = new Scanner(System.in);
            String message = welcomeMessage;
            while(true) {
                out.println(scp.message(hostInetAddress.getHostAddress(), port, message));
                System.out.println(String.format("%s is typing...", username));
                System.out.print(recieveMessage());
                System.out.print("Send a message: ");
                message = console.next();
                if(message == "DISCONNECT") {
                    break;
                }
            }
            console.close();
        }
    }
    /**
     * Start a server socket
     * @param hostAddress The ip address for the host server
     * @param port The port to run the server on
     * @return True on successful start
     */
    private boolean startSocket() throws IOException {
        serverSocket = new ServerSocket(port, BACKLOG, hostInetAddress);
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
     * Recieve a scp connection
     * @return Client's username
     */
    private String clientConnect() throws SCPException, IOException {
        String inLine, result = "", packet = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            packet += inLine + "\n";
        }
        try {
            result = scp.parseConnect(packet);
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
        out.println(scp.reject(timeDiff, cliSocket.getLocalAddress().getHostAddress()));
    }
    /**
     * Send a SCP connect message to the client
     * @param username user specified name
     */
    private void scpAccept() {
        out.println(scp.accept(username, "127.0.0.1", 3400));
    }
    /**
     * Check if the client has been acknowledged by the server
     */
    private boolean acknowledged() throws SCPException, IOException {
        String inLine, packet = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            packet += inLine;
        }
        return scp.parseAcknowledge(packet) == "success";
    }
    /**
     * Recieve a message from the client
     */
    private String recieveMessage() throws SCPException, IOException {
        String packet = "", line = "";
        while((line = in.readLine()).compareTo("SCP END") != 0) {
            packet += line + "\n";
        }
        return scp.parseMessage(packet, "127.0.0.1", 3400);
    }
}
