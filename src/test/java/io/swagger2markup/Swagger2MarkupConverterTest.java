package io.swagger2markup;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverterTest {

    @Test
    public void testSwagger2AsciiDocConverter() throws IOException {
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        Swagger2MarkupConverter.from(file.getAbsolutePath()).toAsciiDoc("src/docs/asciidoc/swagger.adoc");

        Swagger2MarkupConverter.from(file.getAbsolutePath()).toMarkdown("src/docs/markdown/swagger.md");
    }
}
