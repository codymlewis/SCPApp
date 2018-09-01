import java.util.Scanner;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
/**
 * Chat.java
 * Shared functions and variables between ChatServer and ChatClient
 *
 * @author Cody Lewis
 * @since 2018-08-19
 */
public class Chat extends JFrame {
    protected PrintWriter out;
    protected BufferedReader in;
    protected Socket cliSocket;
    protected InetAddress address;
    protected int port;
    protected String username;
    public static final long serialVersionUID = 1L;
    protected JTextArea msgArea;
    protected JTextField msgField;
    protected boolean disconnect;
    protected Thread recvMsg;
    protected boolean isRecieving;
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
    public Chat() {}
    /**
     * Input Constructor
     */
    public Chat(String title) {
        // Some sentinal values
        disconnect = false;
        isRecieving = true;

        // Construct the GUI
        setLayout(new FlowLayout());
        msgArea = new JTextArea(20, 55);
        // Make the text area have scrollback and auto scroll with text updates
        JScrollPane msgAScrollPane = new JScrollPane(msgArea);
        msgAScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        DefaultCaret caret = (DefaultCaret) msgArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        add(msgAScrollPane);
        JLabel lbl = new JLabel("Message: ");
        add(lbl);
        msgField = new JTextField(30);
        add(msgField);
        // Some buttons with actions
        JButton msgBtn = new JButton("Send");
        add(msgBtn);
        msgBtn.addActionListener(new Send());
        JButton exitBtn = new JButton("Disconnect");
        add(exitBtn);
        exitBtn.addActionListener(new Exit());
        setTitle(title);
        setSize(700, 375);
        setVisible(true);
    }
    /**
     * Message sending action
     */
    private class Send implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String message = textToMessage();
            msgArea.append("Waiting for message to send...\n");
            out.println(SCP.message(address.getHostAddress(), port, message));
            msgArea.append("Message Sent: " + message + "\n");
        }
    }
    /**
     * Message recieving thread
     */
    protected class Recieve implements Runnable {
        private String uname;
        /**
         * Input constructor
         */
        public Recieve(String uname) { this.uname = uname; }
        public void run() {
            try {
                while(isRecieving) {
                    String recievedMessage = recieveMessage();
                    if(recievedMessage == "DISCONNECT") {
                        out.println(SCP.acknowledge());
                        msgArea.append("Other user disconnected\n");
                        recvMsg.interrupt();
                        disconnect = true;
                        isRecieving = false;
                    } else if(recievedMessage == "ACKNOWLEDGE") {
                        msgArea.append("Successfully disconnected\n");
                    } else {
                        msgArea.append(String.format("%s: %s\n", uname, recievedMessage));
                    }
                }
            } catch(SCPException SCPe) {
                System.err.println("Error: " + SCPe.getMessage());
            } catch(IOException ioe) {
                System.err.println("Error: " + ioe.getMessage());
            } catch(NullPointerException npe) {
                msgArea.append("\nUnexpected cut-off from other user\n");
                recvMsg.interrupt();
                disconnect = true;
                isRecieving = false;
            }
        }
    }
    /**
     * Loop for sending a recieving messages
     */
    protected void messageLoop() throws SCPException, IOException {
        String uname = username == null ? "Client" : "Server"; // a server has null username
        recvMsg = new Thread(new Recieve(uname), "scp");
        recvMsg.start();
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
     * Disconnection event handler
     */
    private class Exit implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                recvMsg.interrupt();
                isRecieving = false;
                disconnect = true;
                out.println(SCP.disconnect());
                String message = recieveMessage();
                if(message.compareTo("ACKNOWLEDGE") == 0) {
                    msgArea.append("Successfully disconnected\n");
                } else {
                    throw new SCPException("Other user did not acknowledge disconnect");
                }
            } catch(SCPException SCPe) {
                System.err.println(SCPe.getMessage() + "\n");
            } catch(IOException ioe) {
                System.err.println("Error: " + ioe.getMessage());
            }
        }
    }
    /**
     * Take an input from the users and give an out suitable to put into a message
     * @return A String of the input formatted to be embedded in a SCP message
     */
    protected String textToMessage() {
        String message = msgField.getText();
        msgField.setText("");
        return message;
    }
}
