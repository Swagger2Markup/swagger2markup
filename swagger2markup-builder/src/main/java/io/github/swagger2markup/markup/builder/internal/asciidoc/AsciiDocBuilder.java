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
package io.github.swagger2markup.markup.builder.internal.asciidoc;

import io.github.swagger2markup.markup.builder.*;
import io.github.swagger2markup.markup.builder.internal.AbstractMarkupDocBuilder;
import io.github.swagger2markup.markup.builder.internal.Markup;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Robert Winkler
 */
public class AsciiDocBuilder extends AbstractMarkupDocBuilder {

    private static final Pattern TITLE_PATTERN = Pattern.compile(String.format("^(%s{1,%d})\\s+(.*)$", AsciiDoc.TITLE, MAX_TITLE_LEVEL + 1));

    private static final Map<MarkupBlockStyle, String> BLOCK_STYLE = new HashMap<MarkupBlockStyle, String>() {{
        put(MarkupBlockStyle.EXAMPLE, "====");
        put(MarkupBlockStyle.LISTING, "----");
        put(MarkupBlockStyle.LITERAL, "....");
        put(MarkupBlockStyle.PASSTHROUGH, "++++");
        put(MarkupBlockStyle.SIDEBAR, "****");
    }};

    public AsciiDocBuilder() {
        super();
    }

    public AsciiDocBuilder(String newLine) {
        super(newLine);
    }

    protected MarkupLanguage getMarkupLanguage() {
        return MarkupLanguage.ASCIIDOC;
    }

    @Override
    public MarkupDocBuilder copy(boolean copyBuffer) {
        AsciiDocBuilder builder = new AsciiDocBuilder(newLine);

        if (copyBuffer)
            builder.documentBuilder = new StringBuilder(this.documentBuilder);

        return builder.withAnchorPrefix(anchorPrefix);
    }

    @Override
    public MarkupDocBuilder documentTitle(String title) {
        documentTitle(AsciiDoc.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel(int level, String title) {
        sectionTitleLevel(AsciiDoc.TITLE, level, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title, String anchor) {
        sectionTitleWithAnchorLevel(AsciiDoc.TITLE, level, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text, boolean hardbreaks) {
        Validate.notBlank(text, "text must not be blank");
        if (hardbreaks)
            documentBuilder.append("[%hardbreaks]").append(newLine);
        text = text.trim();
        documentBuilder.append(replaceNewLines(text)).append(newLine).append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder pageBreak() {
        documentBuilder.append(newLine).append("<<<").append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder block(String text, final MarkupBlockStyle style, String title, MarkupAdmonition admonition) {
        if (admonition != null)
            documentBuilder.append("[").append(admonition).append("]").append(newLine);
        if (title != null)
            documentBuilder.append(".").append(title).append(newLine);

        delimitedBlockText(new Markup() {
            public String toString() {
                return BLOCK_STYLE.get(style);
            }
        }, text);
        return this;
    }

    @Override
    public MarkupDocBuilder literalText(String text) {
        boldText(AsciiDoc.LITERAL, text);
        return this;
    }

    @Override
    public MarkupDocBuilder boldText(String text) {
        boldText(AsciiDoc.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicText(String text) {
        italicText(AsciiDoc.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list) {
        unorderedList(AsciiDoc.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedListItem(String item) {
        unorderedListItem(AsciiDoc.LIST_ENTRY, item);
        return this;
    }

    @Override
    public MarkupDocBuilder listingBlock(String text, String language) {
        if (language != null)
            documentBuilder.append(String.format("[source,%s]", language)).append(newLine);
        block(text, MarkupBlockStyle.LISTING);
        return this;
    }

    private String normalizeAnchor(String anchor) {
        String normalizedAnchor = "_" + normalizeAnchor(AsciiDoc.SPACE_ESCAPE, anchor);

        if (normalizedAnchor.endsWith("-"))
            normalizedAnchor += "_";

        return normalizedAnchor;
    }

    /**
     * Partial workaround for https://github.com/asciidoctor/asciidoctor/issues/844
     */
    private String normalizeDocument(String document) {
        if (document == null)
            return null;

        return new File(document).toPath().normalize().toString();
    }

    @Override
    public MarkupDocBuilder anchor(String anchor, String text) {
        documentBuilder.append(AsciiDoc.ANCHOR_START).append(normalizeAnchor(anchor));
        if (text != null)
            documentBuilder.append(",").append(text);
        documentBuilder.append(AsciiDoc.ANCHOR_END);
        return this;
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String document, String anchor, String text) {
        documentBuilder.append(AsciiDoc.CROSS_REFERENCE_START);
        if (document != null)
            documentBuilder.append(document).append("#");
        documentBuilder.append(anchor);
        if (text != null) {
            documentBuilder.append(",").append(text);
            if (text.endsWith(">"))
                documentBuilder.append(" ");
        }
        documentBuilder.append(AsciiDoc.CROSS_REFERENCE_END);
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String anchor, String text) {
        return crossReferenceRaw(normalizeDocument(document), normalizeAnchor(anchor), text);
    }

    private String formatTableCell(String cell) {
        cell = replaceNewLines(cell.trim());
        return cell.replace(AsciiDoc.TABLE_COLUMN_DELIMITER.toString(), "\\" + AsciiDoc.TABLE_COLUMN_DELIMITER.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {
        Validate.notEmpty(cells, "cells must not be null");
        Boolean hasHeader = false;
        List<String> options = new ArrayList<>();
        List<String> cols = new ArrayList<>();
        if (columnSpecs != null && !columnSpecs.isEmpty()) {
            for (MarkupTableColumn col : columnSpecs) {
                if (!hasHeader && isNotBlank(col.header)) {
                    options.add("header");
                    hasHeader = true;
                }
                String languageStyle = col.markupSpecifiers.get(MarkupLanguage.ASCIIDOC);
                if (languageStyle != null && isNoneBlank(languageStyle)) {
                    cols.add(languageStyle);
                } else {
                    cols.add(String.valueOf(col.widthRatio) + (col.headerColumn ? "h" : ""));
                }
            }
        }

        newLine();
        documentBuilder.append("[options=\"").append(join(options, ",")).append("\", cols=\"").append(join(cols, ",")).append("\"]").append(newLine);
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        if (hasHeader) {
            Collection<String> headerList = columnSpecs.stream().map(header -> formatTableCell(defaultString(header.header))).collect(Collectors.toList());
            documentBuilder.append(AsciiDoc.TABLE_COLUMN_DELIMITER).append(join(headerList, AsciiDoc.TABLE_COLUMN_DELIMITER.toString())).append(newLine);

        }
        for (List<String> row : cells) {
            Collection<String> cellList = row.stream().map(cell -> formatTableCell(defaultString(cell))).collect(Collectors.toList());
            documentBuilder.append(AsciiDoc.TABLE_COLUMN_DELIMITER).append(join(cellList, AsciiDoc.TABLE_COLUMN_DELIMITER.toString())).append(newLine);
        }
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        newLine();
        return this;
    }

    @Override
    public MarkupDocBuilder newLine(boolean forceLineBreak) {
        newLine(AsciiDoc.LINE_BREAK, forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder importMarkup(Reader markupText, MarkupLanguage markupLanguage, int levelOffset) {
        importMarkupStyle1(TITLE_PATTERN, AsciiDoc.TITLE, markupText, markupLanguage, levelOffset);
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return fileName + MarkupLanguage.ASCIIDOC.getFileNameExtensions().get(0);
    }

}
