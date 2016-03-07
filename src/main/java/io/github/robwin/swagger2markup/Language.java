package io.github.robwin.swagger2markup;

import java.util.Locale;

/**
 * @author Maksim Myshkin
 */
public enum Language {
    EN(new Locale("en")),
    RU(new Locale("ru")),
    FR(new Locale("fr"));

    private final Locale lang;

    Language(final Locale lang) {
        this.lang = lang;
    }

    public Locale toLocale() {
        return lang;
    }
}
