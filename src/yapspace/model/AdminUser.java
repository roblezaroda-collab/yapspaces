package yapspace.model;

import java.time.LocalDateTime;

/** The administrator. Same data as a User, but with moderation powers (polymorphism). */
public class AdminUser extends User {

    public AdminUser(int id, String username, String email, String salt, String passwordHash,
                     LocalDateTime createdAt) {
        super(id, username, email, salt, passwordHash, createdAt);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public boolean canModerate() {
        return true;
    }

    @Override
    public boolean canDelete(Post post) {
        return true; // can remove any post that breaks the community rules
    }

    @Override
    public boolean canDelete(Comment comment) {
        return true;
    }
}
