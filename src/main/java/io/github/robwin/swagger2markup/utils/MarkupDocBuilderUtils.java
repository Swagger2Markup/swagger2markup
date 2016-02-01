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

    public static void anchor(String text, MarkupLanguage language, MarkupDocBuilder docBuilder) {
        switch (language) {
            case ASCIIDOC:
                docBuilder.textLine("[[" + normalizeAnchor(text) + "]]");
                break;
            default:
        }
    }

    public static void crossReference(String text, String anchor, MarkupLanguage language, MarkupDocBuilder docBuilder) {
        docBuilder.textLine(crossReference(text, anchor, language));
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

    public static void sectionTitleLevel(int level, String title, String anchor, MarkupLanguage language, MarkupDocBuilder docBuilder) {
        if (anchor != null)
            MarkupDocBuilderUtils.anchor(anchor, language, docBuilder);

        switch (level) {
            case 1:
                docBuilder.sectionTitleLevel1(title);
                break;
            case 2:
                docBuilder.sectionTitleLevel2(title);
                break;
            case 3:
                docBuilder.sectionTitleLevel3(title);
                break;
            case 4:
                docBuilder.sectionTitleLevel4(title);
                break;
            case 5:
                if (anchor == null)
                    MarkupDocBuilderUtils.anchor(title, language, docBuilder);
                docBuilder.boldTextLine(title);
                break;
            default:
                throw new RuntimeException("Illegal section level : " + level);
        }

    }
}
