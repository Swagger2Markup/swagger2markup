package io.github.robwin.swagger2markup.builder;

import io.github.robwin.swagger2markup.builder.markup.DocumentBuilder;
import io.github.robwin.swagger2markup.builder.markup.DocumentBuilders;
import io.github.robwin.swagger2markup.builder.markup.MarkupLanguage;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Project:   swagger2markup
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
public class DocumentBuilderTest {

    @Test
    public void testToFile() throws IOException {
        DocumentBuilder builder = DocumentBuilders.documentBuilder(MarkupLanguage.ASCIIDOC);
        builder.documentTitle("Test title").textLine("Text line").writeToFile("/tmp", "test.adoc", StandardCharsets.UTF_8);
    }

}
