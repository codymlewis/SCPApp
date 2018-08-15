/**
 * ChatClient.java - SENG3400A1
 * A socket based half duplex chat client
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
public class ChatClient {
    private Socket socket;
    public static void main(String args[]) {
        ChatClient cc = new ChatClient();
        cc.run(args);
        System.exit(0);
    }
    public void run(String args[]) {
        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        System.out.println(String.format("Connecting to %s:%d", hostName, port));
        connectToServer(hostName, port);
        System.out.println("Connected to server");
    }
    private boolean connectToServer(String hostName, int port) {
        try {
            socket = new Socket(hostName, port);
            return true;
        } catch(UnknownHostException uhe) {
            return false;
        } catch(IOException ioe) {
            return false;
        }
    }
}