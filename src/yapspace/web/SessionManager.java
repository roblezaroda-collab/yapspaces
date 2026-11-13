package yapspace.web;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Creates, finds and removes login sessions (kept in memory; restarting the program logs everybody out). */
public class SessionManager {

    public static final String COOKIE_NAME = "YAPSESSION";
    private static final long IDLE_LIMIT_MILLIS = 2L * 60 * 60 * 1000; // 2 hours

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session create(int userId) {
        long now = System.currentTimeMillis();
        sessions.values().removeIf(s -> now - s.getLastSeen() > IDLE_LIMIT_MILLIS);
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        Session session = new Session(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes), userId);
        sessions.put(session.getToken(), session);
        return session;
    }

    public Session find(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (System.currentTimeMillis() - session.getLastSeen() > IDLE_LIMIT_MILLIS) {
            sessions.remove(token);
            return null;
        }
        session.touch();
        return session;
    }

    public void destroy(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    /** Value for the Set-Cookie header that logs the browser in. HttpOnly keeps scripts away from it. */
    public String loginCookie(Session session) {
        return COOKIE_NAME + "=" + session.getToken() + "; Path=/; HttpOnly; SameSite=Lax";
    }

    /** Value for the Set-Cookie header that logs the browser out. */
    public String logoutCookie() {
        return COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
    }
}
