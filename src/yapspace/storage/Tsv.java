package yapspace.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiny helper for the data files. Every record is one line; fields are separated
 * by a TAB. Tabs, new lines and backslashes inside a field are escaped so a post
 * that contains several lines still fits on one line of the file.
 */
final class Tsv {

    private Tsv() {
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    static String unescape(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 't' -> sb.append('\t');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Builds one line of the file from the given fields. */
    static String join(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append('\t');
            }
            sb.append(escape(fields[i] == null ? "" : fields[i].toString()));
        }
        return sb.toString();
    }

    /** Splits one line of the file back into its (unescaped) fields. */
    static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        for (String raw : line.split("\t", -1)) {
            fields.add(unescape(raw));
        }
        return fields;
    }
}
