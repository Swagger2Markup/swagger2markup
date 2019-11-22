package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;

public enum Style {
    ASCIIDOC("a"), EMPHASIS("e"), HEADER("h"), LITERAL("l"), MONOSPACED("m"), NONE("d"), STRONG("s"), VERSE("v");

    String shortHand;

    Style(String h) {
        this.shortHand = h;
    }

    public static Style fromString(String text) {
        if(StringUtils.isNotBlank(text)) {
            for (Style s : Style.values()) {
                if (s.shortHand.equalsIgnoreCase(text)) {
                    return s;
                }
            }
        }
        return null;
    }

    public static Style fromName(String text) {
        if(StringUtils.isNotBlank(text)) {
            return valueOf(text.toUpperCase());
        }
        return null;
    }

    public String getShortHand() {
        return shortHand;
    }
}
