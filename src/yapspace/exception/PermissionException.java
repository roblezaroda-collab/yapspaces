package yapspace.exception;

/** Thrown when a logged-in user tries to do something they are not allowed to do. */
public class PermissionException extends YapException {

    public PermissionException(String message) {
        super(message);
    }
}
