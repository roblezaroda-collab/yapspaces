package yapspace.exception;

/** Thrown when user input is empty, too long, or otherwise invalid. */
public class ValidationException extends YapException {

    public ValidationException(String message) {
        super(message);
    }
}
