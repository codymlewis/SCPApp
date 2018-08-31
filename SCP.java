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
    public static String connect(String username, String address, int port) {
        return String.format(
            "SCP CONNECT\nSERVERADDRESS %s\nSERVERPORT %d\nREQUESTCREATED %d\nUSERNAME \"%s\"\nSCP END",
            address, port, Instant.now().getEpochSecond(), username
        );
    }
    /**
     * Acknowledge a disconnect
     * @return A SCP Acknowledge packet for disconnection
     */
    public static String acknowledge() {
        return "SCP ACKNOWLEDGE\nSCP END";
    }
    /**
     * Acknowledge a connect
     * @param username username of the sender
     * @param address the server address
     * @param port the server port
     * @return an acknowledge packet
     */
    public static String acknowledge(String username, String address, int port) {
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
    public static String accept(String username, String address, int port) {
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
    public static String message(String address, int port, String contents) {
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
    public static String reject(int timeDiff, String address) {
        return String.format(
                "SCP REJECT\nTIMEDIFFERENTIAL %d\nREMOTEADDRESS %s\nSCP END",
                timeDiff, address
            );
    }
    /**
     * Disconnect
     * @return A disconnect packet
     */
    public static String disconnect() {
        return "SCP DISCONNECT\nSCP END";
    }
    // packet parsing
    /**
     * Parse a SCP connect packet
     * @param packet the connect packet
     * @param address the caller's ip address
     * @return The client's username
     */
    public static String parseConnect(String packet, String address, int port) throws TimeDiffException, SCPException, IOException {
        String username = "";
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        boolean firstLine = true;
        String line;
        int lineNum = 1;
        while((line = sstream.readLine()) != null) { // check that the packet matchs expected input
            if(firstLine) {
                if(line.indexOf("SCP CONNECT") < 0) {
                    sstream.close();
                    throw new SCPException("SCP CONNECT", line);
                }
                firstLine = false;
            } else {
                if(lineNum == 2)  {
                    if(line.indexOf("SERVERADDRESS") == -1 && line.indexOf(address) == -1) {
                        sstream.close();
                        throw new SCPException("SERVERADDRESS " + address, line);
                    }
                } else if(lineNum == 3) {
                    if(line.indexOf("SERVERPORT") == -1 && line.indexOf(String.format("%d", port)) == -1) {
                        sstream.close();
                        throw new SCPException("SERVERPORT " + port, line);
                    }
                } else if(lineNum == 4) {
                    if(line.indexOf("REQUESTCREATED") > -1) {
                        int requestTime = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
                        int timeDiff = findTimeDiff(requestTime);
                        if(timeDiff > 5) {
                            sstream.close();
                            throw new TimeDiffException(timeDiff);
                        }
                    } else {
                        sstream.close();
                        throw new SCPException("REQUESTCREATED epochtime", line);
                    }
                } else if(line.indexOf("USERNAME") > -1 && lineNum == 5) {
                    username = line.substring(line.indexOf(" ") + 1);
                }
            }
            ++lineNum;
        }
        sstream.close();
        if(lineNum < 5) {
            throw new SCPException("SCP connect was not the right length");
        }
        return username;
    }
    /**
     * Find passes epoch time from specified time
     * @param otherTime specified time
     * @return The difference in times
     */
    private static int findTimeDiff(int otherTime) {
        return Math.abs((int)Instant.now().getEpochSecond() - otherTime);
    }
    /**
     * Parse a SCP acknowledge packet
     * @param packet a SCP packet
     * @return true on successful parse else false
     */
    public static boolean parseAcknowledge(String packet) {
        if (packet.indexOf("SCP ACKNOWLEDGE") > -1) { // packet is only 2 lines
            return true;
        }
        return false;
    }
    /**
     * Parse an extended acknowledge packet
     * @param packet a SCP packet
     * @param address the server ip address
     * @param port the server's port
     * @param username the client's username
     * @return true on successful parse
     */
    public static boolean parseAcknowledge(String packet, String address, int port, String username) throws SCPException, IOException {
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        boolean firstLine = true;
        String line;
        int lineNum = 1;
        while((line = sstream.readLine()) != null) {
            if(firstLine) {
                if(line.indexOf("SCP ACKNOWLEDGE") < 0) {
                    sstream.close();
                    throw new SCPException("SCP ACKNOWLEDGE", line);
                }
                firstLine = false;
            }
            if(lineNum == 2) {
                if(line.indexOf("USERNAME") == -1 || line.indexOf(username) == -1) {
                    sstream.close();
                    throw new SCPException("USERNAME " + username, line);
                }
            } else if(lineNum == 3)  {
                if(line.indexOf("SERVERADDRESS") == -1 || line.indexOf(address) == -1) {
                    sstream.close();
                    throw new SCPException("SERVERADDRESS " + address, line);
                }
            } else if(lineNum == 4) {
                if(line.indexOf("SERVERPORT") == -1 || line.indexOf(String.format("%d", port)) == -1) {
                    sstream.close();
                    throw new SCPException("SERVERPORT " + port, line);
                }
            }
            ++lineNum;
        }
        if(lineNum < 4) {
            throw new SCPException("SCP acknowledge was not the right length");
        }
        sstream.close();
        return true;
    }
    /**
     * Parse a SCP accept packet
     * @param packet a SCP packet
     * @param username the Client's username
     * @return true if successful parse else false
     */
    public static boolean parseAccept(String packet, String username, String address, int port) throws SCPException, IOException {
        boolean accept = false;
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        String line;
        int lineNum = 1;
        while((line = sstream.readLine()) != null) {
            if(!accept) {
                if(line.indexOf("SCP ACCEPT") > -1) {
                    accept = true;
                } else {
                    throw new SCPException("SCP ACCEPT", line);
                }
            }
            if(lineNum == 2) {
                if(line.indexOf("USERNAME") == -1 || line.indexOf(username) == -1) {
                    throw new SCPException("USERNAME " + username, line);
                }
            } else if(lineNum == 3) {
                if(line.indexOf("CLIENTADDRESS") == -1 || line.indexOf(address) == -1) {
                    throw new SCPException("CLIENTADDRESS " + address, line);
                }
            } else if(lineNum == 4) {
                if(line.indexOf("CLIENTPORT") == -1 || line.indexOf(String.format("%d", port)) == -1) {
                    throw new SCPException("CLIENTPORT " + port, line);
                }
            }
            ++lineNum;
        }
        if(lineNum < 4) {
            throw new SCPException("SCP accept was not the right length");
        }
        return true;
    }
    /**
     * Parse a SCP message
     * @param packet the SCP packet
     * @param address the caller's ip address
     * @param port the caller's port used for the application
     * @return a String of the Message contents
     */
    public static String parseMessage(String packet, String address, int port) throws SCPException, IOException {
        boolean firstLine = true;
        BufferedReader sstream = new BufferedReader(new StringReader(packet));
        String line;
        int lineNum = 1;
        String message = "";
        while((line = sstream.readLine()) != null) {
            if(firstLine) {
                if(line.indexOf("SCP CHAT") == -1) {
                    throw new SCPException("SCP CHAT", line);
                }
                firstLine = false;
            }
            if(lineNum == 2) {
                if(line.indexOf("REMOTEADDRESS") == -1 || line.indexOf(address) == -1) {
                    throw new SCPException("REMOTEADDRESS " + address, line);
                }
            } else if(lineNum == 3) {
                if(line.indexOf("REMOTEPORT") == -1 || Integer.parseInt(line.substring(line.indexOf(" ") + 1)) != port) {
                    throw new SCPException("REMOTEPORT " + port, line);
                }
            } else if(lineNum == 4) {
                if(line.indexOf("MESSAGECONTENT") > -1) {
                    message = packet.substring(packet.indexOf("\n\n") + 2);
                    break;
                } else {
                    throw new SCPException("MESSAGECONTENT", line);
                }
            }
            ++lineNum;
        }
        return message;
    }
    /**
     * Parse a SCP Disconnect
     * @param packet a SCP packet
     * @return true if successful parse else false
     */
    public static boolean parseDisconnect(String packet) {
        if (packet.indexOf("SCP DISCONNECT") > -1) {
            return true;
        }
        return false;
    }
}
