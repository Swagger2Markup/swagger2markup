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

    List<String> tableRowsInPSV;
    List<MarkupTableColumn> tableColumns;
    List<List<String>> tableCells;


    @Before
    public void setUp(){
        tableRowsInPSV = new ArrayList<>();
        tableRowsInPSV.add("Header 1 | Header 2 | Header2");
        tableRowsInPSV.add("Row 1, Column 1 | Row 1, Column 2 | Row 1, Column 3");
        tableRowsInPSV.add("Row 2, Column 1 | Row 2, Column 2 | Row 2, Column 3");

        tableColumns = Arrays.asList(
                new MarkupTableColumn().withHeader("Header1"),
                new MarkupTableColumn().withWidthRatio(2),
                new MarkupTableColumn().withHeader("Header3").withWidthRatio(1));
        tableCells = new ArrayList<>();
        tableCells.add(Arrays.asList("Row 1 | Column 1", "Row 1 | Column 2", "Row 1 | Column 3"));
        tableCells.add(Arrays.asList("Row 2 | Column 1", "Row 2 | Column 2", "Row 2 | Column 3"));
    }


    @Test
    public void testToAsciiDocFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.documentTitle("Test title")
                .sectionTitleLevel1("Section Level 1a")
                .sectionTitleWithAnchorLevel1("Section with anchor Level 1a", "level-1a")
                .sectionTitleWithAnchorLevel1("Section with anchor Level 1a")
                .sectionTitleLevel2("Section Level 2a")
                .sectionTitleWithAnchorLevel2("Section with anchor Level 2a", "level-2a")
                .sectionTitleWithAnchorLevel2("Section with anchor Level 2a")
                .sectionTitleLevel3("Section Level 3a")
                .sectionTitleWithAnchorLevel3("Section with anchor Level 3a", "level-3a")
                .sectionTitleWithAnchorLevel3("Section with anchor Level 3a")
                .sectionTitleLevel4("Section Level 4a")
                .sectionTitleWithAnchorLevel4("Section with anchor Level 4a", "level-4a")
                .sectionTitleWithAnchorLevel4("Section with anchor Level 4a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .source("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC)", "java")
                .tableWithHeaderRow(tableRowsInPSV)
                .table(tableCells)
                .tableWithColumnSpecs(tableColumns, tableCells)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .textLine("text line b", true)
                .boldTextLine("Bold text line b", true)
                .italicTextLine("Italic text line b", true)
                .boldText("bold").italicText("italic").text("regular").newLine(true)
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .anchor("anchor", "text").newLine()
                .anchor(" Simple    anchor").newLine()
                .anchor("  \u0240 µ&|ù This .:/-_#  ").newLine()
                .crossReferenceRaw("./document.adoc", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.adoc", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .writeToFile("build/tmp", "test", StandardCharsets.UTF_8);
    }

    @Test
     public void testToMarkdownDocFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.documentTitle("Test title")
                .sectionTitleLevel1("Section Level 1a")
                .sectionTitleWithAnchorLevel1("Section with anchor Level 1a", "level-1a")
                .sectionTitleWithAnchorLevel1("Section with anchor Level 1a")
                .sectionTitleLevel2("Section Level 2a")
                .sectionTitleWithAnchorLevel2("Section with anchor Level 2a", "level-2a")
                .sectionTitleWithAnchorLevel2("Section with anchor Level 2a")
                .sectionTitleLevel3("Section Level 3a")
                .sectionTitleWithAnchorLevel3("Section with anchor Level 3a", "level-3a")
                .sectionTitleWithAnchorLevel3("Section with anchor Level 3a")
                .sectionTitleLevel4("Section Level 4a")
                .sectionTitleWithAnchorLevel4("Section with anchor Level 4a", "level-4a")
                .sectionTitleWithAnchorLevel4("Section with anchor Level 4a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .source("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN)", "java")
                .tableWithHeaderRow(tableRowsInPSV)
                //.table(tableCells)
                .tableWithColumnSpecs(tableColumns, tableCells)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .textLine("text line b", true)
                .boldTextLine("Bold text line b", true)
                .italicTextLine("Italic text line b", true)
                .boldText("bold").italicText("italic").text("regular").newLine(true)
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .anchor("anchor", "text").newLine()
                .anchor(" Simple    anchor").newLine()
                .anchor("  \u0240 µ&|ù This .:/-_#  ").newLine()
                .crossReferenceRaw("./document.md", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.md", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .writeToFile("build/tmp", "test", StandardCharsets.UTF_8);
    }

}
