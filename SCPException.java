/**
 * SCPException.java - Seng3400A1
 * Exception for SCP related errors
 *
 * @author Cody Lewis
 * @since 2018-08-10
 */
public class SCPException extends Exception {
    static final long serialVersionUID = 1L;
    public SCPException() {}
    public SCPException(String message) {
        super(message);
    }
}