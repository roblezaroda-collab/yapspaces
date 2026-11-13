package yapspace.web;

/** One page of the website: takes a request and returns a response. */
@FunctionalInterface
public interface Handler {
    WebResponse handle(WebRequest request);
}
