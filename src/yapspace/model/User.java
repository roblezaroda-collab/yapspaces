package yapspace.model;

import java.time.LocalDateTime;

/**
 * Represents a YapSpace account.
 *
 * ENCAPSULATION: fields are private and only reachable through getters.
 * ABSTRACTION: the class is abstract; RegularUser and AdminUser decide what
 * each kind of account is allowed to do.
 * POLYMORPHISM: canDelete(...) and canModerate() behave differently
 * depending on the real type of the object.
 */
public abstract class User extends BaseEntity {

    private final String username;
    private String email;
    private final String salt;
    private final String passwordHash;

    protected User(int id, String username, String email, String salt, String passwordHash,
                   LocalDateTime createdAt) {
        super(id, createdAt);
        this.username = username;
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /** Which kind of account this is. */
    public abstract Role getRole();

    /** Can this account review reported posts and see site statistics? */
    public abstract boolean canModerate();

    /** Can this account delete the given post? */
    public abstract boolean canDelete(Post post);

    /** Can this account delete the given comment? */
    public abstract boolean canDelete(Comment comment);

    /** Only the author may edit a post, even an administrator. */
    public boolean canEdit(Post post) {
        return post.isOwnedBy(username);
    }

    /** Only the author may edit a comment. */
    public boolean canEdit(Comment comment) {
        return comment.getAuthor().equalsIgnoreCase(username);
    }

    @Override
    public String getSummary() {
        return username + " (" + getRole().getLabel() + ")";
    }
}
