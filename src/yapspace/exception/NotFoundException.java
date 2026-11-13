package yapspace.exception;

/** Thrown when a post, comment or page does not exist. */
public class NotFoundException extends YapException {

    public NotFoundException(String message) {
        super(message);
    }
}
