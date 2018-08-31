/**
 * SCPException.java - Seng3400A1
 * Exception for SCP related errors
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class SCPException extends Exception {
    static final long serialVersionUID = 1L;
    /**
     * Default constructor
     */
    public SCPException() {}
    /**
     * Input constructor
     * @param message the exception's message
     */
    public SCPException(String message) { super(message); }
    /**
     * Sentence input constructor
     * @param expected the String that was Expected
     * @param received the String that was recieved
     */
    public SCPException(String expected, String received) {
        super("Expected " + expected + " but got " + received);
    }
}
