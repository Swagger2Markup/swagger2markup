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

import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneralConverterTest {

    /*
    @Test
    public void testConfigDefaultPaths() throws URISyntaxException {
        //Given
        Path file = Paths.get(GeneralConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withDefinitionDescriptions()
                .withOperationDescriptions()
                .build();

        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build();

        //Then
        URI baseUri = IOUtils.uriParent(converter.getContext().getSwaggerLocation());
        assertThat(converter.getContext().getConfig().getDefinitionDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converter.getContext().getConfig().getOperationDescriptionsUri()).isEqualTo(baseUri);
    }
    */

    @Test
    public void testConfigDefaultPathsWithUri() throws MalformedURLException {
        //Given

        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withDefinitionDescriptions()
                .withOperationDescriptions()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(URI.create("http://petstore.swagger.io/v2/swagger.json").toURL())
                .withConfig(config)
                .build();

        //Then
        assertThat(converterBuilder.getContext().getConfig().getDefinitionDescriptionsUri()).isNull();
        assertThat(converterBuilder.getContext().getConfig().getOperationDescriptionsUri()).isNull();
    }

    /*
    @Test
    public void testDefaultPathsWithoutFile() {
        //Given
        //When
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withDefinitionDescriptions()
                .build();

        //Then
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(new Swagger())
                .withConfig(config)
                .build();
        assertThat(converter.getContext().getConfig().isDefinitionDescriptionsEnabled()).isFalse();
    }
    */
}
