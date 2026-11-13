package yapspace.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Small helpers for building safe HTML. Everything a user typed goes through esc() first. */
public final class Html {

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private Html() {
    }

    /** Escapes text so it can never be read by the browser as HTML (prevents XSS). */
    public static String esc(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Escapes text and keeps the user's line breaks. */
    public static String multiline(String text) {
        return esc(text).replace("\n", "<br>");
    }

    public static String url(String text) {
        return URLEncoder.encode(text == null ? "" : text, StandardCharsets.UTF_8);
    }

    /** "just now", "5 min ago", "3 h ago", "2 d ago", or the date for older items. */
    public static String timeAgo(LocalDateTime time) {
        Duration d = Duration.between(time, LocalDateTime.now());
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + " h ago";
        }
        long days = d.toDays();
        if (days < 7) {
            return days + " d ago";
        }
        return time.format(SHORT);
    }

    public static String fullDate(LocalDateTime time) {
        return time.format(FULL);
    }

    /** A &lt;time&gt; tag with the relative time as text and the exact time as a tooltip. */
    public static String timeTag(LocalDateTime time) {
        return "<time datetime=\"" + time + "\" title=\"" + esc(fullDate(time)) + "\">" + esc(timeAgo(time)) + "</time>";
    }

    /** Only allows redirects to pages on this site (stops "open redirect" tricks). */
    public static String safeNext(String next, String fallback) {
        if (next == null || next.isEmpty() || next.length() > 300) {
            return fallback;
        }
        if (!next.startsWith("/") || next.startsWith("//") || next.contains("\\")) {
            return fallback;
        }
        for (int i = 0; i < next.length(); i++) {
            if (next.charAt(i) < 0x20 || next.charAt(i) == 0x7f) {
                return fallback;
            }
        }
        return next;
    }

    /** Colour slot (0-7) for a user's avatar, always the same for the same name. */
    public static int avatarSlot(String username) {
        return Math.floorMod(username.toLowerCase(Locale.ROOT).hashCode(), 8);
    }

    public static String initial(String username) {
        return username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
