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

import io.github.swagger2markup.assertions.DiffUtils;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneralConverterTest {

    @Test
    public void testOutputFile() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"));
        Path outputFile = Paths.get("build/test/asciidoc/outputFile.adoc");

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .toFileWithoutExtension(outputFile);

        //Then
        Path expectedFile = Paths.get(AsciidocConverterTest.class.getResource("/expected/asciidoc/toFile/outputFile.adoc").toURI());
        DiffUtils.assertThatFileIsEqual(expectedFile, outputFile, "testOutputFile.html");
    }
    
}
