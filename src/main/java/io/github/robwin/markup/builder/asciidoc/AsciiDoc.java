package io.github.robwin.markup.builder.asciidoc;


import io.github.robwin.markup.builder.Markup;

/**
 * @author Robert Winkler
 */
public enum AsciiDoc implements Markup {
    LABELED(":: "),
    TABLE("|==="),
    TABLE_COLUMN_DELIMITER("|"),
    LISTING("----"),
    HARDBREAKS(":hardbreaks:"),
    DOCUMENT_TITLE("= "),
    SECTION_TITLE_LEVEL1("== "),
    SECTION_TITLE_LEVEL2("=== "),
    SECTION_TITLE_LEVEL3("==== "),
    BOLD("*"),
    ITALIC("_"),
    LIST_ENTRY("* "),
    CROSS_REFERENCE_START("<<"),
    CROSS_REFERENCE_END(">>");

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
