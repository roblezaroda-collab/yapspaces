package yapspace.web;

/** One logged-in browser. Holds who it is and a one-time message to show on the next page. */
public final class Session {

    /** A short message shown once at the top of the next page. */
    public record Flash(String message, boolean error) {
    }

    private final String token;
    private final int userId;
    private volatile Flash flash;
    private volatile long lastSeen = System.currentTimeMillis();

    Session(String token, int userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    long getLastSeen() {
        return lastSeen;
    }

    void touch() {
        lastSeen = System.currentTimeMillis();
    }

    public void setFlash(String message) {
        this.flash = new Flash(message, false);
    }

    public void setErrorFlash(String message) {
        this.flash = new Flash(message, true);
    }

    /** Returns the message once, then forgets it. */
    public Flash takeFlash() {
        Flash message = flash;
        flash = null;
        return message;
    }
}
