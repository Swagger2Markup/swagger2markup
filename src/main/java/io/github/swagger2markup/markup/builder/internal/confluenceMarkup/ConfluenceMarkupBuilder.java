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
package io.github.swagger2markup.markup.builder.internal.confluenceMarkup;

import io.github.swagger2markup.markup.builder.*;
import io.github.swagger2markup.markup.builder.internal.AbstractMarkupDocBuilder;
import io.github.swagger2markup.markup.builder.internal.Markup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

public final class ConfluenceMarkupBuilder extends AbstractMarkupDocBuilder {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^h([0-9])\\.\\s+(.*)$");
    private static final String TITLE_FORMAT = "h%d. %s";
    private static final Pattern ESCAPE_CELL_PIPE_PATTERN = Pattern.compile("((\\[.*?(?<!\\\\)\\])|(.))");

    /**
     * Associate macro name to block style.<br>
     * ending ':' means the macro supports title attribute.<br>
     * '>ADMONITION_BLOCK' means value should refer to {@link #ADMONITION_BLOCK_STYLE}.
     */
    private static final Map<MarkupBlockStyle, String> BLOCK_STYLE = new HashMap<MarkupBlockStyle, String>() {{
        put(MarkupBlockStyle.EXAMPLE, ">ADMONITION_BLOCK");
        put(MarkupBlockStyle.LISTING, "code:");
        put(MarkupBlockStyle.LITERAL, "noformat");
        put(MarkupBlockStyle.PASSTHROUGH, "html");
        put(MarkupBlockStyle.SIDEBAR, ">ADMONITION_BLOCK");
    }};

    private static final Map<MarkupAdmonition, String> ADMONITION_BLOCK_STYLE = new HashMap<MarkupAdmonition, String>() {{
        put(null, "panel:");
        put(MarkupAdmonition.CAUTION, "note:");
        put(MarkupAdmonition.IMPORTANT, "alert:");
        put(MarkupAdmonition.NOTE, "info:");
        put(MarkupAdmonition.TIP, "tip:");
        put(MarkupAdmonition.WARNING, "warning:");
    }};

    public ConfluenceMarkupBuilder() {
        super();
    }

    public ConfluenceMarkupBuilder(String newLine) {
        super(newLine);
    }

    protected MarkupLanguage getMarkupLanguage() {
        return MarkupLanguage.CONFLUENCE_MARKUP;
    }

    @Override
    public MarkupDocBuilder copy(boolean copyBuffer) {
        ConfluenceMarkupBuilder builder = new ConfluenceMarkupBuilder(newLine);

        if (copyBuffer)
            builder.documentBuilder = new StringBuilder(this.documentBuilder);

        return builder.withAnchorPrefix(anchorPrefix);
    }

