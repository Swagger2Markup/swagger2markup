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
package io.github.swagger2markup.internal.document.builder;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.component.MarkupComponent;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupDocBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * @author Robert Winkler
 */
abstract class MarkupDocumentBuilder {

    Logger logger = LoggerFactory.getLogger(getClass());

    Swagger2MarkupConverter.Context globalContext;
    Swagger2MarkupExtensionRegistry extensionRegistry;
    Swagger2MarkupConfig config;
    MarkupDocBuilder markupDocBuilder;
    Path outputPath;
    ResourceBundle labels;
    MarkupComponent.Context componentContext;

    MarkupDocumentBuilder(Swagger2MarkupConverter.Context globalContext, Swagger2MarkupExtensionRegistry extensionRegistry, Path outputPath) {
        this.globalContext = globalContext;
        this.extensionRegistry = extensionRegistry;
        this.config = globalContext.getConfig();
        this.outputPath = outputPath;
        this.markupDocBuilder = MarkupDocBuilders.documentBuilder(config.getMarkupLanguage(), config.getLineSeparator()).withAnchorPrefix(config.getAnchorPrefix());
        this.componentContext = new MarkupComponent.Context(config, markupDocBuilder, extensionRegistry);
        labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
    }

    /**
     * Builds the MarkupDocument.
     *
     * @return the built MarkupDocument
     * @throws IOException if the files to include are not readable
     */
    public abstract MarkupDocument build() throws IOException;


    MarkupDocBuilder copyMarkupDocBuilder() {
        return markupDocBuilder.copy(false);
    }
}
