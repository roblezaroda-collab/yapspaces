package yapspace.web;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What the server sends back: a status code, headers and a body. */
public final class WebResponse {

    private int status = 200;
    private String contentType = "text/html; charset=utf-8";
    private byte[] body = new byte[0];
    private final Map<String, List<String>> headers = new LinkedHashMap<>();

    private WebResponse() {
    }

    public static WebResponse html(String page) {
        return html(200, page);
    }

    public static WebResponse html(int status, String page) {
        WebResponse r = new WebResponse();
        r.status = status;
        r.body = page.getBytes(StandardCharsets.UTF_8);
        return r;
    }

    public static WebResponse asset(String contentType, String content) {
        WebResponse r = new WebResponse();
        r.contentType = contentType;
        r.body = content.getBytes(StandardCharsets.UTF_8);
        r.header("Cache-Control", "public, max-age=300");
        return r;
    }

    public static WebResponse redirect(String location) {
        WebResponse r = new WebResponse();
        r.status = 303; // "See Other": the browser follows with a GET after a POST
        r.header("Location", location);
        return r;
    }

    public static WebResponse plain(int status, String text) {
        WebResponse r = new WebResponse();
        r.status = status;
        r.contentType = "text/plain; charset=utf-8";
        r.body = text.getBytes(StandardCharsets.UTF_8);
        return r;
    }

    public WebResponse header(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}
