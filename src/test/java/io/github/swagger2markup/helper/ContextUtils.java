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
package io.github.swagger2markup.helper;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.builder.Swagger2MarkupExtensionRegistryBuilder;
import io.swagger.models.Swagger;

public class ContextUtils {

    public static Swagger2MarkupConverter.Context createContext() {
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder().build();
        Swagger2MarkupExtensionRegistry extensionRegistry = new Swagger2MarkupExtensionRegistryBuilder().build();
        return new Swagger2MarkupConverter.Context(config, extensionRegistry, null, null);
    }

    public static Swagger2MarkupConverter.Context createContext(Swagger2MarkupConfig config) {
        Swagger2MarkupExtensionRegistry extensionRegistry = new Swagger2MarkupExtensionRegistryBuilder().build();
        return new Swagger2MarkupConverter.Context(config, extensionRegistry, null, null);
    }

    public static Swagger2MarkupConverter.Context createContext(Swagger2MarkupConfig config, Swagger swagger) {
        Swagger2MarkupExtensionRegistry extensionRegistry = new Swagger2MarkupExtensionRegistryBuilder().build();
        return new Swagger2MarkupConverter.Context(config, extensionRegistry, swagger, null);
    }
}
