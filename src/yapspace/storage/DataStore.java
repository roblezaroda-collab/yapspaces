package yapspace.storage;

import yapspace.exception.YapException;
import yapspace.model.AdminUser;
import yapspace.model.Category;
import yapspace.model.Comment;
import yapspace.model.Feeling;
import yapspace.model.Post;
import yapspace.model.Reaction;
import yapspace.model.ReactionType;
import yapspace.model.RegularUser;
import yapspace.model.Role;
import yapspace.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DATA HANDLING: keeps every user, post, comment and reaction in an ArrayList
 * and saves them to plain text files (in the "data" folder) so nothing is lost
 * when the program is closed.
 *
 * Services wrap their work in synchronized (store) { ... } so two web requests
 * never change the lists at the same moment.
 */
public class DataStore {

    private final Path dir;
    private final List<User> users = new ArrayList<>();
    private final List<Post> posts = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();
    private final List<Reaction> reactions = new ArrayList<>();

    private int lastUserId;
    private int lastPostId;
    private int lastCommentId;
    private int lastReactionId;

    public DataStore(Path dir) {
        this.dir = dir;
    }

    public List<User> users() {
        return users;
    }

    public List<Post> posts() {
        return posts;
    }

    public List<Comment> comments() {
        return comments;
    }

    public List<Reaction> reactions() {
        return reactions;
    }

    public synchronized int nextUserId() {
        return ++lastUserId;
    }

    public synchronized int nextPostId() {
        return ++lastPostId;
    }

    public synchronized int nextCommentId() {
        return ++lastCommentId;
    }

    public synchronized int nextReactionId() {
        return ++lastReactionId;
    }

    // ------------------------------------------------------------- loading

    /** Reads the data files (if they exist). A broken line is skipped instead of crashing the program. */
    public synchronized void load() {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new YapException("Cannot create the data folder: " + dir, e);
        }
        users.clear();
        posts.clear();
        comments.clear();
        reactions.clear();

        for (List<String> f : readTable("users.tsv")) {
            try {
                users.add(parseUser(f));
            } catch (RuntimeException e) {
                System.err.println("Skipped a bad line in users.tsv: " + e.getMessage());
            }
        }
        for (List<String> f : readTable("posts.tsv")) {
            try {
                posts.add(parsePost(f));
            } catch (RuntimeException e) {
                System.err.println("Skipped a bad line in posts.tsv: " + e.getMessage());
            }
        }
        for (List<String> f : readTable("comments.tsv")) {
            try {
                comments.add(parseComment(f));
            } catch (RuntimeException e) {
                System.err.println("Skipped a bad line in comments.tsv: " + e.getMessage());
            }
        }
        for (List<String> f : readTable("reactions.tsv")) {
            try {
                reactions.add(parseReaction(f));
            } catch (RuntimeException e) {
                System.err.println("Skipped a bad line in reactions.tsv: " + e.getMessage());
            }
        }

        users.forEach(u -> lastUserId = Math.max(lastUserId, u.getId()));
        posts.forEach(p -> lastPostId = Math.max(lastPostId, p.getId()));
        comments.forEach(c -> lastCommentId = Math.max(lastCommentId, c.getId()));
        reactions.forEach(r -> lastReactionId = Math.max(lastReactionId, r.getId()));
    }

    private List<List<String>> readTable(String fileName) {
        Path file = dir.resolve(fileName);
        List<List<String>> rows = new ArrayList<>();
        if (!Files.exists(file)) {
            return rows;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    rows.add(Tsv.split(line));
                }
            }
        } catch (IOException e) {
            throw new YapException("Cannot read " + fileName, e);
        }
        return rows;
    }

    private static LocalDateTime timeOrNull(String text) {
        return text.isEmpty() ? null : LocalDateTime.parse(text);
    }

    private static User parseUser(List<String> f) {
        int id = Integer.parseInt(f.get(0));
        Role role = Role.valueOf(f.get(1));
        LocalDateTime created = LocalDateTime.parse(f.get(6));
        return role == Role.ADMIN
                ? new AdminUser(id, f.get(2), f.get(3), f.get(4), f.get(5), created)
                : new RegularUser(id, f.get(2), f.get(3), f.get(4), f.get(5), created);
    }

    private static Post parsePost(List<String> f) {
        return new Post(Integer.parseInt(f.get(0)), f.get(1), f.get(6),
                Category.valueOf(f.get(2)), Feeling.valueOf(f.get(3)),
                LocalDateTime.parse(f.get(4)), timeOrNull(f.get(5)));
    }

    private static Comment parseComment(List<String> f) {
        return new Comment(Integer.parseInt(f.get(0)), Integer.parseInt(f.get(1)), f.get(2), f.get(5),
                LocalDateTime.parse(f.get(3)), timeOrNull(f.get(4)));
    }

    private static Reaction parseReaction(List<String> f) {
        return new Reaction(Integer.parseInt(f.get(0)), Integer.parseInt(f.get(1)), f.get(2),
                ReactionType.valueOf(f.get(3)), LocalDateTime.parse(f.get(4)));
    }

    // -------------------------------------------------------------- saving

    public synchronized void saveUsers() {
        write("users.tsv", users.stream()
                .map(u -> Tsv.join(u.getId(), u.getRole().name(), u.getUsername(), u.getEmail(),
                        u.getSalt(), u.getPasswordHash(), u.getCreatedAt()))
                .collect(Collectors.toList()));
    }

    public synchronized void savePosts() {
        write("posts.tsv", posts.stream()
                .map(p -> Tsv.join(p.getId(), p.getAuthor(), p.getCategory().name(), p.getFeeling().name(),
                        p.getCreatedAt(), p.getEditedAt(), p.getContent()))
                .collect(Collectors.toList()));
    }

    public synchronized void saveComments() {
        write("comments.tsv", comments.stream()
                .map(c -> Tsv.join(c.getId(), c.getPostId(), c.getAuthor(), c.getCreatedAt(),
                        c.getEditedAt(), c.getContent()))
                .collect(Collectors.toList()));
    }

    public synchronized void saveReactions() {
        write("reactions.tsv", reactions.stream()
                .map(r -> Tsv.join(r.getId(), r.getPostId(), r.getUsername(), r.getType().name(), r.getCreatedAt()))
                .collect(Collectors.toList()));
    }

    /** Writes to a temporary file first, then swaps it in, so a crash never leaves half a file. */
    private void write(String fileName, List<String> lines) {
        try {
            Files.createDirectories(dir);
            Path temp = dir.resolve(fileName + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new YapException("Cannot save " + fileName, e);
        }
    }
}
