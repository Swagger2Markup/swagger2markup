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
package io.github.swagger2markup.builder;

import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.spi.SwaggerModelExtension;
import io.swagger.models.Swagger;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Swagger2MarkupExtensionRegistryBuilderTest {

    @Test
    public void testRegistering() {
        Swagger2MarkupExtensionRegistryBuilder registryBuilder = new Swagger2MarkupExtensionRegistryBuilder();
        registryBuilder.withSwaggerModelExtension(new MySwaggerModelExtension());
    }

    @Test
    public void testListing() {
        SwaggerModelExtension ext1 = new MySwaggerModelExtension();
        SwaggerModelExtension ext2 = new MySwaggerModelExtension();
        SwaggerModelExtension ext3 = new SwaggerModelExtension() {
            public void apply(Swagger swagger) {
            }
        };

        Swagger2MarkupExtensionRegistry registry = new Swagger2MarkupExtensionRegistryBuilder()
                .withSwaggerModelExtension(ext2)
                .withSwaggerModelExtension(ext3)
                .withSwaggerModelExtension(ext1)
                .build();
        List<SwaggerModelExtension> extensions = registry.getSwaggerModelExtensions();
        assertThat(extensions.size()).isEqualTo(3);
        assertThat(extensions).contains(ext1, ext2, ext3);
    }
}
