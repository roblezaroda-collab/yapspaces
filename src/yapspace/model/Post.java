package yapspace.model;

import java.time.LocalDateTime;

/** A text post shared in the community feed, with a category and a feeling. */
public class Post extends BaseEntity {

    private final String author;
    private String content;
    private Category category;
    private Feeling feeling;
    private LocalDateTime editedAt;

    /** Used when loading saved data. */
    public Post(int id, String author, String content, Category category, Feeling feeling,
                LocalDateTime createdAt, LocalDateTime editedAt) {
        super(id, createdAt);
        this.author = author;
        this.content = content;
        this.category = category;
        this.feeling = feeling;
        this.editedAt = editedAt;
    }

    /** Used when a user publishes a brand-new post. */
    public Post(int id, String author, String content, Category category, Feeling feeling) {
        this(id, author, content, category, feeling, LocalDateTime.now(), null);
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Category getCategory() {
        return category;
    }

    public Feeling getFeeling() {
        return feeling;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public boolean isOwnedBy(String username) {
        return author.equalsIgnoreCase(username);
    }

    /** Applies an edit and remembers when it happened. */
    public void update(String content, Category category, Feeling feeling) {
        this.content = content;
        this.category = category;
        this.feeling = feeling;
        this.editedAt = LocalDateTime.now();
    }

    @Override
    public String getSummary() {
        return "Post #" + getId() + " by " + author + " [" + category.getLabel() + ", " + feeling.getLabel() + "]";
    }
}
