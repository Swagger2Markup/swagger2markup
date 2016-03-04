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

import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.swagger.models.Swagger;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneralConverterTest {

    @Test
    public void testSwagger2MarkupConfigDefaultPaths() throws URISyntaxException {
        //Given
        Path file = Paths.get(GeneralConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

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
}
