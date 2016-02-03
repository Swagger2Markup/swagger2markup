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
package io.github.robwin.markup.builder.markdown;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import io.github.robwin.markup.builder.AbstractMarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Robert Winkler
 */
public class MarkdownBuilder extends AbstractMarkupDocBuilder
{
    private static final String MARKDOWN_FILE_EXTENSION = "md";

    @Override
    public MarkupDocBuilder documentTitle(String title){
        documentTitle(Markdown.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder documentTitleWithAttributes(String title) {
        documentTitle(Markdown.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel1(String title){
        sectionTitleLevel1(Markdown.SECTION_TITLE_LEVEL1, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel2(String title){
        sectionTitleLevel2(Markdown.SECTION_TITLE_LEVEL2, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel3(String title){
        sectionTitleLevel3(Markdown.SECTION_TITLE_LEVEL3, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel4(String title){
        sectionTitleLevel4(Markdown.SECTION_TITLE_LEVEL4, title);
        return this;
    }

    @Override
    public MarkupDocBuilder paragraph(String text){
        paragraph(Markdown.HARDBREAKS, text);
        return this;
    }

    @Override
    public MarkupDocBuilder listing(String text){
        listing(Markdown.LISTING, text);
        return this;
    }

    @Override
    public MarkupDocBuilder source(String text, String language){
        documentBuilder.append(Markdown.LISTING).append(language).append(newLine).
                append(text).append(newLine).
                append(Markdown.LISTING).append(newLine).append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder boldTextLine(String text){
        boldTextLine(Markdown.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicTextLine(String text) {
        italicTextLine(Markdown.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list){
        unorderedList(Markdown.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder tableWithHeaderRow(List<String> rowsInPSV){
        String headersInPSV = rowsInPSV.get(0);
        List<String> contentRowsInPSV = rowsInPSV.subList(1, rowsInPSV.size());
        String[] headersAsArray = headersInPSV.split(String.format("\\%s", Markdown.TABLE_COLUMN_DELIMITER.toString()));
        List<String> headers = Arrays.asList(headersAsArray);

        newLine();
        // Header
        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER.toString());
        documentBuilder.append(headersInPSV);
        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER.toString());
        newLine();
        // Header/Content separator
        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER.toString());
        for(String header : headers){
            for(int i = 1; i<5; i++) {
                documentBuilder.append(Markdown.TABLE_ROW);
            }
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER.toString());
        }
        newLine();
        // Content
        for(String contentRowInPSV : contentRowsInPSV){
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER.toString());
            documentBuilder.append(contentRowInPSV);
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER.toString());
            newLine();
        }
        newLine().newLine();
        return this;
    }

    @Override
    public MarkupDocBuilder anchor(String anchor) {
        documentBuilder.append(anchorAsString(anchor));
        return this;
    }

    @Override
    public String anchorAsString(String anchor) {
        return "";
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
            stringBuilder.append(anchor);
        else
            stringBuilder.append(text);
        return stringBuilder.toString();
    }

    private String escapeTableCell(String cell) {
        return cell.replace(Markdown.TABLE_COLUMN_DELIMITER.toString(), Markdown.TABLE_COLUMN_DELIMITER_ESCAPE.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<TableColumnSpec> columns, List<List<String>> cells) {
        if (CollectionUtils.isEmpty(columns))
            throw new RuntimeException("Header is mandatory in Markdown");

        newLine();
        Collection<String> headerList = Collections2.transform(columns, new Function<TableColumnSpec, String>() {
            public String apply(final TableColumnSpec header) {
                return escapeTableCell(defaultString(header.header));
            }
        });
        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER).append(join(headerList, Markdown.TABLE_COLUMN_DELIMITER.toString())).append(Markdown.TABLE_COLUMN_DELIMITER).append(newLine);

        documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER);
        for (TableColumnSpec col : columns) {
            documentBuilder.append(StringUtils.repeat(Markdown.TABLE_ROW.toString(), 3));
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER);
        }
        documentBuilder.append(newLine);

        for (List<String> row : cells) {
            Collection<String> cellList = Collections2.transform(row, new Function<String, String>() {
                public String apply(final String cell) {
                    return escapeTableCell(cell);
                }
            });
            documentBuilder.append(Markdown.TABLE_COLUMN_DELIMITER).append(join(cellList, Markdown.TABLE_COLUMN_DELIMITER.toString())).append(Markdown.TABLE_COLUMN_DELIMITER).append(newLine);
        }
        newLine();

        return this;
    }

    @Override
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        writeToFileWithExtension(directory, fileName + "." + MARKDOWN_FILE_EXTENSION, charset);
    }
}
