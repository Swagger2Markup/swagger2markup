/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.robwin.swagger2markup;

import io.github.robwin.swagger2markup.assertions.DiffUtils;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.extension.Swagger2MarkupExtensionRegistry;
import io.github.robwin.swagger2markup.extension.repository.DynamicDefinitionsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOperationsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.SpringRestDocsExtension;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.BDDAssertions.assertThat;

public class AsciidocConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(AsciidocConverterTest.class);

    @Test
    public void testSwagger2AsciiDocConversionAsString() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

        //When
        String asciiDocAsString = Swagger2MarkupConverter.from(file).build()
                .asString();
        //Then
        assertThat(asciiDocAsString).isNotEmpty();
    }


    @Test
    public void testSwagger2AsciiDocConversion() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/default").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversion.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionFromString() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(swaggerJsonString).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/default").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversion.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSpringRestDocsExtension() throws IOException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new SpringRestDocsExtension(Paths.get("src/docs/asciidoc/paths").toUri()).withDefaultSnippets())
                .build();

        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("paths.adoc"))))
                .contains("==== HTTP request", "==== HTTP response", "==== Curl request", "===== curl-request");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithExamples() throws IOException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
        String orderExample = "----\n" +
                "{\n" +
                "  \"id\" : 99,\n" +
                "  \"petId\" : 122,\n" +
                "  \"quantity\" : 2,\n" +
                "  \"shipDate\" : \"2016-02-22T23:02:05Z\",\n" +
                "  \"status\" : \"PENDING\",\n" +
                "  \"complete\" : true\n" +
                "}\n" +
                "----\n";
        String petResponseExample = "----\n" +
                "{\n" +
                "  \"application/json\" : {\n" +
                "    \"name\" : \"Puma\",\n" +
                "    \"type\" : 22,\n" +
                "    \"color\" : \"Black\",\n" +
                "    \"gender\" : \"Female\",\n" +
                "    \"breed\" : \"Mixed\"\n" +
                "  }\n" +
                "}\n" +
                "----\n";

        String pathsDocument = new String(Files.readAllBytes(outputDirectory.resolve("paths.adoc")));
        assertThat(pathsDocument)
                .contains("==== Response 405\n" + petResponseExample);
        assertThat(pathsDocument)
                .contains("==== Request body\n" + orderExample);
        assertThat(pathsDocument)
                .contains("==== Response 200\n" + orderExample);

        String definitionsDocument = new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")));
        assertThat(definitionsDocument)
                .contains("|name||true|string||\"doggie\"");
        assertThat(definitionsDocument)
                .contains("|id||false|integer(int64)||77");
        assertThat(definitionsDocument).contains("|pictures||false|string(byte) array||");
        assertThat(definitionsDocument).contains("|shipDate||false|string(date-time)||");
        assertThat(definitionsDocument)
                .doesNotContain("99");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithGeneratedExamples() throws IOException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        String petGeneratedExample = "----\n" +
                "{\n" +
                "  \"tags\" : [ {\n" +
                "    \"id\" : 0,\n" +
                "    \"name\" : \"string\"\n" +
                "  } ],\n" +
                "  \"id\" : 0,\n" +
                "  \"nicknames\" : {\n" +
                "    \"string\" : \"string\"\n" +
                "  },\n" +
                "  \"category\" : {\n" +
                "    \"id\" : 123,\n" +
                "    \"name\" : \"Canines\"\n" +
                "  },\n" +
                "  \"weight\" : 0.0,\n" +
                "  \"status\" : \"string\",\n" +
                "  \"name\" : \"doggie\",\n" +
                "  \"photoUrls\" : [ \"string\" ]\n" +
                "}\n" +
                "----\n";
        String petResponseExample = "----\n" +
                "{\n" +
                "  \"application/json\" : {\n" +
                "    \"name\" : \"Puma\",\n" +
                "    \"type\" : 22,\n" +
                "    \"color\" : \"Black\",\n" +
                "    \"gender\" : \"Female\",\n" +
                "    \"breed\" : \"Mixed\"\n" +
                "  }\n" +
                "}\n" +
                "----\n";
        String pathsDocument = new String(Files.readAllBytes(outputDirectory.resolve("paths.adoc")));
        assertThat(pathsDocument)
                .contains("==== Request body\n" + petGeneratedExample);
        assertThat(pathsDocument)
                .contains("== Request path\n" + "----\n" +
                        "\"/pets\"\n" +
                        "----");
        assertThat(pathsDocument)
                .contains("==== Request query\n" +
                        "----\n" +
                        "{\n" +
                        "  \"status\" : \"string\"\n" +
                        "}\n" +
                        "----\n");
        assertThat(pathsDocument)
                .contains("==== Response 405\n" + petResponseExample);
        assertThat(pathsDocument)
                .contains("==== Response 200\n" +
                        "----\n" +
                        "\"array\"\n" +
                        "----");
        assertThat(pathsDocument)
                .contains("==== Request path\n" +
                        "----\n" +
                        "\"/pets/0\"\n" +
                        "----");

        String definitionsDocument = new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")));
        assertThat(definitionsDocument)
                .contains("|name||true|string||\"doggie\"");
        assertThat(definitionsDocument)
                .contains("|id||false|integer(int64)||77");
        assertThat(definitionsDocument).contains("|pictures||false|string(byte) array||[ \"string\" ]");
        assertThat(definitionsDocument).contains("|shipDate||false|string(date-time)||\"string\"");
        assertThat(definitionsDocument)
                .doesNotContain("99");
        assertThat(definitionsDocument)
                .contains("|nicknames||false|object||{\n" +
                        "  \"string\" : \"string\"\n" +
                        "}");
        assertThat(definitionsDocument)
                .contains("[options=\"header\", cols=\".^1h,.^6,.^1,.^1,.^1,.^1\"]\n" +
                        "|===\n" +
                        "|Name|Description|Required|Schema|Default|Example\n" +
                        "|id||false|integer(int64)||0\n" +
                        "|===\n");


    }

    @Test
    public void testSwagger2AsciiDocWithInlineSchema() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").toURI());
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
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTags() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTagsWithMissingTag() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_missing_tag.json").toURI());
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
    public void testSwagger2AsciiDocConversionWithDefinitionDescriptions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionDoesNotContainUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_should_not_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .doesNotContain("=== URI scheme");
    }

    @Test
    public void testSwagger2AsciiDocConversionContainsUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_should_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .contains("=== URI scheme");
    }


    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedDefinitions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(5).containsAll(
                asList("definitions", "definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path definitionsDirectory = outputDirectory.resolve("definitions");
        String[] definitions = definitionsDirectory.toFile().list();
        assertThat(definitions).hasSize(5).containsAll(
                asList("Category.adoc", "Order.adoc", "Pet.adoc", "Tag.adoc", "User.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedOperations() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedOperations()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(5).containsAll(
                asList("operations", "definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path pathsDirectory = outputDirectory.resolve("operations");
        String[] paths = pathsDirectory.toFile().list();
        assertThat(paths).hasSize(18);
    }

    @Test
    public void testSwagger2AsciiDocConversionWithRussianOutputLanguage() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc")), Charset.forName("UTF-8")))
                .contains("== Sch\u00E9ma d'URI");
    }

    @Test
    public void testSwagger2AsciiDocExtensions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
}
