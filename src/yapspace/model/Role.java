package yapspace.model;

/** The two kinds of account in YapSpace (see Target Users in the proposal). */
public enum Role {
    USER("User"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
