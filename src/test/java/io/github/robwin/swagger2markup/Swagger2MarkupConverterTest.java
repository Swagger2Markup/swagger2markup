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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.extension.Swagger2MarkupExtensionRegistry;
import io.github.robwin.swagger2markup.extension.repository.DynamicDefinitionsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOperationsContentExtension;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.BDDAssertions.assertThat;

public class Swagger2MarkupConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(Swagger2MarkupConverterTest.class);

    @Test
    public void testSwagger2AsciiDocConversionFromString() throws IOException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger.json"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(swaggerJsonString).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithExamples() throws IOException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withExamples(Paths.get("src/docs/asciidoc/paths"))
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("paths.adoc"))))
                .contains("==== Example HTTP response");
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc"))))
                .contains("|name||true|string||doggie");
    }

    @Test
    public void testSwagger2AsciiDocConversionAsString() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());

        //When
        String asciiDocAsString = Swagger2MarkupConverter.from(file).build()
                .asString();
        //Then
        assertThat(asciiDocAsString).isNotEmpty();
    }


    @Test
    public void testSwagger2AsciiDocConversion() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocWithInlineSchema() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withInlineSchemaDepthLevel(1)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTags() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());
        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTagsWithMissingTag() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger_missing_tag.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());
        //When
        try {
            Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                    .withPathsGroupedBy(GroupBy.TAGS)
                    .build();

            Swagger2MarkupConverter.from(file)
                    .withConfig(config)
                    .build()
                    .intoFolder(outputDirectory);
            // If NullPointerException was not thrown, test would fail the specified message
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception e) {
            assertThat(e).hasMessage("Can't GroupBy.TAGS > Operation 'updatePet' has not tags");
        }
    }

    @Test
    public void testOldSwaggerSpec2AsciiDocConversion() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/error_swagger_12.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithDescriptionsAndExamples() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions(Paths.get("src/docs/asciidoc/definitions"))
                .build();

        Swagger2MarkupConverter.from(file)
                .withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionDoesNotContainUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_should_not_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .doesNotContain("=== URI scheme");
    }

    @Test
    public void testSwagger2AsciiDocConversionContainsUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_should_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .contains("=== URI scheme");
    }

    @Test
    public void testSwagger2MarkdownConversion() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.md", "overview.md", "paths.md", "security.md"));
    }

    @Test
    public void testSwagger2MarkdownConversionWithDescriptions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions(Paths.get("src/docs/markdown/definitions"))
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.md", "overview.md", "paths.md", "security.md"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedDefinitions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(5).containsAll(
                asList("definitions", "definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path definitionsDirectory = outputDirectory.resolve("definitions");
        String[] definitions = definitionsDirectory.toFile().list();
        assertThat(definitions).hasSize(6).containsAll(
                asList("identified.adoc", "user.adoc", "category.adoc", "pet.adoc", "tag.adoc", "order.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedOperations() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedOperations()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(5).containsAll(
                asList("operations", "definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path pathsDirectory = outputDirectory.resolve("operations");
        String[] paths = pathsDirectory.toFile().list();
        assertThat(paths).hasSize(18);
    }

    @Test
    public void testSwagger2MarkdownConversionWithSeparatedDefinitions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(5).containsAll(
                asList("definitions", "definitions.md", "overview.md", "paths.md", "security.md"));

        Path definitionsDirectory = outputDirectory.resolve("definitions");
        String[] definitions = definitionsDirectory.toFile().list();
        assertThat(definitions).hasSize(6).containsAll(
                asList("identified.md", "user.md", "category.md", "pet.md", "tag.md", "order.md"));
    }

    @Test
    public void testSwagger2MarkdownConversionHandlesComposition() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        // Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(5).containsAll(
                asList("definitions", "definitions.md", "overview.md", "paths.md", "security.md"));
        Path definitionsDirectory = outputDirectory.resolve("definitions");
        verifyMarkdownContainsFieldsInTables(
                definitionsDirectory.resolve("user.md").toFile(),
                ImmutableMap.<String, Set<String>>builder()
                        .put("User", ImmutableSet.of("id", "username", "firstName",
                                "lastName", "email", "password", "phone", "userStatus"))
                        .build()
        );

    }

    @Test
    public void testSwagger2AsciiDocConversionWithRussianOutputLanguage() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withOutputLanguage(Language.RU)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")), Charset.forName("UTF-8")))
                .contains("== Определения");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithFrenchOutputLanguage() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withOutputLanguage(Language.FR)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")), Charset.forName("UTF-8")))
                .contains("== D\u0233finitions");
    }

    @Test
    public void testSwagger2AsciiDocExtensions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new DynamicDefinitionsContentExtension(Paths.get("src/docs/asciidoc/extensions")))
                .withExtension(new DynamicOperationsContentExtension(Paths.get("src/docs/asciidoc/extensions")))
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("paths.adoc")))).contains(
                "Pet update request extension");
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")))).contains(
                "Pet extension");

    }

    @Test
    public void testSwagger2MarkdownExtensions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        Path outputDirectory = Paths.get("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new DynamicDefinitionsContentExtension(Paths.get("src/docs/markdown/extensions")))
                .withExtension(new DynamicOperationsContentExtension(Paths.get("src/docs/markdown/extensions")))
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("paths.md")))).contains(
                "Pet update request extension");
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.md")))).contains(
                "Pet extension");

    }

    @Test
    public void testSwagger2MarkupConfigDefaultPaths() throws URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .withExamples()
                .withOperationDescriptions()
                .withSchemas()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build();

        //Then
        URI baseUri = io.github.robwin.swagger2markup.utils.IOUtils.uriParent(converterBuilder.globalContext.swaggerLocation);
        assertThat(converterBuilder.globalContext.config.getDefinitionDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getExamplesUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getOperationDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getSchemasUri()).isEqualTo(baseUri);
    }

    @Test
    public void testSwagger2MarkupConfigDefaultPathsWithUri() throws MalformedURLException {
        //Given

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .withExamples()
                .withOperationDescriptions()
                .withSchemas()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(URI.create("http://petstore.swagger.io/v2/swagger.json").toURL())
                .withConfig(config)
                .build();

        //Then
        assertThat(converterBuilder.globalContext.config.getDefinitionDescriptionsUri()).isNull();
        assertThat(converterBuilder.globalContext.config.getExamplesUri()).isNull();
        assertThat(converterBuilder.globalContext.config.getOperationDescriptionsUri()).isNull();
        assertThat(converterBuilder.globalContext.config.getSchemasUri()).isNull();
    }

    @Test
    public void testSwagger2MarkupConfigDefaultPathsWithoutFile() {
        //Given
        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .build();

        //Then
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(new Swagger())
                .withConfig(config)
                .build();
        assertThat(converter.globalContext.config.isDefinitionDescriptions()).isFalse();
    }

    /**
     * Given a markdown document to search, this checks to see if the specified tables
     * have all of the expected fields listed.
     *
     * @param doc           markdown document file to inspect
     * @param fieldsByTable map of table name (header) to field names expected
     *                      to be found in that table.
     * @throws IOException if the markdown document could not be read
     */
    private static void verifyMarkdownContainsFieldsInTables(File doc, Map<String, Set<String>> fieldsByTable) throws IOException {
        final List<String> lines = Files.readAllLines(doc.toPath(), Charset.defaultCharset());
        final Map<String, Set<String>> fieldsLeftByTable = Maps.newHashMap();
        for (Map.Entry<String, Set<String>> entry : fieldsByTable.entrySet()) {
            fieldsLeftByTable.put(entry.getKey(), Sets.newHashSet(entry.getValue()));
        }
        String inTable = null;
        for (String line : lines) {
            // If we've found every field we care about, quit early
            if (fieldsLeftByTable.isEmpty()) {
                return;
            }

            // Transition to a new table if we encounter a header
            final String currentHeader = getTableHeader(line);
            if (inTable == null || currentHeader != null) {
                inTable = currentHeader;
            }

            // If we're in a table that we care about, inspect this potential table row
            if (inTable != null && fieldsLeftByTable.containsKey(inTable)) {
                // If we're still in a table, read the row and check for the field name
                //  NOTE: If there was at least one pipe, then there's at least 2 fields
                String[] parts = line.split("\\|");
                if (parts.length > 1) {
                    final String fieldName = parts[1];
                    final Set<String> fieldsLeft = fieldsLeftByTable.get(inTable);
                    // Mark the field as found and if this table has no more fields to find,
                    //  remove it from the "fieldsLeftByTable" map to mark the table as done
                    if (fieldsLeft.remove(fieldName) && fieldsLeft.isEmpty()) {
                        fieldsLeftByTable.remove(inTable);
                    }
                }
            }
        }

        // After reading the file, if there were still types, fail
        if (!fieldsLeftByTable.isEmpty()) {
            fail(String.format("Markdown file '%s' did not contain expected fields (by table): %s",
                    doc, fieldsLeftByTable));
        }
    }

    private static String getTableHeader(String line) {
        return line.startsWith("###")
                ? line.replace("###", "").trim()
                : null;
    }

    /*
    @Test
    public void testSwagger2HtmlConversion() throws IOException {
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").toURI());
        String asciiDoc =  Swagger2MarkupConverter.from(file).build().asString();
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
