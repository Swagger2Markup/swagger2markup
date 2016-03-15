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

import io.github.swagger2markup.spi.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.ServiceLoader.load;
import static org.apache.commons.collections4.IteratorUtils.toList;

public class Swagger2MarkupExtensionRegistry {

    private Context context;

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
                       List<SecurityDocumentExtension> securityDocumentExtensions){
            this.swaggerModelExtensions = swaggerModelExtensions;
            this.overviewDocumentExtensions = overviewDocumentExtensions;
            this.definitionsDocumentExtensions = definitionsDocumentExtensions;
            this.pathsDocumentExtensions = pathsDocumentExtensions;
            this.securityDocumentExtensions = securityDocumentExtensions;
        }
    }

    Swagger2MarkupExtensionRegistry(Context context) {
        this.context = context;
    }

    public List<SwaggerModelExtension> getSwaggerModelExtensions(){
        return context.swaggerModelExtensions;
    }

    public List<OverviewDocumentExtension> getOverviewDocumentExtensions(){
        return context.overviewDocumentExtensions;
    }

    public List<DefinitionsDocumentExtension> getDefinitionsDocumentExtensions(){
        return context.definitionsDocumentExtensions;
    }

    public List<SecurityDocumentExtension> getSecurityDocumentExtensions(){
        return context.securityDocumentExtensions;
    }

    public List<PathsDocumentExtension> getPathsDocumentExtensions(){
        return context.pathsDocumentExtensions;
    }

    public static Builder ofEmpty() {
        Context context = new Context(
                new ArrayList<SwaggerModelExtension>(),
                new ArrayList<OverviewDocumentExtension>(),
                new ArrayList<DefinitionsDocumentExtension>(),
                new ArrayList<PathsDocumentExtension>(),
                new ArrayList<SecurityDocumentExtension>());
        return new Builder(context);
    }

    public static Builder ofServiceLoader() {
        List<SwaggerModelExtension> swaggerModelExtensions = toList(load(SwaggerModelExtension.class).iterator());
        List<OverviewDocumentExtension> overviewDocumentExtensions = toList(load(OverviewDocumentExtension.class).iterator());
        List<DefinitionsDocumentExtension> definitionsDocumentExtensions = toList(load(DefinitionsDocumentExtension.class).iterator());
        List<PathsDocumentExtension> pathsDocumentExtensions = toList(load(PathsDocumentExtension.class).iterator());
        List<SecurityDocumentExtension> securityDocumentExtensions = toList(load(SecurityDocumentExtension.class).iterator());
        Context context = new Context(
                swaggerModelExtensions,
                overviewDocumentExtensions,
                definitionsDocumentExtensions,
                pathsDocumentExtensions,
                securityDocumentExtensions);
        return new Builder(context);
    }

    public static class Builder {

        private final Context context;

        Builder(Context context) {
            this.context = context;
        }

        public Swagger2MarkupExtensionRegistry build() {
            return new Swagger2MarkupExtensionRegistry(context);
        }

        public Builder withSwaggerModelExtension(SwaggerModelExtension extension) {
            context.swaggerModelExtensions.add(extension);
            return this;
        }

        public Builder withOverviewDocumentExtension(OverviewDocumentExtension extension) {
            context.overviewDocumentExtensions.add(extension);
            return this;
        }

        public Builder withDefinitionsDocumentExtension(DefinitionsDocumentExtension extension) {
            context.definitionsDocumentExtensions.add(extension);
            return this;
        }

        public Builder withPathsDocumentExtension(PathsDocumentExtension extension) {
            context.pathsDocumentExtensions.add(extension);
            return this;
        }

        public Builder withSecurityDocumentExtension(SecurityDocumentExtension extension) {
            context.securityDocumentExtensions.add(extension);
            return this;
        }
    }
}
