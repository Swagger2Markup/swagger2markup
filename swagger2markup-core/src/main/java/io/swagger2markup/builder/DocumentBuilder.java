package io.swagger2markup.builder;

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

    DocumentBuilder boldTextLine(String text);

    DocumentBuilder italicTextLine(String text);

    DocumentBuilder unorderedList(List<String> list);

    DocumentBuilder tableWithHeaderRow(List<String> rowsInCSV);

    DocumentBuilder newLine();

    String toString();
}
