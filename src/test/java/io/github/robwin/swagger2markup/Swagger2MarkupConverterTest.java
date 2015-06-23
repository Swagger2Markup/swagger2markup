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
package io.github.robwin.swagger2markup;

import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.assertThat;

/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverterTest {

    @Test
    public void testSwagger2AsciiDocConversion() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.getAbsolutePath()).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(3).containsAll(asList("definitions.adoc", "overview.adoc", "paths.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithDescriptionsAndExamples() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.getAbsolutePath()).withDescriptions("src/docs/asciidoc")
                .withExamples("src/docs/asciidoc/paths").build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(3).containsAll(asList("definitions.adoc", "overview.adoc", "paths.adoc"));
    }

    @Test
    public void testSwagger2MarkdownConversion() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.getAbsolutePath()).
                withMarkupLanguage(MarkupLanguage.MARKDOWN).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(3).containsAll(asList("definitions.md", "overview.md", "paths.md"));
    }

    @Test
    public void testSwagger2MarkdownConversionWithDescriptions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.getAbsolutePath()).withDescriptions("src/docs/markdown").
                withMarkupLanguage(MarkupLanguage.MARKDOWN).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(3).containsAll(asList("definitions.md", "overview.md", "paths.md"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedDefinitions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.getAbsolutePath()).withSeparatedDefinitions().build()
            .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(8).containsAll(
            asList("definitions.adoc", "overview.adoc", "paths.adoc",
                "user.adoc", "category.adoc", "pet.adoc", "tag.adoc", "order.adoc"));
        assertThat(new String(Files.readAllBytes(Paths.get(outputDirectory + File.separator + "definitions.adoc"))))
            .contains(new String(Files.readAllBytes(Paths.get(outputDirectory + File.separator + "user.adoc"))));
    }

    @Test
    public void testSwagger2MarkdownConversionWithSeparatedDefinitions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.getAbsolutePath()).withSeparatedDefinitions().
                withMarkupLanguage(MarkupLanguage.MARKDOWN).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(8).containsAll(
                asList("definitions.md", "overview.md", "paths.md",
                        "user.md", "category.md", "pet.md", "tag.md", "order.md"));
        assertThat(new String(Files.readAllBytes(Paths.get(outputDirectory + File.separator + "definitions.md"))))
                .contains(new String(Files.readAllBytes(Paths.get(outputDirectory + File.separator + "user.md"))));
    }

    /*
    @Test
    public void testSwagger2HtmlConversion() throws IOException {
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        String asciiDoc =  Swagger2MarkupConverter.from(file.getAbsolutePath()).build().asString();
        String path = "build/docs/generated/asciidocAsString";
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
    */
}
