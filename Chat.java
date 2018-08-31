import java.util.Scanner;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Chat.java
 * Shared functions and variables between ChatServer and ChatClient
 *
 * @author Cody Lewis
 * @since 2018-08-19
 */
public class Chat extends Frame {
    protected PrintWriter out;
    protected BufferedReader in;
    protected Socket cliSocket;
    protected InetAddress address;
    protected int port;
    protected String username;
    public static final long serialVersionUID = 1L;
    protected TextArea msgArea;
    protected TextField msgField;
    protected boolean disconnect;
    protected Thread recvMsg;
    private boolean isRecieving;
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
        disconnect = false;
        isRecieving = true;
        setLayout(new FlowLayout());
        msgArea = new TextArea(); // use append(String str) to add messages
        add(msgArea);
        Label lbl = new Label("Message: ");
        add(lbl);
        msgField = new TextField(30);
        add(msgField);
        Button msgBtn = new Button("Send");
        add(msgBtn);
        msgBtn.addActionListener(new Send());
        Button exitBtn = new Button("Exit");
        add(exitBtn);
        exitBtn.addActionListener(new Exit());
        setTitle(title);
        setSize(500, 250);
        setVisible(true);
    }
    private class Send implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                String message = textToMessage();
                msgArea.append("Waiting for message to send...\n");
                out.println(SCP.message(address.getHostAddress(), port, message));
                msgArea.append("Message Sent: " + message + "\n");
            } catch(Exception e) {}
        }
    }
    protected class Recieve implements Runnable {
        private String uname;
        public Recieve(String uname) { this.uname = uname; }
        public void run() {
            try {
                while(isRecieving) {
                    String recievedMessage = recieveMessage();
                    if(recievedMessage == "DISCONNECT") {
                        out.println(SCP.acknowledge());
                        msgArea.append("Other user disconnected\n");
                        disconnect = true;
                    } else {
                        msgArea.append(String.format("%s: %s\n", uname, recievedMessage));
                    }
                }
            } catch(Exception e) {}
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
    private class Exit implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                // out.println(SCP.disconnect());
                disconnect(); // server side disconnects don't quite work
                isRecieving = false;
                recvMsg.interrupt();
                disconnect = true;
                if(recieveMessage().compareTo("ACKNOWLEDGE") == 0) { // this seems to be the problem
                    msgArea.append("Successfully disconnected\n");
                } else {
                    throw new SCPException("Client did not acknowledge disconnect");
                }
            } catch(Exception e) { }
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
    /**
     * Send a SCP disconnect to the other user
     */
    protected void disconnect() {
        out.println(SCP.disconnect());
    }
}
