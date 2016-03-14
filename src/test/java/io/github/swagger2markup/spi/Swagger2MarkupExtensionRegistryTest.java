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
package io.github.swagger2markup.spi;

import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.swagger.models.Swagger;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class Swagger2MarkupExtensionRegistryTest {

    @Test
    public void testRegistering() {
        Swagger2MarkupExtensionRegistry.Builder registryBuilder = Swagger2MarkupExtensionRegistry.ofDefaults();

        registryBuilder.withExtension(new MySwaggerModelExtension());

        try {
            registryBuilder.withExtension(new AbstractExtension() {
            });
            fail("No IllegalArgumentException thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Provided extension class does not extend any of the supported extension points");
        }
    }

    @Test
    public void testListing() {
        Extension ext1 = new MySwaggerModelExtension();
        Extension ext2 = new MySwaggerModelExtension();
        Extension ext3 = new SwaggerModelExtension() {
            public void apply(Swagger swagger) {
            }
        };

        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofDefaults()
                .withExtension(ext2)
                .withExtension(ext3)
                .withExtension(ext1)
                .build();
        List<Extension> extensions = registry.getExtensions(Extension.class);
        assertThat(extensions.size()).isEqualTo(7);
        assertThat(extensions).contains(ext1, ext2, ext3);
        assertThat(registry.getExtensions(SwaggerModelExtension.class)).isEqualTo(Arrays.asList(ext2, ext3, ext1));
    }
}
