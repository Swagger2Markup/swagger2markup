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
package io.github.swagger2markup.markup.builder.internal.markdown;

import io.github.swagger2markup.markup.builder.*;
import io.github.swagger2markup.markup.builder.internal.AbstractMarkupDocBuilder;
import io.github.swagger2markup.markup.builder.internal.Markup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    protected MarkupLanguage getMarkupLanguage() {
        return MarkupLanguage.MARKDOWN;
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
    public MarkupDocBuilder sectionTitleLevel(int level, String title) {
        sectionTitleLevel(Markdown.TITLE, level, title);
        return this;
    }
    
    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title, String anchor) {
        sectionTitleWithAnchorLevel(Markdown.TITLE, level, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text, boolean hardbreaks) {
        Validate.notBlank(text, "text must not be blank");

        text = text.trim();
        if (hardbreaks)
            text = replaceNewLines(text, Markdown.LINE_BREAK + newLine);
        else
            text = replaceNewLines(text);
        documentBuilder.append(text).append(newLine).append(newLine);

        return this;
    }

    @Override
    public MarkupDocBuilder pageBreak() {
        documentBuilder.append(newLine).append("***").append(newLine);
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
    public MarkupDocBuilder listingBlock(String text, String language) {
        if (language != null)
            text = language + " :" + newLine + text;
        block(text, MarkupBlockStyle.LISTING);
        return this;
    }

    @Override
    public MarkupDocBuilder literalText(String text) {
        boldText(Markdown.LITERAL, text);
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

    private String formatTableCell(String cell) {
        cell = replaceNewLines(cell.trim(), "<br>");
        return cell.replace(Markdown.TABLE_COLUMN_DELIMITER.toString(), "\\" + Markdown.TABLE_COLUMN_DELIMITER.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {
        Validate.notEmpty(cells, "cells must not be null");
        newLine();
        if (columnSpecs != null && !columnSpecs.isEmpty()) {
            Collection<String> headerList = columnSpecs.stream().map(header -> formatTableCell(defaultString(header.header))).collect(Collectors.toList());
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER).append(join(headerList, Markdown.TABLE_COLUMN_DELIMITER.toString())).append(Markdown.TABLE_COLUMN_DELIMITER).append(newLine);

            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER);
            columnSpecs.forEach(col -> {
                documentBuilder.append(StringUtils.repeat(Markdown.TABLE_ROW.toString(), 3));
                documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER);
            });
            documentBuilder.append(newLine);
        }

        for (List<String> row : cells) {
            Collection<String> cellList = row.stream().map(cell -> formatTableCell(defaultString(cell))).collect(Collectors.toList());
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
    public MarkupDocBuilder importMarkup(Reader markupText, MarkupLanguage markupLanguage, int levelOffset) {
        importMarkupStyle1(TITLE_PATTERN, Markdown.TITLE, markupText, markupLanguage, levelOffset);
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return fileName + MarkupLanguage.MARKDOWN.getFileNameExtensions().get(0);
    }
}
