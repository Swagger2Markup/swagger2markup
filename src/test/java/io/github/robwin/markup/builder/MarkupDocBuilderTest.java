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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


/**
 * @author Robert Winkler
 */
public class MarkupDocBuilderTest {

    private final String newLine = System.getProperty("line.separator");

    private List<MarkupTableColumn> tableColumns;
    private List<List<String>> tableCells;

    @Before
    public void setUp() {
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

        builder = builder.documentTitle("Test title")
                .sectionTitleLevel(1, "Section Level 1a")
                .sectionTitleWithAnchorLevel(1, "Section with anchor Level 1a", "level-1a")
                .sectionTitleWithAnchorLevel(1, "Section with anchor Level 1a")
                .sectionTitleLevel(2, "Section Level 2a")
                .sectionTitleWithAnchorLevel(2, "Section with anchor Level 2a", "level-2a")
                .sectionTitleWithAnchorLevel(2, "Section with anchor Level 2a")
                .sectionTitleLevel(3, "Section Level 3a")
                .sectionTitleWithAnchorLevel(3, "Section with anchor Level 3a", "level-3a")
                .sectionTitleWithAnchorLevel(3, "Section with anchor Level 3a")
                .sectionTitleLevel(4, "Section Level 4a")
                .sectionTitleWithAnchorLevel(4, "Section with anchor Level 4a", "level-4a")
                .sectionTitleWithAnchorLevel(4, "Section with anchor Level 4a")
                .sectionTitleLevel(5, "Section Level 5a")
                .sectionTitleWithAnchorLevel(5, "Section with anchor Level 5a", "level-5a")
                .sectionTitleWithAnchorLevel(5, "Section with anchor Level 5a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .listing("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN)", "java")
                .block("Example", MarkupBlockStyle.EXAMPLE)
                .block("Example", MarkupBlockStyle.EXAMPLE, "Example", null)
                .block("Example", MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.IMPORTANT)
                .block("Listing", MarkupBlockStyle.LISTING, null, MarkupAdmonition.CAUTION)
                .block("Literal", MarkupBlockStyle.LITERAL, null, MarkupAdmonition.NOTE)
                .block("Sidebar", MarkupBlockStyle.SIDEBAR, null, MarkupAdmonition.TIP)
                .block("Passthrough", MarkupBlockStyle.PASSTHROUGH, null, MarkupAdmonition.WARNING)
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
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        builder.writeToFileWithoutExtension(builder.addFileExtension(Paths.get("build/tmp/test")), StandardCharsets.UTF_8);
        builder.writeToFile(Paths.get("build/tmp/test"), StandardCharsets.UTF_8);
        builder.writeToFileWithoutExtension("build/tmp", builder.addFileExtension("test"), StandardCharsets.UTF_8);
        builder.writeToFile("build/tmp", "test", StandardCharsets.UTF_8);

        MarkupDocBuilder builderWithConfig = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC).withAnchorPrefix(" mdb test- ");
        String prefixMarkup = builderWithConfig.anchor("anchor", "text")
                .crossReference("anchor", "text")
                .toString();

