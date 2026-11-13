package yapspace.model;

import java.time.LocalDateTime;

/** A normal member (aged 13 and above). Can only remove their own content. */
public class RegularUser extends User {

    public RegularUser(int id, String username, String email, String salt, String passwordHash,
                       LocalDateTime createdAt) {
        super(id, username, email, salt, passwordHash, createdAt);
    }

    @Override
    public Role getRole() {
        return Role.USER;
    }

    @Override
    public boolean canModerate() {
        return false;
    }

    @Override
    public boolean canDelete(Post post) {
        return post.isOwnedBy(getUsername());
    }

    @Override
    public boolean canDelete(Comment comment) {
        return comment.getAuthor().equalsIgnoreCase(getUsername());
    }
}
