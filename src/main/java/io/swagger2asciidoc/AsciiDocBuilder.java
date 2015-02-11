package io.swagger2asciidoc;

import java.util.List;

/**
 * @author Robert Winkler
 */
public class AsciiDocBuilder {

    private StringBuilder asciiDocBuilder = new StringBuilder();
    private String newLine = System.getProperty("line.separator");
    private static final String LABEL_DELIMITER = ":: ";
    private static final String TABLE_DELIMITER = "|===";
    private static final String LISTING_DELIMITER = "----";
    private static final String HARDBREAKS_ATTRIBUTE = ":hardbreaks:";
    private static final String DOCUMENT_TITLE = "= ";
    private static final String SECTION_TITLE_LEVEL1 = "== ";
    private static final String SECTION_TITLE_LEVEL2 = "=== ";
    private static final String SECTION_TITLE_LEVEL3 = "==== ";
    private static final String BOLD_DELIMITER = "*";
    private static final String LIST_ENTRY = "* ";

    public AsciiDocBuilder documentTitle(String title){
        asciiDocBuilder.append(DOCUMENT_TITLE).append(title).append(newLine);
        return this;
    }

    public AsciiDocBuilder sectionTitleLevel1(String title){
        asciiDocBuilder.append(SECTION_TITLE_LEVEL1).append(title).append(newLine);
        return this;
    }

    public AsciiDocBuilder sectionTitleLevel2(String title){
        asciiDocBuilder.append(SECTION_TITLE_LEVEL2).append(title).append(newLine);
        return this;
    }

    public AsciiDocBuilder sectionTitleLevel3(String title){
        asciiDocBuilder.append(SECTION_TITLE_LEVEL3).append(title).append(newLine);
        return this;
    }

    public AsciiDocBuilder textLine(String text){
        asciiDocBuilder.append(text).append(newLine);
        return this;
    }

    public AsciiDocBuilder paragraph(String text){
        asciiDocBuilder.append(HARDBREAKS_ATTRIBUTE).append(newLine).append(text).append(newLine).append(newLine);
        return this;
    }

    public AsciiDocBuilder listing(String text){
        delimitedText(LISTING_DELIMITER, text);
        return this;
    }

    private AsciiDocBuilder delimitedText(String delimiter, String text){
        asciiDocBuilder.append(delimiter).append(newLine).append(text).append(newLine).append(delimiter).append(newLine);
        return this;
    }

    public AsciiDocBuilder preserveLineBreaks(){
        asciiDocBuilder.append(HARDBREAKS_ATTRIBUTE).append(newLine);
        return this;
    }

    public AsciiDocBuilder boldTextLine(String text){
        delimitedText(BOLD_DELIMITER, text);
        return this;
    }

    public AsciiDocBuilder textLineWithTitle(String title, String text){
        asciiDocBuilder.append(title).append(LABEL_DELIMITER).append(text).append(newLine);
        return this;
    }

    public AsciiDocBuilder unorderedList(List<String> list){
        for(String listEntry : list){
            asciiDocBuilder.append(LIST_ENTRY).append(listEntry).append(newLine);
        }
        asciiDocBuilder.append(newLine);
        return this;
    }

    public AsciiDocBuilder tableWithHeaderRow(List<String> csvContent){
        asciiDocBuilder.append("[format=\"csv\", options=\"header\"]").append(newLine);
        asciiDocBuilder.append(TABLE_DELIMITER).append(newLine);
        for(String row : csvContent){
            asciiDocBuilder.append(row).append(newLine);
        }
        asciiDocBuilder.append(TABLE_DELIMITER).append(newLine).append(newLine);
        return this;
    }

    public AsciiDocBuilder lineBreak(){
        asciiDocBuilder.append(newLine);
        return this;
    }

    public String toString(){
        return asciiDocBuilder.toString();
    }
}
