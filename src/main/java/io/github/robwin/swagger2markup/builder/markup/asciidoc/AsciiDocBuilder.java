package io.github.robwin.swagger2markup.builder.markup.asciidoc;

import io.github.robwin.swagger2markup.builder.markup.AbstractDocumentBuilder;
import io.github.robwin.swagger2markup.builder.markup.DocumentBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Robert Winkler
 */
public class AsciiDocBuilder extends AbstractDocumentBuilder{

    @Override
    public DocumentBuilder documentTitle(String title){
        documentTitle(AsciiDoc.DOCUMENT_TITLE, title);
        return this;
    }

    @Override
    public DocumentBuilder sectionTitleLevel1(String title){
        sectionTitleLevel1(AsciiDoc.SECTION_TITLE_LEVEL1, title);
        return this;
    }

    @Override
    public DocumentBuilder sectionTitleLevel2(String title){
        sectionTitleLevel2(AsciiDoc.SECTION_TITLE_LEVEL2, title);
        return this;
    }

    @Override
    public DocumentBuilder sectionTitleLevel3(String title){
        sectionTitleLevel3(AsciiDoc.SECTION_TITLE_LEVEL3, title);
        return this;
    }

    @Override
    public DocumentBuilder paragraph(String text){
        paragraph(AsciiDoc.HARDBREAKS, text);
        return this;
    }

    @Override
    public DocumentBuilder listing(String text){
        listing(AsciiDoc.LISTING, text);
        return this;
    }

    @Override
    public DocumentBuilder boldTextLine(String text){
        boldTextLine(AsciiDoc.BOLD, text);
        return this;
    }

    @Override
    public DocumentBuilder italicTextLine(String text) {
        italicTextLine(AsciiDoc.ITALIC, text);
        return this;
    }

    @Override
    public DocumentBuilder unorderedList(List<String> list){
        unorderedList(AsciiDoc.LIST_ENTRY, list);
        return this;
    }

    @Override
    public DocumentBuilder source(String text, String language){
        documentBuilder.append(String.format("[source,%s]", language)).append(newLine);
        listing(AsciiDoc.LISTING, text);
        return this;
    }

    @Override
    public DocumentBuilder tableWithHeaderRow(List<String> rowsInCSV){
        documentBuilder.append("[format=\"csv\", options=\"header\"]").append(newLine);
        documentBuilder.append(AsciiDoc.TABLE).append(newLine);
        for(String row : rowsInCSV){
            documentBuilder.append(row).append(newLine);
        }
        documentBuilder.append(AsciiDoc.TABLE).append(newLine).append(newLine);
        return this;
    }

    @Override
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        String fileNameWithExtension = fileName + ".adoc";
        super.writeToFile(directory, fileNameWithExtension, charset);
    }

}
