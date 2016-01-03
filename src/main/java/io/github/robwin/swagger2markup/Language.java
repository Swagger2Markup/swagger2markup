package io.github.robwin.swagger2markup;

/**
 * @author Maksim Myshkin
 */
public enum Language {
    EN("en"),
    RU("ru");

    private final String lang;

    Language(final String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return lang;
    }
}
