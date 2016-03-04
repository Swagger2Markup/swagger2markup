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
package io.github.robwin.markup.builder.asciidoc;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import io.github.robwin.markup.builder.*;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Robert Winkler
 */
public class AsciiDocBuilder extends AbstractMarkupDocBuilder {

    public AsciiDocBuilder(){
        super(System.getProperty("line.separator"));
    }

    public AsciiDocBuilder(String lineSeparator){
        super(lineSeparator);
    }

    private static final Map<MarkupBlockStyle, String> BLOCK_STYLE = new HashMap<MarkupBlockStyle, String>() {{
        put(MarkupBlockStyle.EXAMPLE, "====");
        put(MarkupBlockStyle.LISTING, "----");
        put(MarkupBlockStyle.LITERAL, "....");
        put(MarkupBlockStyle.PASSTHROUGH, "++++");
        put(MarkupBlockStyle.SIDEBAR, "****");
    }};

    @Override
    public MarkupDocBuilder copy() {
        return new AsciiDocBuilder().withAnchorPrefix(anchorPrefix);
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
                assert (BLOCK_STYLE.containsKey(style));
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

    @Override
    public MarkupDocBuilder tableWithHeaderRow(List<String> rowsInPSV) {
        newLine();
        documentBuilder.append("[options=\"header\"]").append(newLine);
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        for (String row : rowsInPSV) {
            documentBuilder.append(AsciiDoc.TABLE_COLUMN_DELIMITER).append(row).append(newLine);
        }
        documentBuilder.append(AsciiDoc.TABLE).append(newLine).append(newLine);
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
            Collection<String> headerList = Collections2.transform(columnSpecs, new Function<MarkupTableColumn, String>() {
                public String apply(final MarkupTableColumn header) {
                    return escapeTableCell(defaultString(header.header));
                }
            });
            documentBuilder.append(AsciiDoc.TABLE_COLUMN_DELIMITER).append(join(headerList, AsciiDoc.TABLE_COLUMN_DELIMITER.toString())).append(newLine);

        }
        for (List<String> row : cells) {
            Collection<String> cellList = Collections2.transform(row, new Function<String, String>() {
                public String apply(final String cell) {
                    return escapeTableCell(cell);
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
        importMarkup(AsciiDoc.TITLE, markupText, levelOffset);
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return addFileExtension(AsciiDoc.FILE_EXTENSION, fileName);
    }

}
