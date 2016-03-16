/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.markup.builder.internal.markdown;

import io.github.robwin.markup.builder.*;
import io.github.robwin.markup.builder.internal.AbstractMarkupDocBuilder;
import io.github.robwin.markup.builder.internal.Markup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Robert Winkler
 */
public class MarkdownBuilder extends AbstractMarkupDocBuilder {

    private static final Pattern TITLE_PATTERN = Pattern.compile(String.format("^(%s{1,%d})\\s+(.*)$", Markdown.TITLE, MAX_TITLE_LEVEL + 1));

    private static final Map<MarkupBlockStyle, String> BLOCK_STYLE = new HashMap<MarkupBlockStyle, String>() {{
        put(MarkupBlockStyle.EXAMPLE, "");
        put(MarkupBlockStyle.LISTING, Markdown.LISTING.toString());
        put(MarkupBlockStyle.LITERAL, Markdown.LISTING.toString());
        put(MarkupBlockStyle.PASSTHROUGH, "");
        put(MarkupBlockStyle.SIDEBAR, "");
    }};

    public MarkdownBuilder() {
        super();
    }

    public MarkdownBuilder(String newLine) {
        super(newLine);
    }

    @Override
    public MarkupDocBuilder copy(boolean copyBuffer) {
        MarkdownBuilder builder = new MarkdownBuilder(newLine);

        if (copyBuffer)
            builder.documentBuilder = new StringBuilder(this.documentBuilder);

        return builder.withAnchorPrefix(anchorPrefix);
    }

    @Override
    public MarkupDocBuilder documentTitle(String title) {
        documentTitle(Markdown.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title, String anchor) {
        sectionTitleWithAnchorLevel(Markdown.TITLE, level, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder block(String text, final MarkupBlockStyle style, String title, MarkupAdmonition admonition) {
        if (admonition != null)
            documentBuilder.append(StringUtils.capitalize(admonition.name().toLowerCase()));
        if (title != null) {
            if (admonition != null)
                documentBuilder.append(" | ");
            documentBuilder.append(title);
        }
        if (admonition != null || title != null)
            documentBuilder.append(" : ").append(newLine);

        delimitedBlockText(new Markup() {
            public String toString() {
                return BLOCK_STYLE.get(style);
            }
        }, text);
        return this;
    }

    @Override
    public MarkupDocBuilder listing(String text, String language) {
        if (language != null)
            text = language + " :" + newLine + text;
        block(text, MarkupBlockStyle.LISTING);
        return this;
    }

    @Override
    public MarkupDocBuilder boldText(String text) {
        boldText(Markdown.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicText(String text) {
        italicText(Markdown.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list) {
        unorderedList(Markdown.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedListItem(String item) {
        unorderedListItem(Markdown.LIST_ENTRY, item);
        return this;
    }

    private String normalizeAnchor(String anchor) {
        return normalizeAnchor(Markdown.SPACE_ESCAPE, anchor);
    }

    @Override
    public MarkupDocBuilder anchor(String anchor, String text) {
        documentBuilder.append("<a name=\"").append(normalizeAnchor(anchor)).append("\"></a>");
        return this;
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String document, String anchor, String text) {
        if (text == null)
            text = anchor.trim();
        documentBuilder.append("[").append(text).append("]").append("(");
        if (document != null)
            documentBuilder.append(document);
        documentBuilder.append("#").append(anchor).append(")");
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String anchor, String text) {
        return crossReferenceRaw(document, normalizeAnchor(anchor), text);
    }

    private String escapeTableCell(String cell) {
        return cell.replace(Markdown.TABLE_COLUMN_DELIMITER.toString(), "\\" + Markdown.TABLE_COLUMN_DELIMITER.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {
        Validate.notEmpty(columnSpecs);

        newLine();
        Collection<String> headerList = CollectionUtils.collect(columnSpecs, new Transformer<MarkupTableColumn, String>() {
            public String transform(final MarkupTableColumn header) {
                return escapeTableCell(replaceNewLinesWithWhiteSpace(defaultString(header.header)));
            }
        });
        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER).append(join(headerList, Markdown.TABLE_COLUMN_DELIMITER.toString())).append(Markdown.TABLE_COLUMN_DELIMITER).append(newLine);

        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER);
        for (MarkupTableColumn col : columnSpecs) {
            documentBuilder.append(StringUtils.repeat(Markdown.TABLE_ROW.toString(), 3));
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER);
        }
        documentBuilder.append(newLine);

        for (List<String> row : cells) {
            Collection<String> cellList = CollectionUtils.collect(row, new Transformer<String, String>() {
                public String transform(final String cell) {
                    return escapeTableCell(replaceNewLines(cell));
                }
            });
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER).append(join(cellList, Markdown.TABLE_COLUMN_DELIMITER.toString())).append(Markdown.TABLE_COLUMN_DELIMITER).append(newLine);
        }
        newLine();

        return this;
    }

    @Override
    public MarkupDocBuilder newLine(boolean forceLineBreak) {
        newLine(Markdown.LINE_BREAK, forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder importMarkup(Reader markupText, int levelOffset) throws IOException {
        importMarkupStyle1(TITLE_PATTERN, Markdown.TITLE, markupText, levelOffset);
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return fileName + MarkupLanguage.MARKDOWN.getFileNameExtensions().get(0);
    }
}
