package io.github.konstantinosioan.typesetter.parsing;

/**
 * Signals that the input could not be parsed, and on which line.
 */
public final class ParseException extends Exception {
    private static final long serialVersionUID = 1L;

    /** The 1-based number of the line that failed to parse. */
    private final int lineNumber;

    /**
     * Creates an exception for a problem on the given line.
     *
     * @param lineNumber the 1-based number of the offending line
     * @param message what is wrong with the line
     * @param cause the underlying exception
     */
    public ParseException(int lineNumber, String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the line on which parsing failed.
     *
     * @return the 1-based line number
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
