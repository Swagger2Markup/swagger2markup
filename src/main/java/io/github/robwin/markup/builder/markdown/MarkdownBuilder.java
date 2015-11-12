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

import io.github.robwin.markup.builder.AbstractMarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

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
        sectionTitleLevel3(Markdown.SECTION_TITLE_LEVEL4, title);
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
    // TODO
    public MarkupDocBuilder crossReference(String text) {
        throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        String fileNameWithExtension = fileName + ".md";
        super.writeToFile(directory, fileNameWithExtension, charset);
    }
}
