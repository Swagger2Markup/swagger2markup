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
package io.github.swagger2markup;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class GeneralConverterTest {

    private static final String[] EXPECTED_FILES = new String[]{"definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"};
    private List<String> expectedFiles;

    @Before
    public void setUp() {
        expectedFiles = new ArrayList<>(asList(EXPECTED_FILES));
    }

    @Test
    public void testFromHttpURI() throws IOException, URISyntaxException {
        //Given
        Path outputDirectory = Paths.get("build/test/asciidoc/fromUri");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(URI.create("http://petstore.swagger.io/v2/swagger.json")).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

    @Test
    public void testFromResourceURI() throws IOException, URISyntaxException {
        //Given
        Path outputDirectory = Paths.get("build/test/asciidoc/fileUri");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(GeneralConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI()).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

    @Test
    public void testFromPathURI() throws IOException, URISyntaxException {
        //Given
        Path outputDirectory = Paths.get("build/test/asciidoc/pathUri");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(Paths.get("src/test/resources/yaml/swagger_petstore.yaml").toUri()).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

    @Test
    public void testFromStringURIWithoutScheme() throws IOException, URISyntaxException {
        //Given
        Path outputDirectory = Paths.get("build/test/asciidoc/pathUri");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(URI.create("src/test/resources/yaml/swagger_petstore.yaml")).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

    @Test
    public void testFromURL() throws IOException, URISyntaxException {
        //Given
        Path outputDirectory = Paths.get("build/test/asciidoc/fromUrl");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(new URL("http://petstore.swagger.io/v2/swagger.json")).build()
                .toFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

}
