package lancs.dividend.oclBenchMapper.message.cmd;

public class IllegalCommandMessageException extends Exception {

	private static final long serialVersionUID = -2305138864511409111L;
	
    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public IllegalCommandMessageException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public IllegalCommandMessageException(String message) {
        super(message);
    }
}
