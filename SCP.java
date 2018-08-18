import java.time.Instant;
/**
 * SCP.java - Seng3400A1
 * SCP parsing/packet creating library
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class SCP {
    // Packet generating methods
    /**
     * Generate the scp connect packet
     * @param username username of the sender
     * @param address the server address
     * @param port port number of the server
     * @return A SCP Connect packet
     */
    public String connect(String username, String address, int port) {
        return String.format(
            "SCP CONNECT\nSERVERADDRESS %s\nSERVERPORT %d\nREQUESTCREATED %d\nUSERNAME \"%s\"\nSCP END",
            address, port, Instant.now().getEpochSecond(), username
        );
    }
    /**
     * Acknowledge a disconnect
     * @return A SCP Acknowledge packet for disconnection
     */
    public String acknowledge() {
        return "SCP ACKNOWLEDGE\nSCP END";
    }
    /**
     * Acknowledge a connect
     * @param username username of the sender
     * @param address the server address
     * @param port the server port
     * @return an acknowledge packet
     */
    public String acknowledge(String username, String address, int port) {
        return String.format(
                    "SCP ACKNOWLEDGE\nUSERNAME \"%s\"\nSERVERADDRESS %s\nSERVERPORT %d\nSCP END",
                    username, address, port
                );
    }
    /**
     * Accept a connection
     * @param username the username of the sender
     * @param address address of the client
     * @param port port of the client
     * @return an accept packet
     */
    public String accept(String username, String address, int port) {
        return String.format(
                "SCP ACCEPT\nUSERNAME %s\nCLIENTADDRESS %s\nCLIENTPORT %d\nSCP END",
                username, address, port
            );
    }
    /**
     * Send a message
     * @param address the address of the recipient
     * @param port the port of the recipient
     * @param contents the contents of the message
     * @return a message packet
     */
    public String message(String address, int port, String contents) {
        return String.format(
                    "SCP CHAT\nREMOTEADDRESS %s\nREMOTEPORT %d\nMESSAGECONTENT\n\n%s\nSCP END",
                    address, port, contents
                );
    }
    /**
     * Reject a connection
     * @param timeDiff the difference in connection times
     * @param address address of the recipient
     * @return a reject packet
     */
    public String reject(int timeDiff, String address) {
        return String.format(
                "SCP REJECT\nTIMEDIFFERENTIAL %d\nREMOTEADDRESS %s\nSCP END",
                timeDiff, address
            );
    }
    /**
     * Disconnect
     * @return A disconnect packet
     */
    public String disconnect() {
        return "SCP DISCONNECT\nSCP END";
    }
    // packet parsing
    
}