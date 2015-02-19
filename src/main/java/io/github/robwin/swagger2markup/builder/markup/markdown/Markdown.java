package io.github.robwin.swagger2markup.builder.markup.markdown;

import io.github.robwin.swagger2markup.builder.markup.Markup;

/**
 * @author Robert Winkler
 */
public enum Markdown implements Markup {
    HARDBREAKS(""),
    TABLE_COLUMN("|"),
    TABLE_ROW("-"),
    LISTING("```"),
    DOCUMENT_TITLE("# "),
    SECTION_TITLE_LEVEL1("## "),
    SECTION_TITLE_LEVEL2("### "),
    SECTION_TITLE_LEVEL3("### "),
    BOLD("**"),
    ITALIC("*"),
    LIST_ENTRY("* ");

    private final String markup;

    /**
     * @param markup AsciiDoc markup
     */
    private Markdown(final String markup) {
        this.markup = markup;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return markup;
    }
}
