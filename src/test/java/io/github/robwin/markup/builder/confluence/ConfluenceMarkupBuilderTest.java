package io.github.robwin.markup.builder.confluence;

import io.github.robwin.markup.builder.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfluenceMarkupBuilderTest {

    private final List<MarkupTableColumn> tableColumns;
    private final List<List<String>> tableCells;

    public ConfluenceMarkupBuilderTest() {

        tableColumns = Arrays.asList(
                new MarkupTableColumn().withHeader("Header1"),
                new MarkupTableColumn().withWidthRatio(2),
                new MarkupTableColumn().withHeader("Header3").withWidthRatio(1));
        tableCells = new ArrayList<>();
        tableCells.add(Arrays.asList("Row 1 | Column 1", "Row 1 | Column 2", "Row 1 | Column 3"));
        tableCells.add(Arrays.asList("Row 2 | Column 1", "Row 2 | Column 2", "Row 2 | Column 3"));
    }

    @Test
    public void testToConfluenceWikiFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(ConfluenceMarkupBuilder.CONFLUENCE);

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
                .listing("MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(ConfluenceMarkupBuilder.CONFLUENCE)", "java")
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
                .crossReferenceRaw("./document.md", "anchor", "text").newLine(true)
                .crossReferenceRaw("  \u0240 µ&|ù This .:/-_  ").newLine(true)
                .crossReference("./document.md", "anchor", "text").newLine(true)
                .crossReference("  \u0240 µ&|ù This .:/-_  ").newLine(true);

        builder.writeToFileWithoutExtension(builder.addFileExtension(Paths.get("build/tmp/test")), StandardCharsets.UTF_8);
        builder.writeToFile(Paths.get("build/tmp/test"), StandardCharsets.UTF_8);
        builder.writeToFileWithoutExtension("build/tmp", builder.addFileExtension("test"), StandardCharsets.UTF_8);
        builder.writeToFile("build/tmp", "test", StandardCharsets.UTF_8);
    }

}