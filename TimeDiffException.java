/**
 * TimeDiffException.java - Seng3400A1
 * Exception for the Time Diff error from SCP parsing
 *
 * @author Cody Lewis
 * @since 2018-08-19
 */
public class TimeDiffException extends Exception {
    static final long serialVersionUID = 1L;
    private int timeDiff;
    /**
     * Default constructor
     */
    public TimeDiffException() { timeDiff = 0; }
    /**
     * Input constructor
     * @param timeDiff the difference in the times
     */
    public TimeDiffException(int timeDiff) { this.timeDiff = timeDiff; }
    /**
     * Query for the time difference
     * @return the Difference in times
     */
    public int getTimeDiff() { return timeDiff; }
}