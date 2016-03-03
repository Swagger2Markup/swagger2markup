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
package io.github.robwin.swagger2markup.extension;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.repository.DynamicDefinitionsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOperationsContentExtension;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class MySwaggerExtension extends SwaggerExtension {

    public MySwaggerExtension() {
        super();
    }

    public void apply(Swagger2MarkupConverter.Context globalContext) {
        globalContext.swagger.setHost("host.domain.tld");
    }
}

public class Swagger2MarkupExtensionRegistryTest {

    @Test
    public void testRegistering() {
        Swagger2MarkupExtensionRegistry.Builder registryBuilder = Swagger2MarkupExtensionRegistry.ofDefaults();

        registryBuilder.withExtension(new MySwaggerExtension());
        registryBuilder.withExtension(new DynamicDefinitionsContentExtension(Paths.get("src/docs/asciidoc/extensions")));
        registryBuilder.withExtension(new DynamicOperationsContentExtension(Paths.get("src/docs/asciidoc/extensions")));

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
        Extension ext1 = new MySwaggerExtension();
        Extension ext2 = new MySwaggerExtension();
        Extension ext3 = new SwaggerExtension() {
            public void apply(Swagger2MarkupConverter.Context globalContext) {
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
        assertThat(registry.getExtensions(SwaggerExtension.class)).isEqualTo(Arrays.asList(ext2, ext3, ext1));
    }
}
