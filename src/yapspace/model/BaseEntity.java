package yapspace.model;

import java.time.LocalDateTime;

/**
 * ABSTRACTION + INHERITANCE: the general parent of every stored object
 * (User, Post, Comment, Reaction). It holds what they all share - an id and
 * the time they were created - so the subclasses do not repeat it.
 */
public abstract class BaseEntity {

    private final int id;
    private final LocalDateTime createdAt;

    protected BaseEntity(int id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Every entity must describe itself in one line (each subclass answers differently). */
    public abstract String getSummary();

    @Override
    public String toString() {
        return getSummary();
    }
}
