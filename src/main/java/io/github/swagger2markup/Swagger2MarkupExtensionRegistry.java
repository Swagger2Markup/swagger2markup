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

import java.util.List;

/**
 * Extension points registry interface.
 */
public interface Swagger2MarkupExtensionRegistry {
    /**
     * SwaggerModelExtension extension point can be used to preprocess the Swagger model.
     *
     * @return registered extensions extending SwaggerModelExtension extension point
     */
    List<SwaggerModelExtension> getSwaggerModelExtensions();

    /**
     * OverviewDocumentExtension extension point can be used to extend the overview document content.
     *
     * @return registered extensions extending OverviewDocumentExtension extension point
     */
    List<OverviewDocumentExtension> getOverviewDocumentExtensions();

    /**
     * DefinitionsDocumentExtension extension point can be used to extend the definitions document content.
     *
     * @return registered extensions extending DefinitionsDocumentExtension extension point
     */
    List<DefinitionsDocumentExtension> getDefinitionsDocumentExtensions();

    /**
     * SecurityContentExtension extension point can be used to extend the security document content.
     *
     * @return registered extensions extending SecurityContentExtension extension point
     */
    List<SecurityDocumentExtension> getSecurityDocumentExtensions();

    /**
     * PathsDocumentExtension extension point can be used to extend the paths document content.
     *
     * @return registered extensions extending PathsDocumentExtension extension point
     */
    List<PathsDocumentExtension> getPathsDocumentExtensions();
}
