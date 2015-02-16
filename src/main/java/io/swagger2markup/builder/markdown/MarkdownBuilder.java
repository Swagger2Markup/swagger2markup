package io.swagger2markup.builder.markdown;

import io.swagger2markup.builder.AbstractDocumentBuilder;
import io.swagger2markup.builder.DocumentBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Project:   swagger2asciidoc
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
public class MarkdownBuilder extends AbstractDocumentBuilder
{
    @Override
    public DocumentBuilder documentTitle(String title){
        documentTitle(Markdown.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public DocumentBuilder sectionTitleLevel1(String title){
        sectionTitleLevel1(Markdown.SECTION_TITLE_LEVEL1, title);
        return this;
    }

    @Override
    public DocumentBuilder sectionTitleLevel2(String title){
        sectionTitleLevel2(Markdown.SECTION_TITLE_LEVEL2, title);
        return this;
    }

    @Override
    public DocumentBuilder sectionTitleLevel3(String title){
        sectionTitleLevel3(Markdown.SECTION_TITLE_LEVEL3, title);
        return this;
    }

    @Override
    public DocumentBuilder paragraph(String text){
        paragraph(Markdown.HARDBREAKS, text);
        return this;
    }

    @Override
    public DocumentBuilder listing(String text){
        listing(Markdown.LISTING, text);
        return this;
    }

    @Override
    public DocumentBuilder boldTextLine(String text){
        boldTextLine(Markdown.BOLD, text);
        return this;
    }

    @Override
    public DocumentBuilder italicTextLine(String text) {
        italicTextLine(Markdown.ITALIC, text);
        return this;
    }

    @Override
    public DocumentBuilder unorderedList(List<String> list){
        unorderedList(Markdown.LIST_ENTRY, list);
        return this;
    }

    @Override
    public DocumentBuilder tableWithHeaderRow(List<String> rowsInCSV){
        String headersInCSV = rowsInCSV.get(0);
        List<String> contentRowsInCSV = rowsInCSV.subList(1, rowsInCSV.size());
        List<String> headers = Arrays.asList(headersInCSV.split(","));
        // Header
        documentBuilder.append(Markdown.TABLE_COLUMN);
        for(String header : headers){
            documentBuilder.append(header).append(Markdown.TABLE_COLUMN);
        }
        newLine();
        // Header/Content separator
        documentBuilder.append(Markdown.TABLE_COLUMN);
        for(String header : headers){
            for(int i = 1; i<5; i++) {
                documentBuilder.append(Markdown.TABLE_ROW);
            }
            documentBuilder.append(Markdown.TABLE_COLUMN);
        }
        newLine();
        // Content
        for(String contentRow : contentRowsInCSV){
            documentBuilder.append(Markdown.TABLE_COLUMN);
            List<String> columns = Arrays.asList(contentRow.split(","));
            for(String columnText : columns){
                documentBuilder.append(columnText).append(Markdown.TABLE_COLUMN);
            }
            newLine();
        }
        newLine().newLine();
        return this;
    }
}
