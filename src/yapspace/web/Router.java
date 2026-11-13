package yapspace.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import yapspace.exception.AuthenticationException;
import yapspace.exception.NotFoundException;
import yapspace.exception.PermissionException;
import yapspace.exception.ValidationException;
import yapspace.exception.YapException;
import yapspace.model.User;
import yapspace.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The front door of the website. Every request arrives here; the router
 * 1) works out who is logged in, 2) finds the matching page handler, and
 * 3) turns exceptions into friendly error pages (exception handling).
 */
public class Router implements HttpHandler {

    private static final int MAX_BODY_BYTES = 20_000;

    private record Route(String method, List<String> segments, Handler handler) {
    }

    private final List<Route> routes = new ArrayList<>();
    private final SessionManager sessions;
    private final UserService users;
    private final Views views;

    public Router(SessionManager sessions, UserService users, Views views) {
        this.sessions = sessions;
        this.users = users;
        this.views = views;
    }

    public void get(String pattern, Handler handler) {
        routes.add(new Route("GET", segments(pattern), handler));
    }

    public void post(String pattern, Handler handler) {
        routes.add(new Route("POST", segments(pattern), handler));
    }

    private static List<String> segments(String path) {
        List<String> parts = new ArrayList<>();
        for (String p : path.split("/")) {
            if (!p.isEmpty()) {
                parts.add(p);
            }
        }
        return parts;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        WebResponse response;
        try {
            response = process(exchange);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response = WebResponse.plain(500, "Something went wrong.");
        }
        send(exchange, response);
    }

    private WebResponse process(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Map<String, String> cookies = parseCookies(exchange.getRequestHeaders().get("Cookie"));

        Map<String, String> form = new HashMap<>();
        boolean bodyTooBig = false;
        if (method.equals("POST")) {
            byte[] body = exchange.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
            if (body.length > MAX_BODY_BYTES) {
                bodyTooBig = true;
            } else {
                form = WebRequest.parseUrlEncoded(new String(body, StandardCharsets.UTF_8));
            }
        }

        WebRequest req = new WebRequest(method, path, exchange.getRequestURI().getRawQuery(), form, cookies);
        attachLogin(req);

        if (bodyTooBig) {
            return views.error(req, 413, "That was too much", "What you tried to send is too large.");
        }
        if (!method.equals("GET") && !method.equals("POST")) {
            return views.error(req, 405, "Not allowed", "That kind of request is not supported.");
        }

        List<String> requested = segments(path);
        for (Route route : routes) {
            if (!route.method().equals(method)) {
                continue;
            }
            Map<String, String> params = match(route.segments(), requested);
            if (params != null) {
                req.setPathParams(params);
                return run(route, req);
            }
        }
        return views.error(req, 404, "Page not found", "We could not find the page you were looking for.");
    }

    private WebResponse run(Route route, WebRequest req) {
        try {
            return route.handler().handle(req);
        } catch (AuthenticationException e) {
            return WebResponse.redirect("/login");
        } catch (PermissionException e) {
            return views.error(req, 403, "Not allowed", e.getMessage());
        } catch (NotFoundException e) {
            return views.error(req, 404, "Not found", e.getMessage());
        } catch (ValidationException e) {
            return views.error(req, 400, "Please check that", e.getMessage());
        } catch (YapException e) {
            e.printStackTrace();
            return views.error(req, 500, "Something went wrong", "Sorry, we could not finish that. Please try again.");
        }
    }

    /** Looks at the login cookie and, if it is valid, remembers who the visitor is. */
    private void attachLogin(WebRequest req) {
        Session session = sessions.find(req.cookie(SessionManager.COOKIE_NAME));
        if (session == null) {
            return;
        }
        User user = users.findById(session.getUserId());
        if (user == null) {
            sessions.destroy(session.getToken());
            return;
        }
        req.setLogin(session, user);
    }

    private static Map<String, String> match(List<String> pattern, List<String> actual) {
        if (pattern.size() != actual.size()) {
            return null;
        }
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < pattern.size(); i++) {
            String p = pattern.get(i);
            if (p.startsWith("{") && p.endsWith("}")) {
                params.put(p.substring(1, p.length() - 1), actual.get(i));
            } else if (!p.equals(actual.get(i))) {
                return null;
            }
        }
        return params;
    }

    private static Map<String, String> parseCookies(List<String> headers) {
        Map<String, String> cookies = new HashMap<>();
        if (headers == null) {
            return cookies;
        }
        for (String header : headers) {
            for (String part : header.split(";")) {
                int eq = part.indexOf('=');
                if (eq > 0) {
                    cookies.put(part.substring(0, eq).trim(), part.substring(eq + 1).trim());
                }
            }
        }
        return cookies;
    }

    private static void send(HttpExchange exchange, WebResponse response) throws IOException {
        var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", response.getContentType());
        for (Map.Entry<String, List<String>> h : response.getHeaders().entrySet()) {
            for (String value : h.getValue()) {
                headers.add(h.getKey(), value);
            }
        }
        if (!headers.containsKey("Cache-Control")) {
            headers.set("Cache-Control", "no-store");
        }
        // Browser-side protections: no inline scripts, no framing, no guessing of file types.
        headers.set("Content-Security-Policy", "default-src 'self'; img-src 'self' data:; "
                + "form-action 'self'; frame-ancestors 'none'; base-uri 'none'");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("X-Frame-Options", "DENY");

        byte[] body = response.getBody();
        if (body.length == 0) {
            exchange.sendResponseHeaders(response.getStatus(), -1);
        } else {
            exchange.sendResponseHeaders(response.getStatus(), body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
        exchange.close();
    }
}
