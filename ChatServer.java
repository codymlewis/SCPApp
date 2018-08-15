import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
/**
 * ChatServer.java - Seng3400A1
 * A socket based half duplex chat server
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private Socket cliSocket;
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
        InetAddress hostInetAddress;
        try {
            hostInetAddress = InetAddress.getLocalHost();
            int port = Integer.parseInt(args[1]);
            System.out.println(String.format("Starting server on %s:%d", hostInetAddress.getHostAddress(), port));
            startSocket(hostInetAddress, port);
        } catch(UnknownHostException uhe) {
            System.err.println("That host does not exist");
        }
    }
    /**
     * Start a server socket
     * @param hostAddress The ip address for the host server
     * @param port The port to run the server on
     * @return True on successful start else false
     */
    private boolean startSocket(InetAddress hostAddress, int port) {
        try {
            serverSocket = new ServerSocket(port, BACKLOG, hostAddress);
            cliSocket = serverSocket.accept();
            return true;
        } catch(IOException ioe) {
            System.err.println("No client connected");
            return false;
        }
    }
}