import java.time.Instant;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
/**
 * SCP.java - Seng3400A1
 * SCP parsing/packet creating library
 *
 * @author Cody Lewis
 * @since 2018-08-10
 * TODO Add all detail checks to the parsing
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
    /**
     * Parse a SCP connect packet
     * @param packet the connect packet
     * @return A Result object containing final status and corresponding data
     */
    public String parseConnect(String packet, String address, int port) throws TimeDiffException, SCPException, IOException {
        String username = "";
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        boolean firstLine = true;
        String line;
        while((line = sstream.readLine()) != null) {
            if(firstLine) {
                if(line.indexOf("SCP CONNECT") < 0) {
                    sstream.close();
                    throw new SCPException("SCP CONNECT", line);
                }
                firstLine = false;
            }
            if(line.indexOf("REQUESTCREATED") > -1) {
                int requestTime = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
                int timeDiff = findTimeDiff(requestTime);
                if(timeDiff > 5) {
                    sstream.close();
                    throw new TimeDiffException(timeDiff);
                }
            } else if(line.indexOf("USERNAME") > -1) {
                username = line.substring(line.indexOf(" ") + 1);
            }
        }
        sstream.close();
        return username;
    }
    /**
     * Find passes epoch time from specified time
     * @param otherTime specified time
     * @return The difference in times
     */
    private int findTimeDiff(int otherTime) {
        return Math.abs((int)Instant.now().getEpochSecond() - otherTime);
    }
    public boolean parseAcknowledge(String packet) {
        if (packet.indexOf("SCP ACKNOWLEDGE") > -1) {
            return true;
        }
        return false;
    }
    public boolean parseAccept(String packet, String username) throws SCPException, IOException {
        boolean accept = false;
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        String line;
        while((line = sstream.readLine()) != null) {
            if(!accept) {
                if(line.indexOf("SCP ACCEPT") > -1) {
                    accept = true;
                } else {
                    throw new SCPException("SCP ACCEPT", line);
                }
            }
            if(line.indexOf("USERNAME") > -1) {
                if(line.indexOf(username) == -1) {
                    throw new SCPException(username, line);
                }
            }
        }
        return true;
    }
    public String parseMessage(String packet, String address, int port) throws SCPException, IOException {
        boolean firstLine = true;
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        String line;
        String message = "";
        while((line = sstream.readLine()) != null) {
            if(firstLine) {
                if(line.indexOf("SCP CHAT") == -1) {
                    throw new SCPException("SCP CHAT", line);
                }
                firstLine = false;
            }
            if(line.indexOf("REMOTEADDRESS") > -1) {
                if(line.substring(line.indexOf(" ") + 1).indexOf(address) == -1) {
                    throw new SCPException("REMOTEADDRESS " + address, line);
                }
            } else if(line.indexOf("REMOTEPORT") > -1) {
                if(Integer.parseInt(line.substring(line.indexOf(" ") + 1)) != port) {
                    throw new SCPException("REMOTEPORT " + port, line);
                }
            } else if(line.indexOf("MESSAGECONTENT") > -1) {
                message = packet.substring(packet.indexOf("\n\n") + 2);
                break;
            }
        }
        return message;
    }
    public boolean parseDisconnect(String packet) {
        if (packet.indexOf("SCP DISCONNECT") > -1) {
            return true;
        }
        return false;
    }
}