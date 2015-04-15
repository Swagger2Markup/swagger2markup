package io.github.robwin.swagger2markup;

import io.github.robwin.markup.builder.MarkupLanguage;
import org.asciidoctor.*;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverterTest {

    @Test
    public void testSwagger2MarkupConversion() throws IOException {
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());

        Swagger2MarkupConverter.from(file.getAbsolutePath()).
                withMarkupLanguage(MarkupLanguage.MARKDOWN).
                withExamples("docs").withSchemas("docs/schemas").build()
                .intoFolder("src/docs/markdown");

        Swagger2MarkupConverter.from(file.getAbsolutePath()).
                withExamples("docs").withSchemas("docs/schemas").build()
                .intoFolder("src/docs/asciidoc");

    }

    @Test
    public void testSwagger2HtmlConversion() throws IOException {
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        String asciiDoc =  Swagger2MarkupConverter.from(file.getAbsolutePath()).build().asString();
        String path = "src/docs/asciidocAsString";
        Files.createDirectories(Paths.get(path));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, "swagger.adoc"), StandardCharsets.UTF_8)){
            writer.write(asciiDoc);        }
        String asciiDocAsHtml = Asciidoctor.Factory.create().convert(asciiDoc,
                OptionsBuilder.options().backend("html5").headerFooter(true).safe(SafeMode.UNSAFE).docType("book").attributes(AttributesBuilder.attributes()
                        .tableOfContents(true).tableOfContents(Placement.LEFT).sectionNumbers(true).hardbreaks(true).setAnchors(true).attribute("sectlinks")));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, "swagger.html"), StandardCharsets.UTF_8)){
            writer.write(asciiDocAsHtml);
        }
    }
/*
    @Test
    public void testSwagger2PdfConversion() throws IOException {
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/spica.json").getFile());
        String asciiDoc =  Swagger2MarkupConverter.from(file.getAbsolutePath()).build().asString();
        String path = "src/docs/asciidocAsString";
        Files.createDirectories(Paths.get(path));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, "spica.adoc"), StandardCharsets.UTF_8)){
            writer.write(asciiDoc);
        }
        String asciiDocAsPdf= Asciidoctor.Factory.create().convert(asciiDoc, OptionsBuilder.options().backend("pdf"));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, "spica.pdf"), StandardCharsets.UTF_8)){
            writer.write(asciiDocAsPdf);
        }
    }
*/

}
