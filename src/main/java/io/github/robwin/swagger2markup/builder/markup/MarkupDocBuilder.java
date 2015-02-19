package io.github.robwin.swagger2markup.builder.markup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Robert Winkler
 */
public interface MarkupDocBuilder {
    MarkupDocBuilder documentTitle(String title);

    MarkupDocBuilder sectionTitleLevel1(String title);

    MarkupDocBuilder sectionTitleLevel2(String title);

    MarkupDocBuilder sectionTitleLevel3(String title);

    MarkupDocBuilder textLine(String text);

    MarkupDocBuilder paragraph(String text);

    MarkupDocBuilder listing(String text);

    MarkupDocBuilder source(String text, String language);

    MarkupDocBuilder boldTextLine(String text);

    MarkupDocBuilder italicTextLine(String text);

    MarkupDocBuilder unorderedList(List<String> list);

    MarkupDocBuilder tableWithHeaderRow(List<String> rowsInCSV);

    MarkupDocBuilder newLine();

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
