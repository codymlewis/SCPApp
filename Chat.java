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
    protected Scanner console;
    protected PrintWriter out;
    protected BufferedReader in;
    protected Socket cliSocket;
    protected InetAddress address;
    protected int port;
    protected String username;
    /**
     * Enum of the various error codes returned by the classes
     */
    protected enum errorCodes {
        SCPERROR(-1),
        UNKNOWNHOSTERROR(-2),
        IOERROR(-3),
        NULLERROR(-4);

        private final int code; // error code variable
        /**
         * Default constructor
         */
        private errorCodes(int code) { this.code = code; }
        /**
         * Get the error code value
         * @return the error code of this
         */
        public int value() { return code; }
    }
    /**
     * Default Constructor
     */
    public Chat() {
        console = new Scanner(System.in);
    }
    /**
     * The chat message rules
     * @return A String containing the rules of the chat's messaging
     */
    protected String rules() {
        return "Press enter twice to send a message,\nType DISCONNECT to end the chat";
    }
    /**
     * Take an input from the users and give an out suitable to put into a message
     * @return A String of the input formatted to be embedded in a SCP message
     */
    protected String textToMessage() {
        String line = console.nextLine(), message = "";
        boolean empty = true; // Make sure an empty message is not sent
        do {
            if(line.length() != 0) {
                empty = false;
            }
            if(line.compareTo("DISCONNECT") == 0) {
                return "DISCONNECT";
            }
            message += "\n" + line;
            line = console.nextLine();
        } while(empty || line.length() != 0);
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
        if(SCP.parseDisconnect(packet)) {
            return "DISCONNECT";
        }
        if(SCP.parseAcknowledge(packet)) {
            return "ACKNOWLEDGE";
        }
        return SCP.parseMessage(packet, address.getHostAddress(), port);
    }
    /**
     * Send a SCP disconnect to the other user
     */
    protected void disconnect() {
        out.println(SCP.disconnect());
    }
}
