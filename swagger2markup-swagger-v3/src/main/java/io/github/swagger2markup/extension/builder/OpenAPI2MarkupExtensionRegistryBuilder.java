/*
 * Copyright 2017 Robert Winkler
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

package io.github.swagger2markup.extension.builder;

import io.github.swagger2markup.extension.*;

import java.util.List;

import static java.util.ServiceLoader.load;
import static org.apache.commons.collections4.IteratorUtils.toList;

public class OpenAPI2MarkupExtensionRegistryBuilder {

    private final Context context;

    public OpenAPI2MarkupExtensionRegistryBuilder() {
        List<OpenAPIModelExtension> openAPIModelExtensions = toList(load(OpenAPIModelExtension.class).iterator());
        List<OverviewDocumentExtension> overviewDocumentExtensions = toList(load(OverviewDocumentExtension.class).iterator());
        List<DefinitionsDocumentExtension> definitionsDocumentExtensions = toList(load(DefinitionsDocumentExtension.class).iterator());
        List<PathsDocumentExtension> pathsDocumentExtensions = toList(load(PathsDocumentExtension.class).iterator());
        List<SecurityDocumentExtension> securityDocumentExtensions = toList(load(SecurityDocumentExtension.class).iterator());
        context = new Context(
                openAPIModelExtensions,
                overviewDocumentExtensions,
                definitionsDocumentExtensions,
                pathsDocumentExtensions,
                securityDocumentExtensions);
    }

    public OpenAPI2MarkupExtensionRegistry build() {
        return new DefaultOpenAPI2MarkupExtensionRegistry(context);
    }

    public OpenAPI2MarkupExtensionRegistryBuilder withSwaggerModelExtension(OpenAPIModelExtension extension) {
        context.openAPIModelExtensions.add(extension);
        return this;
    }

    public OpenAPI2MarkupExtensionRegistryBuilder withOverviewDocumentExtension(OverviewDocumentExtension extension) {
        context.overviewDocumentExtensions.add(extension);
        return this;
    }

    public OpenAPI2MarkupExtensionRegistryBuilder withDefinitionsDocumentExtension(DefinitionsDocumentExtension extension) {
        context.definitionsDocumentExtensions.add(extension);
        return this;
    }

    public OpenAPI2MarkupExtensionRegistryBuilder withPathsDocumentExtension(PathsDocumentExtension extension) {
        context.pathsDocumentExtensions.add(extension);
        return this;
    }

    public OpenAPI2MarkupExtensionRegistryBuilder withSecurityDocumentExtension(SecurityDocumentExtension extension) {
        context.securityDocumentExtensions.add(extension);
        return this;
    }

    static class DefaultOpenAPI2MarkupExtensionRegistry implements OpenAPI2MarkupExtensionRegistry {

        private Context context;

        DefaultOpenAPI2MarkupExtensionRegistry(Context context) {
            this.context = context;
        }

        @Override
        public List<OpenAPIModelExtension> getSwaggerModelExtensions() {
            return context.openAPIModelExtensions;
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
        final List<OpenAPIModelExtension> openAPIModelExtensions;
        final List<OverviewDocumentExtension> overviewDocumentExtensions;
        final List<DefinitionsDocumentExtension> definitionsDocumentExtensions;
        final List<PathsDocumentExtension> pathsDocumentExtensions;
        final List<SecurityDocumentExtension> securityDocumentExtensions;

        Context(List<OpenAPIModelExtension> openAPIModelExtensions,
                List<OverviewDocumentExtension> overviewDocumentExtensions,
                List<DefinitionsDocumentExtension> definitionsDocumentExtensions,
                List<PathsDocumentExtension> pathsDocumentExtensions,
                List<SecurityDocumentExtension> securityDocumentExtensions) {
            this.openAPIModelExtensions = openAPIModelExtensions;
            this.overviewDocumentExtensions = overviewDocumentExtensions;
            this.definitionsDocumentExtensions = definitionsDocumentExtensions;
            this.pathsDocumentExtensions = pathsDocumentExtensions;
            this.securityDocumentExtensions = securityDocumentExtensions;
        }
    }
}
