package yapspace.web;

import yapspace.exception.AuthenticationException;
import yapspace.exception.ValidationException;
import yapspace.model.User;
import yapspace.service.UserService;

/** Landing page, registration, login and logout. */
public class AuthController {

    private final UserService users;
    private final SessionManager sessions;
    private final Views views;

    public AuthController(UserService users, SessionManager sessions, Views views) {
        this.users = users;
        this.sessions = sessions;
        this.views = views;
    }

    public void addRoutes(Router router) {
        router.get("/", req -> WebResponse.html(views.landing(req)));
        router.get("/register", this::registerForm);
        router.post("/register", this::registerSubmit);
        router.get("/login", this::loginForm);
        router.post("/login", this::loginSubmit);
        router.post("/logout", this::logout);
    }

    private WebResponse registerForm(WebRequest req) {
        if (req.isLoggedIn()) {
            return WebResponse.redirect("/feed");
        }
        return WebResponse.html(views.register(req, null, "", "", false));
    }

    private WebResponse registerSubmit(WebRequest req) {
        if (req.isLoggedIn()) {
            return WebResponse.redirect("/feed");
        }
        String username = req.param("username");
        String email = req.param("email");
        boolean ageConfirmed = req.hasParam("age");
        try {
            User user = users.register(username, email, req.param("password"), req.param("confirm"), ageConfirmed);
            return startSession(user, "Welcome to YapSpace, " + user.getUsername() + "!");
        } catch (ValidationException e) {
            return WebResponse.html(400, views.register(req, e.getMessage(), username, email, ageConfirmed));
        }
    }

    private WebResponse loginForm(WebRequest req) {
        if (req.isLoggedIn()) {
            return WebResponse.redirect("/feed");
        }
        return WebResponse.html(views.login(req, null, ""));
    }

    private WebResponse loginSubmit(WebRequest req) {
        if (req.isLoggedIn()) {
            return WebResponse.redirect("/feed");
        }
        String username = req.param("username");
        try {
            User user = users.login(username, req.param("password"));
            return startSession(user, "Welcome back, " + user.getUsername() + "!");
        } catch (AuthenticationException e) {
            return WebResponse.html(401, views.login(req, e.getMessage(), username));
        } catch (ValidationException e) {
            return WebResponse.html(400, views.login(req, e.getMessage(), username));
        }
    }

    private WebResponse logout(WebRequest req) {
        if (req.getSession() != null) {
            sessions.destroy(req.getSession().getToken());
        }
        return WebResponse.redirect("/").header("Set-Cookie", sessions.logoutCookie());
    }

    /** A brand-new session is created at every login (never reuse an old one). */
    private WebResponse startSession(User user, String welcome) {
        Session session = sessions.create(user.getId());
        session.setFlash(welcome);
        return WebResponse.redirect("/feed").header("Set-Cookie", sessions.loginCookie(session));
    }
}
