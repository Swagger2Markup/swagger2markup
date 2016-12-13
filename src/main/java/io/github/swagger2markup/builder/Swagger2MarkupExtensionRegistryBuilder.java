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
import io.github.swagger2markup.spi.*;

import java.util.List;

import static java.util.ServiceLoader.load;
import static org.apache.commons.collections4.IteratorUtils.toList;

public class Swagger2MarkupExtensionRegistryBuilder {

    private final Context context;

    public Swagger2MarkupExtensionRegistryBuilder() {
        List<SwaggerModelExtension> swaggerModelExtensions = toList(load(SwaggerModelExtension.class).iterator());
        List<OverviewDocumentExtension> overviewDocumentExtensions = toList(load(OverviewDocumentExtension.class).iterator());
        List<DefinitionsDocumentExtension> definitionsDocumentExtensions = toList(load(DefinitionsDocumentExtension.class).iterator());
        List<PathsDocumentExtension> pathsDocumentExtensions = toList(load(PathsDocumentExtension.class).iterator());
        List<SecurityDocumentExtension> securityDocumentExtensions = toList(load(SecurityDocumentExtension.class).iterator());
        context = new Context(
                swaggerModelExtensions,
                overviewDocumentExtensions,
                definitionsDocumentExtensions,
                pathsDocumentExtensions,
                securityDocumentExtensions);
    }

    public Swagger2MarkupExtensionRegistry build() {
        return new DefaultSwagger2MarkupExtensionRegistry(context);
    }

    public Swagger2MarkupExtensionRegistryBuilder withSwaggerModelExtension(SwaggerModelExtension extension) {
        context.swaggerModelExtensions.add(extension);
        return this;
    }

    public Swagger2MarkupExtensionRegistryBuilder withOverviewDocumentExtension(OverviewDocumentExtension extension) {
        context.overviewDocumentExtensions.add(extension);
        return this;
    }

    public Swagger2MarkupExtensionRegistryBuilder withDefinitionsDocumentExtension(DefinitionsDocumentExtension extension) {
        context.definitionsDocumentExtensions.add(extension);
        return this;
    }

    public Swagger2MarkupExtensionRegistryBuilder withPathsDocumentExtension(PathsDocumentExtension extension) {
        context.pathsDocumentExtensions.add(extension);
        return this;
    }

    public Swagger2MarkupExtensionRegistryBuilder withSecurityDocumentExtension(SecurityDocumentExtension extension) {
        context.securityDocumentExtensions.add(extension);
        return this;
    }

    static class DefaultSwagger2MarkupExtensionRegistry implements Swagger2MarkupExtensionRegistry {

        private Context context;

        DefaultSwagger2MarkupExtensionRegistry(Context context) {
            this.context = context;
        }

        @Override
        public List<SwaggerModelExtension> getSwaggerModelExtensions() {
            return context.swaggerModelExtensions;
        }

        @Override
        public List<OverviewDocumentExtension> getOverviewDocumentExtensions() {
            return context.overviewDocumentExtensions;
        }

        @Override
        public List<DefinitionsDocumentExtension> getDefinitionsDocumentExtensions() {
            return context.definitionsDocumentExtensions;
        }

        @Override
        public List<SecurityDocumentExtension> getSecurityDocumentExtensions() {
            return context.securityDocumentExtensions;
        }

        @Override
        public List<PathsDocumentExtension> getPathsDocumentExtensions() {
            return context.pathsDocumentExtensions;
        }

    }

    private static class Context {
        public final List<SwaggerModelExtension> swaggerModelExtensions;
        public final List<OverviewDocumentExtension> overviewDocumentExtensions;
        public final List<DefinitionsDocumentExtension> definitionsDocumentExtensions;
        public final List<PathsDocumentExtension> pathsDocumentExtensions;
        public final List<SecurityDocumentExtension> securityDocumentExtensions;

        public Context(List<SwaggerModelExtension> swaggerModelExtensions,
                       List<OverviewDocumentExtension> overviewDocumentExtensions,
                       List<DefinitionsDocumentExtension> definitionsDocumentExtensions,
                       List<PathsDocumentExtension> pathsDocumentExtensions,
                       List<SecurityDocumentExtension> securityDocumentExtensions) {
            this.swaggerModelExtensions = swaggerModelExtensions;
            this.overviewDocumentExtensions = overviewDocumentExtensions;
            this.definitionsDocumentExtensions = definitionsDocumentExtensions;
            this.pathsDocumentExtensions = pathsDocumentExtensions;
            this.securityDocumentExtensions = securityDocumentExtensions;
        }
    }
}
