package yapspace.service;

import yapspace.exception.ValidationException;

import java.util.regex.Pattern;

/** Checks everything a user types. Each method returns the cleaned value or throws ValidationException. */
public final class Validator {

    public static final int POST_MAX = 1000;
    public static final int COMMENT_MAX = 300;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 72;

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s,]+@[^@\\s,]+\\.[^@\\s,]+$");

    private Validator() {
    }

    public static String username(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            throw new ValidationException("Please enter a username.");
        }
        if (!USERNAME.matcher(value).matches()) {
            throw new ValidationException("Username must be 3-20 characters: letters, numbers and underscores only.");
        }
        return value;
    }

    public static String email(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            throw new ValidationException("Please enter your email address.");
        }
        if (value.length() > 100 || !EMAIL.matcher(value).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        return value;
    }

    public static String password(String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new ValidationException("Please enter a password.");
        }
        if (raw.length() < PASSWORD_MIN) {
            throw new ValidationException("Password must be at least " + PASSWORD_MIN + " characters.");
        }
        if (raw.length() > PASSWORD_MAX) {
            throw new ValidationException("Password must be at most " + PASSWORD_MAX + " characters.");
        }
        return raw;
    }

    public static String postContent(String raw) {
        String value = clean(raw);
        if (value.isEmpty()) {
            throw new ValidationException("Your post cannot be empty. Write something first!");
        }
        if (value.length() > POST_MAX) {
            throw new ValidationException("Your post is too long (maximum " + POST_MAX + " characters).");
        }
        return value;
    }

    public static String commentContent(String raw) {
        String value = clean(raw);
        if (value.isEmpty()) {
            throw new ValidationException("Your comment cannot be empty.");
        }
        if (value.length() > COMMENT_MAX) {
            throw new ValidationException("Your comment is too long (maximum " + COMMENT_MAX + " characters).");
        }
        return value;
    }

    /** Unifies line breaks, removes hidden control characters and trims the ends. */
    private static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.replace("\r\n", "\n").replace('\r', '\n');
        value = value.replace('\t', ' ');
        value = value.replaceAll("[\\p{Cntrl}&&[^\\n]]", "");
        return value.trim();
    }
}
