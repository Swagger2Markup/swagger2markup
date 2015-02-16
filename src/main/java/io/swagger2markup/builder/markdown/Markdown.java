package io.swagger2markup.builder.markdown;

import io.swagger2markup.builder.Markup;

/**
 * Project:   swagger2asciidoc
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
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
