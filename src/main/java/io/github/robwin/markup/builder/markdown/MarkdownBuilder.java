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
    public MarkupDocBuilder tableWithHeaderRow(List<String> rowsInCSV){
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

    @Override
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        String fileNameWithExtension = fileName + ".md";
        super.writeToFile(directory, fileNameWithExtension, charset);
    }
}
