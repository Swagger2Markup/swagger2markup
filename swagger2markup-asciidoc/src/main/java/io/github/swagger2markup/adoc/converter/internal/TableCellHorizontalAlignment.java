package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;

public enum TableCellHorizontalAlignment {
    LEFT("<"), CENTER("^"), RIGHT(">");

    String delimiter;

    TableCellHorizontalAlignment(String s) {
        this.delimiter = s;
    }

    public static TableCellHorizontalAlignment fromString(String text) {
        if(StringUtils.isNotBlank(text)) {
            for (TableCellHorizontalAlignment a : TableCellHorizontalAlignment.values()) {
                if (a.delimiter.equalsIgnoreCase(text)) {
                    return a;
                }
            }
        }
        return null;
    }

    public static TableCellHorizontalAlignment fromName(String text) {
        if(StringUtils.isNotBlank(text)) {
            return valueOf(text.toUpperCase());
        }
        return null;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
