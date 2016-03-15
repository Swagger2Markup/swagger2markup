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
package io.github.robwin.markup.builder.internal.asciidoc;

import io.github.robwin.markup.builder.*;
import io.github.robwin.markup.builder.internal.AbstractMarkupDocBuilder;
import io.github.robwin.markup.builder.internal.Markup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;

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

    public AsciiDocBuilder(){
        super();
    }

    public AsciiDocBuilder(String newLine){
        super(newLine);
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
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title, String anchor) {
        sectionTitleWithAnchorLevel(AsciiDoc.TITLE, level, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text) {
        paragraph(AsciiDoc.HARDBREAKS, text);
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
    public MarkupDocBuilder listing(String text, String language) {
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
        if (text != null)
            documentBuilder.append(",").append(text);
        documentBuilder.append(AsciiDoc.CROSS_REFERENCE_END);
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String anchor, String text) {
        return crossReferenceRaw(normalizeDocument(document), normalizeAnchor(anchor), text);
    }

    private String escapeTableCell(String cell) {
        return cell.replace(AsciiDoc.TABLE_COLUMN_DELIMITER.toString(), AsciiDoc.TABLE_COLUMN_DELIMITER_ESCAPE.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {

        Boolean hasHeader = false;
        List<String> options = new ArrayList<>();
        List<String> cols = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(columnSpecs)) {
            for (MarkupTableColumn col : columnSpecs) {
                if (!hasHeader && isNotBlank(col.header)) {
                    options.add("header");
                    hasHeader = true;
                }
                String languageStyle = col.markupSpecifiers.get(MarkupLanguage.ASCIIDOC);
                if (languageStyle != null && isNoneBlank(languageStyle)) {
                    cols.add(languageStyle);
                } else {
                    cols.add(String.valueOf(col.widthRatio));
                }
            }
        }

        newLine();
        documentBuilder.append("[options=\"").append(join(options, ",")).append("\", cols=\"").append(join(cols, ",")).append("\"]").append(newLine);
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        if (hasHeader) {
            Collection<String> headerList =  CollectionUtils.collect(columnSpecs, new Transformer<MarkupTableColumn, String>() {
                public String transform(final MarkupTableColumn header) {
                    return escapeTableCell(replaceNewLinesWithWhiteSpace(defaultString(header.header)));
                }
            });
            documentBuilder.append(AsciiDoc.TABLE_COLUMN_DELIMITER).append(join(headerList, AsciiDoc.TABLE_COLUMN_DELIMITER.toString())).append(newLine);

        }
        for (List<String> row : cells) {
            Collection<String> cellList =  CollectionUtils.collect(row, new Transformer<String, String>() {
                public String transform(final String cell) {
                    return escapeTableCell(replaceNewLines(defaultString(cell)));
                }
            });
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
    public MarkupDocBuilder importMarkup(Reader markupText, int levelOffset) throws IOException {
        importMarkupStyle1(TITLE_PATTERN, AsciiDoc.TITLE, markupText, levelOffset);
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return fileName + MarkupLanguage.ASCIIDOC.getFileNameExtensions().get(0);
    }

}
