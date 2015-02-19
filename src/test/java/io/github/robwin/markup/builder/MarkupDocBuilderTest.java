package io.github.robwin.markup.builder;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Robert Winkler
 */
public class MarkupDocBuilderTest {

    List<String> tableCsvRows;

    @Before
    public void setUp(){
        tableCsvRows = new ArrayList<>();
        tableCsvRows.add("Header 1,Header 2,Header2");
        tableCsvRows.add("Row 1 Column 1,Row 1 Column 2,Row 1 Column 3");
        tableCsvRows.add("Row 2 Column 1,Row 2 Column 2,Row 2 Column 3");
    }


    @Test
    public void testToAsciiDocFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.documentTitle("Test title")
                .sectionTitleLevel1("Section Level 1a")
                .sectionTitleLevel2("Section Level 2a")
                .sectionTitleLevel3("Section Level 3a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .source("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC)", "java")
                .tableWithHeaderRow(tableCsvRows)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .boldTextLine("Bold text line b")
                .italicTextLine("Italic text line b")
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .writeToFile("build/tmp", "test", StandardCharsets.UTF_8);
    }

    @Test
     public void testToMarkdownDocFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.documentTitle("Test title")
                .sectionTitleLevel1("Section Level 1a")
                .sectionTitleLevel2("Section Level 2a")
                .sectionTitleLevel3("Section Level 3a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .source("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC)", "java")
                .tableWithHeaderRow(tableCsvRows)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .boldTextLine("Bold text line b")
                .italicTextLine("Italic text line b")
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .writeToFile("build/tmp", "test", StandardCharsets.UTF_8);
    }

}
