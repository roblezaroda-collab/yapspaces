package yapspace.web;

import com.sun.net.httpserver.HttpServer;
import yapspace.service.PostService;
import yapspace.service.UserService;
import yapspace.storage.DataStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Puts all the parts together (services, controllers, router) and runs Java's built-in web server. */
public class WebServer {

    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public WebServer(int port, DataStore store) throws IOException {
        UserService users = new UserService(store);
        PostService posts = new PostService(store);
        SessionManager sessions = new SessionManager();
        Views views = new Views(posts);
        Router router = new Router(sessions, users, views);

        new AuthController(users, sessions, views).addRoutes(router);
        new PostController(posts, views).addRoutes(router);

        router.get("/static/style.css", req -> WebResponse.asset("text/css; charset=utf-8", Assets.CSS));
        router.get("/static/app.js", req -> WebResponse.asset("text/javascript; charset=utf-8", Assets.JS));
        router.get("/favicon.ico", req -> WebResponse.plain(204, ""));

        // "localhost" only: the site can be opened from this computer, not from the network.
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", router);
        server.setExecutor(executor);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
        executor.shutdownNow();
    }
}
