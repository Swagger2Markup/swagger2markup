package io.github.robwin.swagger2markup.builder.markup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Robert Winkler
 */
public interface DocumentBuilder {
    DocumentBuilder documentTitle(String title);

    DocumentBuilder sectionTitleLevel1(String title);

    DocumentBuilder sectionTitleLevel2(String title);

    DocumentBuilder sectionTitleLevel3(String title);

    DocumentBuilder textLine(String text);

    DocumentBuilder paragraph(String text);

    DocumentBuilder listing(String text);

    DocumentBuilder source(String text, String language);

    DocumentBuilder boldTextLine(String text);

    DocumentBuilder italicTextLine(String text);

    DocumentBuilder unorderedList(List<String> list);

    DocumentBuilder tableWithHeaderRow(List<String> rowsInCSV);

    DocumentBuilder newLine();

    /**
     * Returns a string representation of the document.
     */
    String toString();

    /**
     * Writes the content of the builder to a file and clears the builder.
     *
     * @param directory the directory where the generated file should be stored
     * @param fileName the name of the file
     * @param charset the the charset to use for encoding
     * @throws IOException if the file cannot be written
     */
    void writeToFile(String directory, String fileName, Charset charset) throws IOException;
}
