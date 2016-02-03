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
import io.github.robwin.markup.builder.AbstractMarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Robert Winkler
 */
public class AsciiDocBuilder extends AbstractMarkupDocBuilder {

    private static final String ASCIIDOC_FILE_EXTENSION = "adoc";
    private static final Pattern ANCHOR_ESCAPE_PATTERN = Pattern.compile("[^0-9a-zA-Z]");

    @Override
    public MarkupDocBuilder documentTitle(String title){
        documentTitle(AsciiDoc.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder documentTitleWithAttributes(String title) {
        documentTitle(AsciiDoc.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel1(String title){
        sectionTitleLevel1(AsciiDoc.SECTION_TITLE_LEVEL1, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel2(String title){
        sectionTitleLevel2(AsciiDoc.SECTION_TITLE_LEVEL2, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel3(String title){
        sectionTitleLevel3(AsciiDoc.SECTION_TITLE_LEVEL3, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel4(String title){
        sectionTitleLevel4(AsciiDoc.SECTION_TITLE_LEVEL4, title);
        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text){
        paragraph(AsciiDoc.HARDBREAKS, text);
        return this;
    }

    @Override
    public MarkupDocBuilder listing(String text){
        listing(AsciiDoc.LISTING, text);
        return this;
    }

    @Override
    public MarkupDocBuilder boldTextLine(String text){
        boldTextLine(AsciiDoc.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicTextLine(String text) {
        italicTextLine(AsciiDoc.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list){
        unorderedList(AsciiDoc.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder source(String text, String language){
        documentBuilder.append(String.format("[source,%s]", language)).append(newLine);
        listing(AsciiDoc.LISTING, text);
        return this;
    }

    @Override
    public MarkupDocBuilder tableWithHeaderRow(List<String> rowsInPSV){
        newLine();
        documentBuilder.append("[options=\"header\"]").append(newLine);
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        for(String row : rowsInPSV){
            documentBuilder.append(AsciiDoc.TABLE_COLUMN_DELIMITER).append(row).append(newLine);
        }
        documentBuilder.append(AsciiDoc.TABLE).append(newLine).append(newLine);
        return this;
    }

    private static String normalizeReferenceAnchor(String anchor) {
        return ANCHOR_ESCAPE_PATTERN.matcher(anchor).replaceAll("_");
    }

    @Override
    public MarkupDocBuilder anchor(String anchor) {
        documentBuilder.append(anchorAsString(anchor));
        return this;
    }

    @Override
    public String anchorAsString(String anchor) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AsciiDoc.ANCHOR_START).append(normalizeReferenceAnchor(anchor)).append(AsciiDoc.ANCHOR_END);
        return stringBuilder.toString();
    }

    @Override
    public MarkupDocBuilder crossReference(String anchor, String text) {
        documentBuilder.append(crossReferenceAsString(anchor, text));
        return this;
    }

    @Override
    public String crossReferenceAsString(String anchor, String text) {
        StringBuilder stringBuilder = new StringBuilder();
        if (text == null)
            stringBuilder.append(AsciiDoc.CROSS_REFERENCE_START).append(normalizeReferenceAnchor(anchor)).append(AsciiDoc.CROSS_REFERENCE_END);
        else
            stringBuilder.append(AsciiDoc.CROSS_REFERENCE_START).append(normalizeReferenceAnchor(anchor)).append(",").append(text).append(AsciiDoc.CROSS_REFERENCE_END);
        return stringBuilder.toString();
    }

    private String escapeTableCell(String cell) {
        return cell.replace(AsciiDoc.TABLE_COLUMN_DELIMITER.toString(), AsciiDoc.TABLE_COLUMN_DELIMITER_ESCAPE.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<TableColumnSpec> columns, List<List<String>> cells) {

        Boolean hasHeader = false;
        List<String> options = new ArrayList<>();
        List<String> cols = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(columns)) {
            for (TableColumnSpec col : columns) {
                if (!hasHeader && isNotBlank(col.header)) {
                    options.add("header");
                    hasHeader = true;
                }
                cols.add(String.valueOf(col.widthRatio));
            }
        }

        newLine();
        documentBuilder.append("[options=\"" + join(options, ",") + "\", cols=\"" + join(cols, ",") + "\"]").append(newLine);
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        if (hasHeader) {
            Collection<String> headerList = Collections2.transform(columns, new Function<TableColumnSpec, String>() {
                public String apply(final TableColumnSpec header) {
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
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        writeToFileWithExtension(directory, fileName + "." + ASCIIDOC_FILE_EXTENSION, charset);
    }
}
