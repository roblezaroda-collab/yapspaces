package yapspace.exception;

/** Thrown when login details are wrong or a page needs a logged-in user. */
public class AuthenticationException extends YapException {

    public AuthenticationException(String message) {
        super(message);
    }
}
