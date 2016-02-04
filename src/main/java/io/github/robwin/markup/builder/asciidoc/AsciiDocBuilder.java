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
import io.github.robwin.markup.builder.MarkupTableColumn;
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
    private static final Pattern ANCHOR_FORBIDDEN_PATTERN = Pattern.compile("[^0-9a-zA-Z-_:.\\s]+");
    private static final Pattern XREF_FORBIDDEN_PATTERN = Pattern.compile("[^0-9a-zA-Z-_:./#\\s]+");
    private static final Pattern SPACE_PATTERN = Pattern.compile("[\\s]+");


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

    private static String normalizeAnchor(String anchor) {
        return "_" + SPACE_PATTERN.matcher(ANCHOR_FORBIDDEN_PATTERN.matcher(anchor.trim()).replaceAll("")).replaceAll("_");
    }

    @Override
    public String anchorAsString(String anchor, String text) {
        StringBuilder stringBuilder = new StringBuilder();
        if (text == null)
            stringBuilder.append(AsciiDoc.ANCHOR_START).append(normalizeAnchor(anchor)).append(AsciiDoc.ANCHOR_END);
        else
            stringBuilder.append(AsciiDoc.ANCHOR_START).append(normalizeAnchor(anchor)).append(",").append(text).append(AsciiDoc.ANCHOR_END);
        return stringBuilder.toString();
    }


    protected String normalizedCrossReferenceAsString(String anchor, String text) {
        StringBuilder stringBuilder = new StringBuilder();
        if (text == null)
            stringBuilder.append(AsciiDoc.CROSS_REFERENCE_START).append(anchor).append(AsciiDoc.CROSS_REFERENCE_END);
        else
            stringBuilder.append(AsciiDoc.CROSS_REFERENCE_START).append(anchor).append(",").append(text).append(AsciiDoc.CROSS_REFERENCE_END);
        return stringBuilder.toString();
    }

    private static String normalizeXRef(String anchor) {
        return "_" + SPACE_PATTERN.matcher(XREF_FORBIDDEN_PATTERN.matcher(anchor.trim()).replaceAll("")).replaceAll("_");
    }

    @Override
    public String crossReferenceAsString(String anchor, String text) {
        return normalizedCrossReferenceAsString(normalizeXRef(anchor), text);
    }

    private static String normalizeTitleXRef(String anchor) {
        return anchor.trim();
    }

    @Override
    public String crossReferenceTitleAsString(String anchor, String text) {
        return normalizedCrossReferenceAsString(normalizeTitleXRef(anchor), text);
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
                cols.add(String.valueOf(col.widthRatio));
            }
        }

        newLine();
        documentBuilder.append("[options=\"" + join(options, ",") + "\", cols=\"" + join(cols, ",") + "\"]").append(newLine);
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
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        writeToFileWithoutExtension(directory, fileName + "." + ASCIIDOC_FILE_EXTENSION, charset);
    }
}
