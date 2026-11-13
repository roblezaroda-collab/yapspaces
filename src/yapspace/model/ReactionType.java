package yapspace.model;

import yapspace.exception.ValidationException;

/** The three ways to react to a post (Support, Relate, Encourage), matching the proposal mockup. */
public enum ReactionType {
    SUPPORT("Support", "&#10084;&#65039;"),
    RELATE("Relate", "&#128077;"),
    ENCOURAGE("Encourage", "&#11088;");

    private final String label;
    private final String icon;

    ReactionType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public static ReactionType parse(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Please choose a reaction.");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("That reaction does not exist.");
        }
    }
}