        assertEquals("[[_mdb_test-anchor,text]]<<_mdb_test-anchor,text>>", prefixMarkup);
    }

    @Test
    public void testToMarkdownDocFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);

        builder = builder.documentTitle("Test title")
                .sectionTitleLevel(1, "Section Level 1a")
                .sectionTitleWithAnchorLevel(1, "Section with anchor Level 1a", "level-1a")
                .sectionTitleWithAnchorLevel(1, "Section with anchor Level 1a")
                .sectionTitleLevel(2, "Section Level 2a")
                .sectionTitleWithAnchorLevel(2, "Section with anchor Level 2a", "level-2a")
                .sectionTitleWithAnchorLevel(2, "Section with anchor Level 2a")
                .sectionTitleLevel(3, "Section Level 3a")
                .sectionTitleWithAnchorLevel(3, "Section with anchor Level 3a", "level-3a")
                .sectionTitleWithAnchorLevel(3, "Section with anchor Level 3a")
                .sectionTitleLevel(4, "Section Level 4a")
                .sectionTitleWithAnchorLevel(4, "Section with anchor Level 4a", "level-4a")
                .sectionTitleWithAnchorLevel(4, "Section with anchor Level 4a")
                .sectionTitleLevel(5, "Section Level 5a")
                .sectionTitleWithAnchorLevel(5, "Section with anchor Level 5a", "level-5a")
                .sectionTitleWithAnchorLevel(5, "Section with anchor Level 5a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .listing("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN)", "java")
                .block("Example", MarkupBlockStyle.EXAMPLE)
                .block("Example", MarkupBlockStyle.EXAMPLE, "Example", null)
                .block("Example", MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.IMPORTANT)
                .block("Listing", MarkupBlockStyle.LISTING, null, MarkupAdmonition.CAUTION)
                .block("Literal", MarkupBlockStyle.LITERAL, null, MarkupAdmonition.NOTE)
                .block("Sidebar", MarkupBlockStyle.SIDEBAR, null, MarkupAdmonition.TIP)
                .block("Passthrough", MarkupBlockStyle.PASSTHROUGH, null, MarkupAdmonition.WARNING)
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
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        builder.writeToFileWithoutExtension(builder.addFileExtension(Paths.get("build/tmp/test")), StandardCharsets.UTF_8);
        builder.writeToFile(Paths.get("build/tmp/test"), StandardCharsets.UTF_8);
        builder.writeToFileWithoutExtension("build/tmp", builder.addFileExtension("test"), StandardCharsets.UTF_8);
        builder.writeToFile("build/tmp", "test", StandardCharsets.UTF_8);

        MarkupDocBuilder builderWithConfig = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN).withAnchorPrefix(" mdb test- ");
        String prefixMarkup = builderWithConfig.anchor("anchor", "text")
                .crossReference("anchor", "text")
                .toString();

        assertEquals("<a name=\"mdb-test-anchor\"></a>[text](#mdb-test-anchor)", prefixMarkup);
    }

    @Test
    public void testToAtlassianWikiFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP);

        builder = builder.documentTitle("Test title")
                .sectionTitleLevel(1, "Section Level 1a")
                .sectionTitleWithAnchorLevel(1, "Section with anchor Level 1a", "level-1a")
                .sectionTitleWithAnchorLevel(1, "Section with anchor Level 1a")
                .sectionTitleLevel(2, "Section Level 2a")
                .sectionTitleWithAnchorLevel(2, "Section with anchor Level 2a", "level-2a")
                .sectionTitleWithAnchorLevel(2, "Section with anchor Level 2a")
                .sectionTitleLevel(3, "Section Level 3a")
                .sectionTitleWithAnchorLevel(3, "Section with anchor Level 3a", "level-3a")
                .sectionTitleWithAnchorLevel(3, "Section with anchor Level 3a")
                .sectionTitleLevel(4, "Section Level 4a")
                .sectionTitleWithAnchorLevel(4, "Section with anchor Level 4a", "level-4a")
                .sectionTitleWithAnchorLevel(4, "Section with anchor Level 4a")
                .sectionTitleLevel(5, "Section Level 5a")
                .sectionTitleWithAnchorLevel(5, "Section with anchor Level 5a", "level-5a")
                .sectionTitleWithAnchorLevel(5, "Section with anchor Level 5a")
                .paragraph("Paragraph with long text bla bla bla bla bla")
                .listing("Source code listing")
                .listing("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP)", "java")
                .block("Example", MarkupBlockStyle.EXAMPLE)
                .block("Example", MarkupBlockStyle.EXAMPLE, "Example", null)
                .block("Example", MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.IMPORTANT)
                .block("Listing", MarkupBlockStyle.LISTING, null, MarkupAdmonition.CAUTION)
                .block("Literal", MarkupBlockStyle.LITERAL, null, MarkupAdmonition.NOTE)
                .block("Sidebar", MarkupBlockStyle.SIDEBAR, null, MarkupAdmonition.TIP)
                .block("Passthrough", MarkupBlockStyle.PASSTHROUGH, null, MarkupAdmonition.WARNING)
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
                .crossReferenceRaw("./document.txt", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.txt", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        builder.writeToFileWithoutExtension(builder.addFileExtension(Paths.get("build/tmp/test")), StandardCharsets.UTF_8);
        builder.writeToFile(Paths.get("build/tmp/test"), StandardCharsets.UTF_8);
        builder.writeToFileWithoutExtension("build/tmp", builder.addFileExtension("test"), StandardCharsets.UTF_8);
        builder.writeToFile("build/tmp", "test", StandardCharsets.UTF_8);
    }


    @Test
    public void shouldReplaceNewLinesWithSystemNewLine() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.paragraph("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla" + newLine + newLine, builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.text("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla", builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.textLine("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla" + newLine, builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.italicText("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("*Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla*", builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.italicTextLine("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("*Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla*" + newLine, builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.boldText("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("**Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla**", builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
        builder.boldTextLine("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("**Long text " + newLine + " bla bla " + newLine + " bla " + newLine + " bla**" + newLine, builder.toString());
    }

    @Test
    public void shouldReplaceTitleNewLinesWithWhiteSpace() throws IOException {
        String whitespace = " ";

        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.documentTitle("Long title \n bla bla \r bla \r\n bla");
        Assert.assertEquals("= Long title " + whitespace + " bla bla " + whitespace + " bla " + whitespace + " bla" + newLine + newLine, builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.sectionTitleLevel1("Long title \n bla bla \r bla \r\n bla");
        Assert.assertEquals(newLine + "== Long title " + whitespace + " bla bla " + whitespace + " bla " + whitespace + " bla" + newLine, builder.toString());

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.sectionTitleLevel2("Long title \n bla bla \r bla \r\n bla");
        Assert.assertEquals(newLine + "=== Long title " + whitespace + " bla bla " + whitespace + " bla " + whitespace + " bla" + newLine, builder.toString());
    }

    @Test
    public void shouldUseProvidedLineSeparator() throws IOException {
        String lineSeparator = LineSeparator.UNIX.toString();
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN, LineSeparator.UNIX);
        builder.paragraph("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("Long text " + lineSeparator + " bla bla " + lineSeparator + " bla " + lineSeparator + " bla" + lineSeparator + lineSeparator, builder.toString());
    }

}
