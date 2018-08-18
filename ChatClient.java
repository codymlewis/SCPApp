import java.util.Scanner;
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
    private SCP scp = new SCP();
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
            String hostName = args.length > 0 ? args[0] : "localhost";
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 3400;
            System.out.println(String.format("Connecting to %s:%d", hostName, port));
            connectToServer(hostName, port);
            System.out.println("Connected to server");
            Scanner console = new Scanner(System.in);
            System.out.print("Input a username: ");
            String username = console.next();
            scpConnect(hostName, port, username);
            System.out.println(in.readLine());
            console.close();
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
    private boolean scpConnect(String hostName, int port, String username) throws IOException {
        String connectionString = scp.connect(username, hostName, port);
        out.println(connectionString);
        if(scpDecide(username)) {
            scpAknowledge(username);
            return true;
        }
        return false;
    }
    private boolean scpDecide(String username) throws IOException {
        String inLine;
        boolean accept = false;
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            if(!accept) {
                if(inLine.indexOf("SCP ACCEPT") > -1) {
                    accept = true;
                } else {
                    throw new IOException();
                }
            }
            if(inLine.indexOf("USERNAME") > -1) {
                if(inLine.indexOf(username) == -1) {
                    return false;
                }
            }
        }
        return true;
    }
    private void scpAknowledge(String username) {
        out.println(scp.acknowledge(username, "127.0.0.1", 3400));
    }
}
