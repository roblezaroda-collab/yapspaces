package yapspace.exception;

/**
 * Base class for every error that YapSpace raises on purpose.
 * Extending RuntimeException keeps method signatures clean while still letting
 * the web layer catch each kind of problem with try-catch (see Router).
 */
public class YapException extends RuntimeException {

    public YapException(String message) {
        super(message);
    }

    public YapException(String message, Throwable cause) {
        super(message, cause);
    }
}
