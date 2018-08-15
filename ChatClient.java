import java.util.Scanner;
import java.time.Instant;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
/**
 * ChatClient.java - SENG3400A1
 * A socket based half duplex chat client
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    public static void main(String args[]) {
        ChatClient cc = new ChatClient();
        cc.run(args);
        System.exit(0);
    }
    /**
     * Main flow of the program
     * @param args arguments sent in with the program
     */
    public void run(String args[]) {
        try {
            String hostName = args[0];
            int port = Integer.parseInt(args[1]);
            System.out.println(String.format("Connecting to %s:%d", hostName, port));
            if(connectToServer(hostName, port)) {
                System.out.println("Connected to server");
                Scanner console = new Scanner(System.in);
                System.out.print("Input a username: ");
                String username = console.next();
                scpConnect(hostName, port, username);
                System.out.println(in.readLine());
            } else {
                System.err.println("Failed to connect to the server");
            }
        } catch(UnknownHostException uhe) {
            System.err.println("Specified host does not exist");
        } catch(IOException ioe) {
            System.err.println("Input/Output error");
        }
    }
    /**
     * Connect to the server
     * @param hostName the name of the server
     * @param port the port number the server is running on
     * @return true on successful connection
     */
    private boolean connectToServer(String hostName, int port) throws UnknownHostException, IOException {
        socket = new Socket(hostName, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return true;
    }
    /**
     * Send the SCP connection packet
     * @param hostName the name of the server
     * @param port the port number the server is running on
     * @param username client specified username
     * @return true on packet send
     */
    private boolean scpConnect(String hostName, int port, String username) {
        String connectionString = String.format(
            "SCP CONNECT\nSERVERADDRESS %s\nSERVERPORT %d\nREQUESTCREATED %d\nUSERNAME %s\nSCP END",
            hostName, port, Instant.now().getEpochSecond(), username
        );
        out.println(connectionString);
        return true;
    }
}