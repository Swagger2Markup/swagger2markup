package io.github.robwin.swagger2markup.utils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.markup.builder.asciidoc.AsciiDoc;
import io.github.robwin.markup.builder.asciidoc.AsciiDocBuilder;
import io.github.robwin.markup.builder.markdown.Markdown;
import io.github.robwin.markup.builder.markdown.MarkdownBuilder;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

/*
 * FIXME : this code should go to markup-document-builder project
 */
public class MarkupDocBuilderUtils {

    public static String normalizeAsciiDocAnchor(String anchor) {
        return anchor.replaceAll("[^0-9a-zA-Z]", "_");
    }

    public static void anchor(String text, MarkupDocBuilder docBuilder) {
        if (docBuilder instanceof AsciiDocBuilder) {
            docBuilder.textLine("[[" + normalizeAsciiDocAnchor(text) + "]]");
        }
    }

    public static void crossReference(String text, String anchor, MarkupDocBuilder docBuilder) {
        if (docBuilder instanceof AsciiDocBuilder)
            docBuilder.textLine(crossReference(text, anchor, MarkupLanguage.ASCIIDOC));
        else if (docBuilder instanceof MarkdownBuilder)
            docBuilder.textLine(crossReference(text, anchor, MarkupLanguage.MARKDOWN));
    }

    public static String crossReference(String text, String anchor, MarkupLanguage language) {
        if (language == MarkupLanguage.ASCIIDOC) {
            String normalizedAnchor = normalizeAsciiDocAnchor(anchor);
            if (text == null && !anchor.equals(normalizedAnchor))
                text = anchor;
            if (text == null)
                return AsciiDoc.CROSS_REFERENCE_START + normalizedAnchor + AsciiDoc.CROSS_REFERENCE_END;
            else
                return AsciiDoc.CROSS_REFERENCE_START + normalizedAnchor + "," + text + AsciiDoc.CROSS_REFERENCE_END;
        } else {
            if (text == null)
                return anchor;
            else
                return text;
        }
    }

    public static void sectionTitleLevel(int level, String title, String anchor, MarkupDocBuilder docBuilder) {
        if (anchor != null)
            MarkupDocBuilderUtils.anchor(anchor, docBuilder);

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
                    MarkupDocBuilderUtils.anchor(title, docBuilder);
                docBuilder.boldTextLine(title);
                break;
            case 6:
                if (anchor == null)
                    MarkupDocBuilderUtils.anchor(title, docBuilder);
                docBuilder.textLine(title);
                break;
            default:
                throw new RuntimeException("Illegal section level : " + level);
        }
    }

    public static void tableWithHeaderRow(List<Integer> columnWidthRatios, List<List<String>> cells, MarkupDocBuilder docBuilder) {
        if (docBuilder instanceof AsciiDocBuilder) {
            docBuilder.textLine("[options=\"header\",cols=\"" + join(columnWidthRatios, ",") + "\"]");
            docBuilder.textLine(AsciiDoc.TABLE.toString());

            for (List<String> cols : cells) {
                String row = AsciiDoc.TABLE_COLUMN_DELIMITER + join(Collections2.transform(cols, new Function<String, String>() {
                    public String apply(final String col) {
                        return col.replace(AsciiDoc.TABLE_COLUMN_DELIMITER.toString(), "{vbar}");
                    }
                }), AsciiDoc.TABLE_COLUMN_DELIMITER.toString());
                docBuilder.textLine(row);
            }
            docBuilder.textLine(AsciiDoc.TABLE.toString());
        } else if (docBuilder instanceof MarkdownBuilder) {
            List<String> rows = Lists.newArrayList(Collections2.transform(cells, new Function<List<String>, String>() {
                public String apply(List<String> cols) {
                    return join(Collections2.transform(cols, new Function<String, String>() {
                        public String apply(final String col) {
                            return col.replace(Markdown.TABLE_COLUMN_DELIMITER.toString(), "&#124;");
                        }
                    }), Markdown.TABLE_COLUMN_DELIMITER.toString());
                }
            }));

            docBuilder.tableWithHeaderRow(rows);
        }
    }
}
