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
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.markup.builder.MarkupTableColumn;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Robert Winkler
 */
public class AsciiDocBuilder extends AbstractMarkupDocBuilder {

    @Override
    public MarkupDocBuilder copy() {
        return new AsciiDocBuilder().withAnchorPrefix(anchorPrefix);
    }

    @Override
    public MarkupDocBuilder documentTitle(String title){
        documentTitle(AsciiDoc.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel1(String title){
        sectionTitleLevel1(AsciiDoc.SECTION_TITLE_LEVEL1, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel1(String title, String anchor) {
        sectionTitleLevel1(AsciiDoc.SECTION_TITLE_LEVEL1, title, (anchor == null ? title : anchor));
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel2(String title){
        sectionTitleLevel2(AsciiDoc.SECTION_TITLE_LEVEL2, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel2(String title, String anchor) {
        sectionTitleLevel2(AsciiDoc.SECTION_TITLE_LEVEL2, title, (anchor == null ? title : anchor));
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel3(String title){
        sectionTitleLevel3(AsciiDoc.SECTION_TITLE_LEVEL3, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel3(String title, String anchor) {
        sectionTitleLevel3(AsciiDoc.SECTION_TITLE_LEVEL3, title, (anchor == null ? title : anchor));
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel4(String title){
        sectionTitleLevel4(AsciiDoc.SECTION_TITLE_LEVEL4, title, null);
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel4(String title, String anchor) {
        sectionTitleLevel4(AsciiDoc.SECTION_TITLE_LEVEL4, title, (anchor == null ? title : anchor));
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
    public MarkupDocBuilder boldText(String text){
        boldText(AsciiDoc.BOLD, text);
        return this;
    }

    @Override
    public MarkupDocBuilder italicText(String text) {
        italicText(AsciiDoc.ITALIC, text);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list){
        unorderedList(AsciiDoc.LIST_ENTRY, list);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedListItem(String item) {
        unorderedListItem(AsciiDoc.LIST_ENTRY, item);
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
                }
                else {
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
    public MarkupDocBuilder importMarkup(String markupText, int levelOffset) {
        importMarkup(AsciiDoc.TITLE, markupText, levelOffset);
        return this;
    }

    @Override
    public String addfileExtension(String fileName) {
        return addfileExtension(AsciiDoc.FILE_EXTENSION, fileName);
    }

}
