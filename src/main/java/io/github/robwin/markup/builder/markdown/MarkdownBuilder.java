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
import io.github.robwin.markup.builder.MarkupTableColumn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

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
    @Override
    public MarkupDocBuilder documentTitle(String title){
        documentTitle(Markdown.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel1(String title){
        sectionTitleLevel1(Markdown.SECTION_TITLE_LEVEL1, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel1(String title, String anchor) {
        sectionTitleLevel1(Markdown.SECTION_TITLE_LEVEL1, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel2(String title){
        sectionTitleLevel2(Markdown.SECTION_TITLE_LEVEL2, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel2(String title, String anchor) {
        sectionTitleLevel2(Markdown.SECTION_TITLE_LEVEL2, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel3(String title){
        sectionTitleLevel3(Markdown.SECTION_TITLE_LEVEL3, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel3(String title, String anchor) {
        sectionTitleLevel3(Markdown.SECTION_TITLE_LEVEL3, title, anchor);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel4(String title){
        sectionTitleLevel4(Markdown.SECTION_TITLE_LEVEL4, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel4(String title, String anchor) {
        sectionTitleLevel4(Markdown.SECTION_TITLE_LEVEL4, title, anchor);
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
    public MarkupDocBuilder boldText(String text){
        boldText(Markdown.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicText(String text) {
        italicText(Markdown.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list){
        unorderedList(Markdown.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedListItem(String item) {
        unorderedListItem(Markdown.LIST_ENTRY, item);
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
    public MarkupDocBuilder crossReference(String document, String title, String text) {
        return crossReferenceRaw(document, normalizeAnchor(title), text);
    }

    private String escapeTableCell(String cell) {
        return cell.replace(Markdown.TABLE_COLUMN_DELIMITER.toString(), Markdown.TABLE_COLUMN_DELIMITER_ESCAPE.toString());
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {
        Validate.notEmpty(columnSpecs);

        newLine();
        Collection<String> headerList = Collections2.transform(columnSpecs, new Function<MarkupTableColumn, String>() {
            public String apply(final MarkupTableColumn header) {
                return escapeTableCell(defaultString(header.header));
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
    public MarkupDocBuilder newLine(boolean forceLineBreak) {
        newLine(Markdown.LINE_BREAK, forceLineBreak);
        return this;
    }

    @Override
    public String addfileExtension(String fileName) {
        return addfileExtension(Markdown.FILE_EXTENSION, fileName);
    }
}
