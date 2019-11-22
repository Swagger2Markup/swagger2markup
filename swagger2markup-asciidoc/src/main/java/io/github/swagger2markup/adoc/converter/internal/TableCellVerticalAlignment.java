package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;

public enum TableCellVerticalAlignment {
    TOP(".<"), MIDDLE(".^"), BOTTOM(".>");

    String delimiter;

    TableCellVerticalAlignment(String s) {
        this.delimiter = s;
    }

    public static TableCellVerticalAlignment fromString(String text) {
        if(StringUtils.isNotBlank(text)) {
            for (TableCellVerticalAlignment a : TableCellVerticalAlignment.values()) {
                if (a.delimiter.equalsIgnoreCase(text)) {
                    return a;
                }
            }
        }
        return null;
    }

    public static TableCellVerticalAlignment fromName(String text) {
        if(StringUtils.isNotBlank(text)) {
            return valueOf(text.toUpperCase());
        }
        return null;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
