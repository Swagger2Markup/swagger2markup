package io.swagger2markup.builder.asciidoc;

import io.swagger2markup.builder.Markup;

/**
 * @author Robert Winkler
 */
public enum AsciiDoc implements Markup {
    LABELED(":: "),
    TABLE("|==="),
    LISTING("----"),
    HARDBREAKS(":hardbreaks:"),
    DOCUMENT_TITLE("= "),
    SECTION_TITLE_LEVEL1("== "),
    SECTION_TITLE_LEVEL2("=== "),
    SECTION_TITLE_LEVEL3("==== "),
    BOLD("*"),
    ITALIC("*"),
    LIST_ENTRY("* ");

    private final String markup;

    /**
     * @param markup AsciiDoc markup
     */
    private AsciiDoc(final String markup) {
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
