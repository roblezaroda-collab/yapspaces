package yapspace;

import yapspace.storage.DataStore;
import yapspace.web.WebServer;

import java.io.IOException;
import java.net.BindException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Starts YapSpace. Run this class, then open http://localhost:8080 in your browser.
 *
 * Optional program arguments:
 *   --port=8081        use another port (default 8080)
 *   --data=somefolder  where to save the data (default: the "data" folder in the project)
 */
public class Main {

    public static void main(String[] args) {
        String renderPort = System.getenv("PORT");
int port = 8080;

if (renderPort != null && !renderPort.isBlank()) {
    port = Integer.parseInt(renderPort);
}
        Path dataDir = Paths.get("data");
        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                try {
                    port = Integer.parseInt(arg.substring("--port=".length()));
                } catch (NumberFormatException e) {
                    System.err.println("The port must be a number, for example --port=8081");
                    return;
                }
            } else if (arg.startsWith("--data=")) {
                dataDir = Paths.get(arg.substring("--data=".length()));
            }
        }

        DataStore store = new DataStore(dataDir);
        store.load();

        try {
            WebServer server = new WebServer(port, store);
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            System.out.println();
            System.out.println("  YapSpace is running at  http://localhost:" + port);
            System.out.println("  Your data is saved in   " + dataDir.toAbsolutePath());
            System.out.println("  Press Ctrl+C (or the stop button) to stop.");
            System.out.println();
        } catch (BindException e) {
            System.err.println("Port " + port + " is already in use. Close the other program or run with --port=" + (port + 1));
        } catch (IOException e) {
            System.err.println("Could not start the server: " + e.getMessage());
        }
    }
}
