package io.github.robwin.swagger2markup.utils;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupLanguage;

/*
 * FIXME : this code should go to markup-document-builder project
 */
public class MarkupDocBuilderUtils {

    public static String normalizeAnchor(String anchor) {
        return anchor.replaceAll("[^0-9a-zA-Z]", "_");
    }

    public static String anchor(String text, MarkupLanguage language) {
        switch (language) {
            case ASCIIDOC:
                return "[[" + normalizeAnchor(text) + "]]";
            default:
                return "";
        }
    }

    public static String crossReference(String text, String anchor, MarkupLanguage language) {
        switch (language) {
            case ASCIIDOC:
                String normalizedAnchor = normalizeAnchor(anchor);
                if (text == null && !anchor.equals(normalizedAnchor))
                    text = anchor;
                if (text == null)
                    return "<<" + normalizedAnchor + ">>";
                else
                    return "<<" + normalizedAnchor + "," + text + ">>";
            default:
                if (text == null)
                    return anchor;
                else
                    return text;
        }
    }
}
