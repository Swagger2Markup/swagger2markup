package io.github.robwin.swagger2markup;

import io.github.robwin.markup.builder.MarkupLanguage;
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

        Swagger2MarkupConverter.from(file.getAbsolutePath()).
                withMarkupLanguage(MarkupLanguage.MARKDOWN).
                withExamples("docs").withSchemas("docs/schemas").build()
                .intoFolder("src/docs/markdown");

        Swagger2MarkupConverter.from(file.getAbsolutePath()).
                withExamples("docs").withSchemas("docs/schemas").build()
                .intoFolder("src/docs/asciidoc");

    }
}
