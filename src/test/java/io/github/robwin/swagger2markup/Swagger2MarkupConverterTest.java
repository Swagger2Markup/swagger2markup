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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.assertions.DiffAssertions;
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
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"));
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
        String[] directories = outputDirectory.toFile().list();
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
        String[] directories = outputDirectory.toFile().list();
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
                .contains("|id||false|integer(int64)||77");
        assertThat(definitionsDocument).contains("|pictures||false|string(byte) array||[ \"string\" ]");
        assertThat(definitionsDocument).contains("|shipDate||false|string(date-time)||\"string\"");
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
        String[] directories = outputDirectory.toFile().list();
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
                .contains("|id||false|integer(int64)||77");
        assertThat(definitionsDocument)
                .contains("|name||true|string||doggie");
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
    public void testSwagger2AsciiDocConversionAsString() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

        //When
        String asciiDocAsString = Swagger2MarkupConverter.from(file).build()
                .asString();
        //Then
        assertThat(asciiDocAsString).isNotEmpty();
    }


    @Test
    public void testSwagger2AsciiDocConversion() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] directories = outputDirectory.toFile().list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        Path actual = outputDirectory.resolve("overview.adoc");
        Path expected = Paths.get(Swagger2MarkupConverterTest.class.getResource("/results/asciidoc/default/overview.adoc").toURI());

        DiffAssertions.assertThat(actual)
                .isEqualTo(expected,"testSwagger2AsciiDocConversion.html");

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
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTags() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
    public void testSwagger2AsciiDocConversionWithDefinitionDescriptions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        assertThat(definitions).hasSize(5).containsAll(
                asList("Category.adoc", "Order.adoc", "Pet.adoc", "Tag.adoc", "User.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedOperations() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        assertThat(definitions).hasSize(5).containsAll(
                asList("Category.md", "Order.md", "Pet.md", "Tag.md", "User.md"));
    }

    @Test
    public void testSwagger2MarkdownConversionHandlesComposition() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
                definitionsDirectory.resolve("User.md").toFile(),
                ImmutableMap.<String, Set<String>>builder()
                        .put("User", ImmutableSet.of("id", "username", "firstName",
                                "lastName", "email", "password", "phone", "userStatus"))
                        .build()
        );

    }

    @Test
    public void testSwagger2AsciiDocConversionWithRussianOutputLanguage() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .withOperationDescriptions()
                .withSchemas()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build();

        //Then
        URI baseUri = io.github.robwin.swagger2markup.utils.IOUtils.uriParent(converterBuilder.globalContext.swaggerLocation);
        assertThat(converterBuilder.globalContext.config.getDefinitionDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getOperationDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getSchemasUri()).isEqualTo(baseUri);
    }

    @Test
    public void testSwagger2MarkupConfigDefaultPathsWithUri() throws MalformedURLException {
        //Given

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .withOperationDescriptions()
                .withSchemas()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(URI.create("http://petstore.swagger.io/v2/swagger.json").toURL())
                .withConfig(config)
                .build();

        //Then
        assertThat(converterBuilder.globalContext.config.getDefinitionDescriptionsUri()).isNull();
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
        assertThat(converter.globalContext.config.isDefinitionDescriptionsEnabled()).isFalse();
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
        Path file = Paths.get(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
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
