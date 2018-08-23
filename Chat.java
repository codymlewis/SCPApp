import java.util.Scanner;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
/**
 * Chat.java - SENG3400A1
 * Shared functions and variables between ChatServer and ChatClient
 *
 * @author Cody Lewis
 * @since 2018-08-19
 */
public class Chat {
    protected SCP scp; // Must be a class as it uses non-static variables
    protected Scanner console;
    protected PrintWriter out;
    protected BufferedReader in;
    protected Socket cliSocket;
    protected InetAddress address;
    protected int port;
    protected String username;
    /**
     * Default Constructor
     */
    public Chat() {
        scp = new SCP();
        console = new Scanner(System.in);
    }
    protected String rules() {
        return "Press enter twice to send a message,\nType DISCONNECT to end the chat";
    }
    /**
     * Take an input from the users and give an out suitable to put into a message
     * @return A String of the input formatted to be embedded in a SCP message
     */
    protected String textToMessage() {
        String line = console.nextLine(), message = "";
        do {
            if(line.compareTo("DISCONNECT") == 0) {
                return "DISCONNECT";
            }
            message += "\n" + line;
            line = console.nextLine();
        } while(line.length() != 0);
        return message.substring(1); // get rid of leading \n
    }
    /**
     * Recieve a message from the other user
     */
    protected String recieveMessage() throws SCPException, IOException {
        String packet = "", line = "";
        while((line = in.readLine()).compareTo("SCP END") != 0) {
            packet += line + "\n";
        }
        if(scp.parseDisconnect(packet)) {
            return "DISCONNECT";
        }
        if(scp.parseAcknowledge(packet)) {
            return "ACKNOWLEDGE";
        }
        return scp.parseMessage(packet, address.getHostAddress(), port);
    }
    /**
     * Send a SCP disconnect to the other user
     */
    protected void disconnect() {
        out.println(scp.disconnect());
    }
}