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
package io.github.swagger2markup.internal.resolver;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.utils.IOUtils;

import java.io.File;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Default {@code DefinitionDocumentResolver} functor
 */
class DefinitionDocumentResolverDefault implements DefinitionDocumentResolver {

    MarkupDocBuilder markupDocBuilder;
    Swagger2MarkupConfig config;
    Path outputPath;

    DefinitionDocumentResolverDefault(MarkupDocBuilder markupDocBuilder,
                                      Swagger2MarkupConfig config,
                                      Path outputPath) {
        this.markupDocBuilder = markupDocBuilder;
        this.config = config;
        this.outputPath = outputPath;
    }

    public String apply(String definitionName) {
        if (!config.isInterDocumentCrossReferencesEnabled() || outputPath == null)
            return null;
        else if (config.isSeparatedDefinitionsEnabled())
            return defaultString(config.getInterDocumentCrossReferencesPrefix()) + new File(config.getSeparatedDefinitionsFolder(), markupDocBuilder.addFileExtension(IOUtils.normalizeName(definitionName))).getPath();
        else
            return defaultString(config.getInterDocumentCrossReferencesPrefix()) + markupDocBuilder.addFileExtension(config.getDefinitionsDocument());
    }
}