package io.github.robwin.swagger2markup.builder;

import io.github.robwin.swagger2markup.builder.markup.MarkupDocBuilder;
import io.github.robwin.swagger2markup.builder.markup.MarkupDocBuilders;
import io.github.robwin.swagger2markup.builder.markup.MarkupLanguage;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Robert Winkler
 */
public class MarkupDocBuilderTest {

    @Test
    public void testToFile() throws IOException {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.documentTitle("Test title").textLine("Text line").writeToFile("/tmp", "test.adoc", StandardCharsets.UTF_8);
    }

}
