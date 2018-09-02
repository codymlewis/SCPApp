import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * ChatClient.java
 * A socket based half duplex chat client
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class ChatClient extends Chat {
    public ChatClient(String title) { super(title); }
    /**
     * The main thread
     * @param args command line arguments
     */
    public static void main(String args[]) {
        ChatClient cc = new ChatClient("SCP Client");
        int exitVal = cc.run(args);
        System.exit(exitVal);
    }
    /**
     * Main flow of the program
     * @param args arguments sent in with the program
     * @return an end status int
     */
    public int run(String args[]) {
        try {
            address = args.length > 0 ? InetAddress.getByName(args[0]) : InetAddress.getLocalHost();
            port = args.length > 1 ? Integer.parseInt(args[1]) : 3400;
            msgArea.append(String.format("Connecting to %s:%d\n", address.getHostAddress(), port));
            connectToServer();
            msgArea.append("Connected to server\n");
            msgArea.append("Exchanging keys with the server\n");
            keyExchange();
            username = args.length > 2 ? args[2] : "Client";
            otherUsername = "Server";
            SCPConnect();
            msgArea.append("Connected to SCP\n");
            while(!disconnect) {
                messageLoop();
                while(recvMsg.isAlive() || !disconnect);
            }
            return 0;
        } catch(SCPException SCPe) {
            System.err.println("Error: " + SCPe.getMessage());
            return errorCodes.SCPERROR.value();
        } catch(UnknownHostException uhe) {
            System.err.println("Error: " + uhe.getMessage());
            return errorCodes.UNKNOWNHOSTERROR.value();
        } catch(IOException ioe) {
            System.err.println("Error: " + ioe.getMessage());
            return errorCodes.IOERROR.value();
        } catch(NullPointerException npe) {
            System.err.println("\nError: unexpected cutoff from Server, ending program");
            return errorCodes.NULLERROR.value();
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
     * perform a diffie-hellman key exchange
     * @return true on completion
     */
    private boolean keyExchange() throws IOException {
        BigInteger prime = DH.genPrime(16);
        out.println(prime); // send prime to server
        BigInteger priKey = DH.genPrivateKey(prime);
        BigInteger pubKey = DH.genPublicKey(priKey, prime);
        out.println(pubKey);
        sessionKey = DH.genSessionKey(new BigInteger(in.readLine()), priKey, prime);
        return true;
    }
    /**
     * Send the SCP connection packet
     * @param hostName the name of the server
     * @param port the port number the server is running on
     * @param username client specified username
     * @return true on packet send
     */
    private boolean SCPConnect() throws SCPException, IOException {
        String connectionString = SCP.connect(username, address.getHostAddress(), port);
        out.println(connectionString);
        if(SCPDecide()) {
            SCPAcknowledge();
            return true;
        }
        return false;
    }
    /**
     * Find out whether the server accepted or rejected by the server
     * @return true if accepted, false if rejected
     */
    private boolean SCPDecide() throws SCPException, IOException {
        String inLine, packet = "";
        while((inLine = in.readLine()).compareTo("SCP END") != 0) {
            packet += inLine + "\n";
        }
        return SCP.parseAccept(packet, username, address.getHostAddress(), port);
    }
    /**
     * Send a acknowledge SCP packet to the server
     */
    private void SCPAcknowledge() {
        out.println(SCP.acknowledge(username, address.getHostAddress(), port));
    }
}