    @Override
    public MarkupDocBuilder documentTitle(String title) {
        Validate.notBlank(title, "title must not be blank");
        documentBuilder.append(String.format(TITLE_FORMAT, 1, title));
        documentBuilder.append(newLine).append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel(int level, String title) {
        Validate.notBlank(title, "title must not be blank");
        Validate.inclusiveBetween(1, MAX_TITLE_LEVEL, level);

        documentBuilder.append(newLine);
        documentBuilder.append(String.format(TITLE_FORMAT, level + 1, replaceNewLinesWithWhiteSpace(title)));
        documentBuilder.append(newLine);

        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title, String anchor) {
        Validate.notBlank(title, "title must not be blank");
        Validate.inclusiveBetween(1, MAX_TITLE_LEVEL, level);

        documentBuilder.append(newLine);
        documentBuilder.append(String.format(TITLE_FORMAT, level + 1, replaceNewLinesWithWhiteSpace(title)));
        if (isBlank(anchor))
            anchor = title;
        documentBuilder.append(" ");
        anchor(replaceNewLinesWithWhiteSpace(anchor));
        
        documentBuilder.append(newLine);

        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text, boolean hardbreaks) {
        Validate.notBlank(text, "text must not be blank");

        text = text.trim();
        if (hardbreaks)
            text = replaceNewLines(text, ConfluenceMarkup.LINE_BREAK + newLine);
        else
            text = replaceNewLines(text);
        documentBuilder.append(text).append(newLine).append(newLine);

        return this;
    }

    @Override
    public MarkupDocBuilder pageBreak() {
        documentBuilder.append(newLine).append("<div style='page-break-before:always;'></div>").append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder block(String text, final MarkupBlockStyle style, String title, MarkupAdmonition admonition) {

        String block = BLOCK_STYLE.get(style);

        boolean admonitionBlock = block.equals(">ADMONITION_BLOCK");
        if (admonitionBlock) {
            block = ADMONITION_BLOCK_STYLE.get(admonition);
        }

        boolean supportTitle = false;
        if (block.endsWith(":")) {
            supportTitle = true;
            block = StringUtils.stripEnd(block, ":");
        }

        String titleString = null;
        if (admonition != null && !admonitionBlock) {
            titleString = StringUtils.capitalize(admonition.name().toLowerCase());
        }
        if (title != null) {
            titleString = (titleString == null ? "" : titleString + " | ") + title;
        }

        final String finalBlock = block;
        Markup blockMarkup = new Markup() {
            @Override
            public String toString() {
                return String.format("{%s}", finalBlock);
            }
        };

        if (!supportTitle) {
            if (titleString != null)
                documentBuilder.append(titleString).append(" : ").append(newLine);
            delimitedBlockText(blockMarkup, text);
        } else {
            final String finalTitleString = titleString;
            delimitedBlockText(new Markup() {
                @Override
                public String toString() {
                    if (finalTitleString == null)
                        return String.format("{%s}", finalBlock);
                    else
                        return String.format("{%s:title=%s}", finalBlock, finalTitleString);
                }
            }, text, blockMarkup);
        }

        return this;
    }

    @Override
    public MarkupDocBuilder listingBlock(String text, final String language) {
        Markup blockMarkup = new Markup() {
            @Override
            public String toString() {
                return String.format("{%s}", "code");
            }
        };

        if (language != null) {
            delimitedBlockText(new Markup() {
                @Override
                public String toString() {
                    return String.format("{code:language=%s}", language);
                }
            }, text, blockMarkup);
        } else {
            delimitedBlockText(blockMarkup, text);
        }
        return this;
    }

    @Override
    public MarkupDocBuilder literalText(String text) {
        boldText(ConfluenceMarkup.LITERAL, text);
        return this;
    }

    @Override
    public MarkupDocBuilder boldText(String text) {
        boldText(ConfluenceMarkup.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicText(String text) {
        italicText(ConfluenceMarkup.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list) {
        unorderedList(ConfluenceMarkup.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedListItem(String item) {
        unorderedListItem(ConfluenceMarkup.LIST_ENTRY, item);
        return this;
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {
        Validate.notEmpty(cells, "cells must not be null");
        documentBuilder.append(newLine);
        if (columnSpecs != null && !columnSpecs.isEmpty()) {
            documentBuilder.append("||");
            for (MarkupTableColumn column : columnSpecs) {
                documentBuilder.append(formatCellContent(defaultString(column.header))).append("||");
            }
            documentBuilder.append(newLine);
        }
        for (List<String> row : cells) {
            documentBuilder.append(ConfluenceMarkup.TABLE_COLUMN_DELIMITER);
            ListIterator<String> cellIterator = row.listIterator();
            while (cellIterator.hasNext()) {
                int cellIndex = cellIterator.nextIndex();
                if (columnSpecs != null && columnSpecs.size() > cellIndex && columnSpecs.get(cellIndex).headerColumn)
                    documentBuilder.append(ConfluenceMarkup.TABLE_COLUMN_DELIMITER);

                documentBuilder.append(formatCellContent(cellIterator.next())).append(ConfluenceMarkup.TABLE_COLUMN_DELIMITER);
            }
            documentBuilder.append(newLine);
        }
        documentBuilder.append(newLine);
        return this;
    }

    private String formatCellContent(String cell) {
        cell = replaceNewLines(cell.trim(), ConfluenceMarkup.LINE_BREAK.toString());
        if (isBlank(cell)) {
            return " ";
        }
        return escapeCellPipes(cell);
    }

    private String escapeCellPipes(String cell) {
        Matcher m = ESCAPE_CELL_PIPE_PATTERN.matcher(cell);

        StringBuffer res = new StringBuffer();
        while (m.find()) {
            String repl = m.group(1);
            if (repl.equals(ConfluenceMarkup.TABLE_COLUMN_DELIMITER.toString()))
                repl = "\\" + ConfluenceMarkup.TABLE_COLUMN_DELIMITER.toString();
            m.appendReplacement(res, Matcher.quoteReplacement(repl));
        }
        m.appendTail(res);

        return res.toString();
    }

    private String normalizeAnchor(String anchor) {
        return normalizeAnchor(ConfluenceMarkup.SPACE_ESCAPE, anchor);
    }


    @Override
    public MarkupDocBuilder anchor(String anchor, String text) {
        documentBuilder.append(ConfluenceMarkup.ANCHOR_START).append(normalizeAnchor(anchor)).append(ConfluenceMarkup.ANCHOR_END);
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String anchor, String text) {
        crossReferenceRaw(document, normalizeAnchor(anchor), text);
        return this;
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String document, String anchor, String text) {
        documentBuilder.append("[");
        if (isNotBlank(text)) {
            documentBuilder.append(text).append("|");
        }
        if (isNotBlank(document)) {
            documentBuilder.append(document);
        }
        documentBuilder.append("#").append(anchor);
        documentBuilder.append("]");
        return this;
    }

    @Override
    public MarkupDocBuilder newLine(boolean forceLineBreak) {
        newLine(ConfluenceMarkup.LINE_BREAK, forceLineBreak);
        return this;
    }

    @Override
    public MarkupDocBuilder importMarkup(Reader markupText, MarkupLanguage markupLanguage, int levelOffset) {
        importMarkupStyle2(TITLE_PATTERN, TITLE_FORMAT, false, markupText, markupLanguage, levelOffset);
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return fileName + MarkupLanguage.CONFLUENCE_MARKUP.getFileNameExtensions().get(0);
    }
}
