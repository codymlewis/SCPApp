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
    private ServerSocket serverSocket;
    private Socket cliSocket;
    private PrintWriter out;
    private BufferedReader in;
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
            InetAddress hostInetAddress = args.length > 0 ? InetAddress.getLocalHost() : InetAddress.getLocalHost();
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 3400;
            String welcomeMessage = args.length > 2 ? args[2] : "Welcome to SCP";
            System.out.println(String.format("Starting server on %s:%d", hostInetAddress.getHostAddress(), port));
            startSocket(hostInetAddress, port);
            System.out.println("Started server");
            System.out.println("Waiting for client to connect");
            acceptClient();
            System.out.println("Client successfully connected");
            System.out.println("Waiting for client to SCP connect");
            String username = clientConnect();
            out.println(welcomeMessage);
        } catch(IOException ioe)  {
            System.err.println("Input/Output error");
        }
    }
    /**
     * Start a server socket
     * @param hostAddress The ip address for the host server
     * @param port The port to run the server on
     * @return True on successful start
     */
    private boolean startSocket(InetAddress hostAddress, int port) throws IOException {
        serverSocket = new ServerSocket(port, BACKLOG, hostAddress);
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
     * @return true on packet reception and parse
     */
    private String clientConnect() throws IOException {
        String inLine;
        boolean isScpConnect = false;
        String username = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            if(!isScpConnect) {
                if(inLine.indexOf("SCP CONNECT") < 0) {
                    throw new IOException();
                } else {
                    isScpConnect = true;
                }
            } else {
                if(inLine.indexOf("SERVERADDRESS") > -1) {
                    // Validate
                } else if(inLine.indexOf("SERVERPORT") > -1) {
                    // Validate
                } else if(inLine.indexOf("REQUESTCREATED") > -1) {
                    int requestTime = Integer.parseInt(inLine.substring(inLine.indexOf(" ") + 1));
                    int currentTime = (int)Instant.now().getEpochSecond();
                    if(!(requestTime >= currentTime - 5 && requestTime <= currentTime + 5)) {
                        throw new IOException();
                    }
                } else if(inLine.indexOf("USERNAME") > -1) {
                    username = inLine.substring(inLine.indexOf(" "));
                }
            }
        }
        return username;
    }
}