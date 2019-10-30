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
package io.github.swagger2markup.markup.builder;

import io.github.swagger2markup.markup.builder.assertions.DiffUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;


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
                new MarkupTableColumn().withHeader("Header3").withWidthRatio(1).withHeaderColumn(true));
        tableCells = new ArrayList<>();
        tableCells.add(Arrays.asList("Row 1 | Column 1", "Row 1 | Column 2", "Row 1 | Column 3"));
        tableCells.add(Arrays.asList("Row 2 | Column 1", "Row 2 | Column 2", "Row 2 | Column 3"));
    }


    @Test
    public void testAsciiDoc() throws IOException, URISyntaxException {
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
                .paragraph("\rLine1\nLine2\r\n", false)
                .paragraph("\rLine1\nLine2\r\n", true)
                .listingBlock("Source code listing")
                .listingBlock("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN)", "java")
                .block("Example", MarkupBlockStyle.EXAMPLE)
                .block("Example", MarkupBlockStyle.EXAMPLE, "Example", null)
                .block("Example", MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.IMPORTANT)
                .block("Listing", MarkupBlockStyle.LISTING, null, MarkupAdmonition.CAUTION)
                .block("Literal", MarkupBlockStyle.LITERAL, null, MarkupAdmonition.NOTE)
                .block("Sidebar", MarkupBlockStyle.SIDEBAR, null, MarkupAdmonition.TIP)
                .block("Passthrough", MarkupBlockStyle.PASSTHROUGH, null, MarkupAdmonition.WARNING)
                .pageBreak()
                .table(tableCells)
                .tableWithColumnSpecs(tableColumns, tableCells)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .textLine("text line", true)
                .literalTextLine("Literal text line", true)
                .boldTextLine("Bold text line", true)
                .italicTextLine("Italic text line", true)
                .boldText("bold").italicText("italic").text("regular").newLine(true)
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .anchor("anchor", "text").newLine()
                .anchor(" Simple    anchor").newLine()
                .anchor("  \u0240 µ&|ù This .:/-_#  ").newLine()
                .crossReferenceRaw("./document.adoc", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.adoc", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        Path outputFile = Paths.get("build/test/asciidoc/test");

        builder.writeToFileWithoutExtension(builder.addFileExtension(outputFile), StandardCharsets.UTF_8);
        builder.writeToFile(outputFile, StandardCharsets.UTF_8);

        Path expectedFile = Paths.get(MarkupDocBuilderTest.class.getResource("/expected/asciidoc/test.adoc").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, builder.addFileExtension(outputFile), "testAsciiDoc.html");
    }

    @Test
    public void testAsciiDocWithAnchorPrefix() {
        MarkupDocBuilder builderWithConfig = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC).withAnchorPrefix(" mdb test- ");
        String prefixMarkup = builderWithConfig.anchor("anchor", "text")
                .crossReference("anchor", "text")
                .toString();

        assertEquals("[[_mdb_test-anchor,text]]<<_mdb_test-anchor,text>>", prefixMarkup);
    }

    @Test
    public void testMarkdownCodeBlock() throws IOException, URISyntaxException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);

        builder = builder.listingBlock("$o = new Thing();", "php");
        Path outputFile = Paths.get("build/test/markdown/test2");
        builder.writeToFileWithoutExtension(builder.addFileExtension(outputFile), StandardCharsets.UTF_8);
        builder.writeToFile(outputFile, StandardCharsets.UTF_8);

        Path expectedFile = Paths.get(MarkupDocBuilderTest.class.getResource("/expected/markdown/test2.md").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, builder.addFileExtension(outputFile), "testMarkdown2.html");
    }

    @Test
    public void testMarkdown() throws IOException, URISyntaxException {
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
                .paragraph("\rLine1\nLine2\r\n", false)
                .paragraph("\rLine1\nLine2\r\n", true)
                .listingBlock("Source code listing")
                .listingBlock("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN)", "java")
                .block("Example", MarkupBlockStyle.EXAMPLE)
                .block("Example", MarkupBlockStyle.EXAMPLE, "Example", null)
                .block("Example", MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.IMPORTANT)
                .block("Listing", MarkupBlockStyle.LISTING, null, MarkupAdmonition.CAUTION)
                .block("Literal", MarkupBlockStyle.LITERAL, null, MarkupAdmonition.NOTE)
                .block("Sidebar", MarkupBlockStyle.SIDEBAR, null, MarkupAdmonition.TIP)
                .block("Passthrough", MarkupBlockStyle.PASSTHROUGH, null, MarkupAdmonition.WARNING)
                .pageBreak()
                //.table(tableCells)
                .tableWithColumnSpecs(tableColumns, tableCells)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .textLine("text line", true)
                .literalTextLine("Literal text line", true)
                .boldTextLine("Bold text line", true)
                .italicTextLine("Italic text line", true)
                .boldText("bold").italicText("italic").text("regular").newLine(true)
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .anchor("anchor", "text").newLine()
                .anchor(" Simple    anchor").newLine()
                .anchor("  \u0240 µ&|ù This .:/-_#  ").newLine()
                .crossReferenceRaw("./document.md", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.md", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        Path outputFile = Paths.get("build/test/markdown/test");

        builder.writeToFileWithoutExtension(builder.addFileExtension(outputFile), StandardCharsets.UTF_8);
        builder.writeToFile(outputFile, StandardCharsets.UTF_8);

        Path expectedFile = Paths.get(MarkupDocBuilderTest.class.getResource("/expected/markdown/test.md").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, builder.addFileExtension(outputFile), "testMarkdown.html");

    }

    @Test
    public void testMarkdownWithAnchorPrefix() {
        MarkupDocBuilder builderWithConfig = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN).withAnchorPrefix(" mdb test- ");
        String prefixMarkup = builderWithConfig.anchor("anchor", "text")
                .crossReference("anchor", "text")
                .toString();

        assertEquals("<a name=\"mdb-test-anchor\"></a>[text](#mdb-test-anchor)", prefixMarkup);
    }

    @Test
    public void testConfluenceMarkup() throws IOException, URISyntaxException {
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
                .paragraph("\rLine1\nLine2\r\n", false)
                .paragraph("\rLine1\nLine2\r\n", true)
                .listingBlock("Source code listing")
                .listingBlock("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP)", "java")
                .block("Example", MarkupBlockStyle.EXAMPLE)
                .block("Example", MarkupBlockStyle.EXAMPLE, "Example", null)
                .block("Example", MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.IMPORTANT)
                .block("Listing", MarkupBlockStyle.LISTING, null, MarkupAdmonition.CAUTION)
                .block("Literal", MarkupBlockStyle.LITERAL, null, MarkupAdmonition.NOTE)
                .block("Sidebar", MarkupBlockStyle.SIDEBAR, null, MarkupAdmonition.TIP)
                .block("Passthrough", MarkupBlockStyle.PASSTHROUGH, null, MarkupAdmonition.WARNING)
                .pageBreak()
                .table(tableCells)
                .tableWithColumnSpecs(tableColumns, tableCells)
                .sectionTitleLevel1("Section Level 1b")
                .sectionTitleLevel2("Section Level 2b")
                .textLine("text line", true)
                .literalTextLine("Literal text line", true)
                .boldTextLine("Bold text line", true)
                .italicTextLine("Italic text line", true)
                .boldText("bold").italicText("italic").text("regular").newLine(true)
                .unorderedList(Arrays.asList("Entry1", "Entry2", "Entry 2"))
                .anchor("anchor", "text").newLine()
                .anchor(" Simple    anchor").newLine()
                .anchor("  \u0240 µ&|ù This .:/-_#  ").newLine()
                .crossReferenceRaw("./document.txt", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.txt", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        Path outputFile = Paths.get("build/test/confluenceMarkup/test");

        builder.writeToFileWithoutExtension(builder.addFileExtension(outputFile), StandardCharsets.UTF_8);
        builder.writeToFile(outputFile, StandardCharsets.UTF_8);

        Path expectedFile = Paths.get(MarkupDocBuilderTest.class.getResource("/expected/confluenceMarkup/test.txt").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, builder.addFileExtension(outputFile), "testConfluenceMarkup.html");
    }

    @Test
    public void testConfluenceMarkupWithAnchorPrefix() {
        MarkupDocBuilder builderWithConfig = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP).withAnchorPrefix(" mdb test- ");
        String prefixMarkup = builderWithConfig.anchor("anchor", "text")
                .crossReference("anchor", "text")
                .toString();

        assertEquals("{anchor:mdb_test-anchor}[text|#mdb_test-anchor]", prefixMarkup);
    }

    @Test
    public void shouldReplaceNewLinesWithSystemNewLine() {
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
    public void shouldReplaceTitleNewLinesWithWhiteSpace() {
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
    public void shouldUseProvidedLineSeparator() {
        String lineSeparator = LineSeparator.UNIX.toString();
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN, LineSeparator.UNIX);
        builder.paragraph("Long text \n bla bla \r bla \r\n bla");
        Assert.assertEquals("Long text " + lineSeparator + " bla bla " + lineSeparator + " bla " + lineSeparator + " bla" + lineSeparator + lineSeparator, builder.toString());
    }

    private void assertImportMarkup(String expected, String text, MarkupLanguage markupLanguage, int levelOffset) {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(markupLanguage, LineSeparator.UNIX);

        builder.importMarkup(new StringReader(text), markupLanguage, levelOffset);

        Assert.assertEquals(expected, builder.toString());
    }

    private void assertImportMarkupException(String expected, String text, MarkupLanguage markupLanguage, int levelOffset) {
        try {
            assertImportMarkup(expected, text, markupLanguage, levelOffset);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testImportMarkupAsciiDoc() {
        assertImportMarkup("", "", MarkupLanguage.ASCIIDOC, 0);
        assertImportMarkup("", "", MarkupLanguage.ASCIIDOC, 4);
        assertImportMarkupException("Specified levelOffset (6) > max levelOffset (5)", "", MarkupLanguage.ASCIIDOC, 6);
        assertImportMarkup("", "", MarkupLanguage.ASCIIDOC, -4);
        assertImportMarkupException("Specified levelOffset (-6) < min levelOffset (-5)", "", MarkupLanguage.ASCIIDOC, -6);

        assertImportMarkup("\n= title\nline 1\nline 2\n\n", "=   title\r\nline 1\r\nline 2", MarkupLanguage.ASCIIDOC, 0);

        assertImportMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", MarkupLanguage.ASCIIDOC, 0);
        assertImportMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", MarkupLanguage.ASCIIDOC, 4);

        assertImportMarkup("\n= title\nline 1\nline 2\n= title 2\nline 3\n\n", "= title\nline 1\nline 2\n= title 2\nline 3", MarkupLanguage.ASCIIDOC, 0);
        assertImportMarkup("\n===== title\nline 1\nline 2\n\n", "= title\nline 1\nline 2", MarkupLanguage.ASCIIDOC, 4);
        assertImportMarkup("\n= title\nline 1\nline 2\n\n", "===== title\nline 1\nline 2", MarkupLanguage.ASCIIDOC, -4);

        assertImportMarkupException("Specified levelOffset (5) set title 'title' level (1) > max title level (5)", "== title\nline 1\nline 2", MarkupLanguage.ASCIIDOC, 5);
        assertImportMarkupException("Specified levelOffset (-1) set title 'title' level (0) < 0", "= title\nline 1\nline 2", MarkupLanguage.ASCIIDOC, -1);
        assertImportMarkupException("Specified levelOffset (-3) set title 'title' level (1) < 0", "== title\nline 1\nline 2", MarkupLanguage.ASCIIDOC, -3);
    }

    @Test
    public void testImportMarkupMarkdown() {
        assertImportMarkup("", "", MarkupLanguage.MARKDOWN, 0);
        assertImportMarkup("", "", MarkupLanguage.MARKDOWN, 4);
        assertImportMarkup("", "", MarkupLanguage.MARKDOWN, -4);
        assertImportMarkupException("Specified levelOffset (6) > max levelOffset (5)", "", MarkupLanguage.MARKDOWN, 6);
        assertImportMarkupException("Specified levelOffset (-6) < min levelOffset (-5)", "", MarkupLanguage.MARKDOWN, -6);

        assertImportMarkup("\n# title\nline 1\nline 2\n\n", "#   title\r\nline 1\r\nline 2", MarkupLanguage.MARKDOWN, 0);

        assertImportMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", MarkupLanguage.MARKDOWN, 0);
        assertImportMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", MarkupLanguage.MARKDOWN, 4);

        assertImportMarkup("\n# title\nline 1\nline 2\n# title 2\nline 3\n\n", "# title\nline 1\nline 2\n# title 2\nline 3", MarkupLanguage.MARKDOWN, 0);
        assertImportMarkup("\n##### title\nline 1\nline 2\n\n", "# title\nline 1\nline 2", MarkupLanguage.MARKDOWN, 4);
        assertImportMarkup("\n# title\nline 1\nline 2\n\n", "##### title\nline 1\nline 2", MarkupLanguage.MARKDOWN, -4);

        assertImportMarkupException("Specified levelOffset (5) set title 'title' level (1) > max title level (5)", "## title\nline 1\nline 2", MarkupLanguage.MARKDOWN, 5);
        assertImportMarkupException("Specified levelOffset (-1) set title 'title' level (0) < 0", "# title\nline 1\nline 2", MarkupLanguage.MARKDOWN, -1);
        assertImportMarkupException("Specified levelOffset (-3) set title 'title' level (1) < 0", "## title\nline 1\nline 2", MarkupLanguage.MARKDOWN, -3);
    }

    @Test
    public void testImportMarkupConfluenceMarkup() {
        assertImportMarkup("", "", MarkupLanguage.CONFLUENCE_MARKUP, 0);
        assertImportMarkup("", "", MarkupLanguage.CONFLUENCE_MARKUP, 4);
        assertImportMarkup("", "", MarkupLanguage.CONFLUENCE_MARKUP, -4);
        assertImportMarkupException("Specified levelOffset (6) > max levelOffset (5)", "", MarkupLanguage.CONFLUENCE_MARKUP, 6);
        assertImportMarkupException("Specified levelOffset (-6) < min levelOffset (-5)", "", MarkupLanguage.CONFLUENCE_MARKUP, -6);

        assertImportMarkup("\nh1. title\nline 1\nline 2\n\n", "h1.   title\r\nline 1\r\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, 0);

        assertImportMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, 0);
        assertImportMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, 4);

        assertImportMarkup("\nh1. title\nline 1\nline 2\nh1. title 2\nline 3\n\n", "h1. title\nline 1\nline 2\nh1. title 2\nline 3", MarkupLanguage.CONFLUENCE_MARKUP, 0);
        assertImportMarkup("\nh5. title\nline 1\nline 2\n\n", "h1. title\nline 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, 4);
        assertImportMarkup("\nh1. title\nline 1\nline 2\n\n", "h5. title\nline 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, -4);

        assertImportMarkupException("Specified levelOffset (5) set title 'title' level (1) > max title level (5)", "h2. title\nline 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, 5);
        assertImportMarkupException("Specified levelOffset (-1) set title 'title' level (0) < 0", "h1. title\nline 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, -1);
        assertImportMarkupException("Specified levelOffset (-3) set title 'title' level (1) < 0", "h2. title\nline 1\nline 2", MarkupLanguage.CONFLUENCE_MARKUP, -3);
    }

    @Test
    public void importMarkupConversion() {
        // ASCIIDOC -> ASCIIDOC
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("= Title"), MarkupLanguage.ASCIIDOC);
        Assert.assertEquals("\n= Title\n\n", builder.toString());

        // ASCIIDOC -> MARKDOWN
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("= Title"), MarkupLanguage.ASCIIDOC);
        // Assert.assertEquals("\n# Title\n\n", builder.toString()); // Unsupported
        Assert.assertEquals("\n= Title\n\n", builder.toString());

        // ASCIIDOC -> CONFLUENCE_MARKUP
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("= Title"), MarkupLanguage.ASCIIDOC);
        // Assert.assertEquals("\nh1. Title\n\n", builder.toString()); // Unsupported
        Assert.assertEquals("\n= Title\n\n", builder.toString());

        // MARKDOWN -> ASCIIDOC
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("# Title"), MarkupLanguage.MARKDOWN);
        Assert.assertEquals("\n= Title\n\n", builder.toString());

        // MARKDOWN -> MARKDOWN
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("# Title"), MarkupLanguage.MARKDOWN);
        Assert.assertEquals("\n# Title\n\n", builder.toString());

        // MARKDOWN -> CONFLUENCE_MARKUP
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("# Title"), MarkupLanguage.MARKDOWN);
        // Assert.assertEquals("\nh1. Title\n\n", builder.toString()); // Unsupported
        Assert.assertEquals("\n# Title\n\n", builder.toString());

        // CONFLUENCE_MARKUP -> ASCIIDOC
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("h1. Title"), MarkupLanguage.CONFLUENCE_MARKUP);
        // Assert.assertEquals("\n= Title\n\n", builder.toString()); // Unsupported
        Assert.assertEquals("\nh1. Title\n\n", builder.toString());

        // CONFLUENCE_MARKUP -> MARKDOWN
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("h1. Title"), MarkupLanguage.CONFLUENCE_MARKUP);
        // Assert.assertEquals("\n# Title\n\n", builder.toString()); // Unsupported
        Assert.assertEquals("\nh1. Title\n\n", builder.toString());

        // CONFLUENCE_MARKUP -> CONFLUENCE_MARKUP
        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP, LineSeparator.UNIX);
        builder.importMarkup(new StringReader("h1. Title"), MarkupLanguage.CONFLUENCE_MARKUP);
        Assert.assertEquals("\nh1. Title\n\n", builder.toString());
    }

    @Test
    public void tableFormatAsciiDoc() throws URISyntaxException, IOException {
        Path outputFile = Paths.get("build/test/asciidoc/tableFormat.adoc");
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);

        List<MarkupTableColumn> cols = Arrays.asList(
                new MarkupTableColumn().withHeader("Header1\nfirst one"),
                new MarkupTableColumn().withWidthRatio(2),
                new MarkupTableColumn().withHeader("Header3").withWidthRatio(1).withHeaderColumn(true));
        List<List<String>> cells = new ArrayList<>();

        cells.add(Arrays.asList("\nRow 2 \\| Column \r\n1\r", "Row 2 || Column 2", "Row 2 | | Column 3"));


        builder = builder.tableWithColumnSpecs(cols, cells);
        builder.writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8);
        
        DiffUtils.assertThatFileIsEqual(Paths.get(MarkupDocBuilderTest.class.getResource("/expected/asciidoc/tableFormat.adoc").toURI()), outputFile, "tableFormatAsciiDoc.html");
    }

    @Test
    public void tableFormatMarkdown() throws URISyntaxException, IOException {
        Path outputFile = Paths.get("build/test/markdown/tableFormat.md");
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);

        List<MarkupTableColumn> cols = Arrays.asList(
                new MarkupTableColumn().withHeader("Header1\nfirst one"),
                new MarkupTableColumn().withWidthRatio(2),
                new MarkupTableColumn().withHeader("Header3").withWidthRatio(1).withHeaderColumn(true));
        List<List<String>> cells = new ArrayList<>();

        cells.add(Arrays.asList("\nRow 2 \\| Column \r\n1\r", "Row 2 || Column 2", "Row 2 | | Column 3"));

        builder = builder.tableWithColumnSpecs(cols, cells);
        builder.writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8);

        DiffUtils.assertThatFileIsEqual(Paths.get(MarkupDocBuilderTest.class.getResource("/expected/markdown/tableFormat.md").toURI()), outputFile, "tableFormatMarkdown.html");
    }

    @Test
    public void tableFormatConfluenceMarkup() throws URISyntaxException, IOException {
        Path outputFile = Paths.get("build/test/confluenceMarkup/tableFormat.txt");
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.CONFLUENCE_MARKUP);

        List<MarkupTableColumn> cols = Arrays.asList(
                new MarkupTableColumn().withHeader("Header1\nfirst one"),
                new MarkupTableColumn().withWidthRatio(2),
                new MarkupTableColumn().withHeader("Header3").withWidthRatio(1).withHeaderColumn(true));
        List<List<String>> cells = new ArrayList<>();

        cells.add(Arrays.asList("Row 1 [Title|Page#Anchor] | Column 1", "Row 1 [Title1|Page#Anchor][Title2|Page#Anchor] [Title3|Page#Anchor] | Column [Title|Page#Anchor] 2", "Row 1 [Ti\\|t\\]\\[le|Page#Anchor] | Column 3"));
        cells.add(Arrays.asList("[Title|Page#Anchor]Row 1 | Column 1[Title|Page#Anchor]", "|[Title1|Page#Anchor]Row1 Column2|[Title1|Page#Anchor]", "|Row 1 Column 3|"));
        cells.add(Arrays.asList("\nRow 2 \\| Column \r\n1\r", "Row 2 || Column 2", "Row 2 | | Column 3"));

        builder = builder.tableWithColumnSpecs(cols, cells);
        builder.writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8);

        DiffUtils.assertThatFileIsEqual(Paths.get(MarkupDocBuilderTest.class.getResource("/expected/confluenceMarkup/tableFormat.txt").toURI()), outputFile, "tableFormatConfluenceMarkup.html");
    }
    
}
