import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * ChatClient.java - SENG3400A1
 * A socket based half duplex chat client
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class ChatClient extends Chat {
    /**
     * The main thread
     * @param args command line arguments
     */
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
            address = args.length > 0 ? InetAddress.getByName(args[0]) : InetAddress.getLocalHost();
            port = args.length > 1 ? Integer.parseInt(args[1]) : 3400;
            if(port < 1024) {
                throw new SCPException("Using a port number 1023 or lower may interrupt system operations");
            }
            System.out.println(String.format("Connecting to %s:%d", address.getHostAddress(), port));
            connectToServer();
            System.out.println("Connected to server");
            System.out.print("Input a username: ");
            username = console.next();
            scpConnect();
            System.out.println("Connected to SCP");
            messageLoop();
            console.close();
        } catch(SCPException scpe) {
            System.err.println(scpe.getMessage());
        } catch(UnknownHostException uhe) {
            System.err.println(uhe.getMessage());
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch(NullPointerException npe) {
            System.out.println("\nError: unexpected cutoff from Server, ending program");
        }
    }
    /**
     * Loop for sending and recieving messages
     */
    private void messageLoop() throws SCPException, IOException, NullPointerException {
        String message;
        String recievedMessage;
        boolean disconnect = false;
        while(!disconnect) {
            recievedMessage = recieveMessage();
            System.out.println();
            if(recievedMessage == "DISCONNECT") {
                out.println(scp.acknowledge());
                System.out.println("Server disconnected");
                disconnect = true;
                break;
            }
            System.out.print(String.format("Server: %s", recievedMessage));
            System.out.print("Send a message: ");
            message = textToMessage();
            if(message.compareTo("DISCONNECT") == 0) {
                out.println(scp.disconnect());
                if(recieveMessage().compareTo("ACKNOWLEDGE") == 0) {
                    System.out.println("Disconnected from server");
                    disconnect = true;
                    break;
                } else {
                    throw new SCPException("Server did not acknowledge disconnect");
                }
            }
            System.out.println("Waiting for message to send");
            out.println(scp.message(address.getHostAddress(), port, message));
            System.out.print("Server is typing...");
        }
    }
    /**
     * Connect to the server
     * @param hostName the name of the server
     * @param port the port number the server is running on
     * @return true on successful connection
     */
    private boolean connectToServer() throws UnknownHostException, IOException {
        cliSocket = new Socket(address.getHostAddress(), port);
        out = new PrintWriter(cliSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));
        return true;
    }
    /**
     * Send the SCP connection packet
     * @param hostName the name of the server
     * @param port the port number the server is running on
     * @param username client specified username
     * @return true on packet send
     */
    private boolean scpConnect() throws SCPException, IOException {
        String connectionString = scp.connect(username, address.getHostAddress(), port);
        out.println(connectionString);
        if(scpDecide()) {
            scpAcknowledge();
            return true;
        }
        return false;
    }
    /**
     * Find out whether the server accepted or rejected by the server
     * @return true if accepted, false if rejected
     */
    private boolean scpDecide() throws SCPException, IOException {
        String inLine, packet = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            packet += inLine + "\n";
        }
        return scp.parseAccept(packet, username, address.getHostAddress(), port);
    }
    /**
     * Send a acknowledge SCP packet to the server
     */
    private void scpAcknowledge() {
        out.println(scp.acknowledge(username, address.getHostAddress(), port));
    }
}
