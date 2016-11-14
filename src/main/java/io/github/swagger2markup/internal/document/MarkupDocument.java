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
package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupDocBuilders;
import javaslang.Function0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * @author Robert Winkler
 */
abstract class MarkupDocument <T> implements Function0<MarkupDocBuilder> {

    Logger logger = LoggerFactory.getLogger(getClass());

    Swagger2MarkupConverter.Context globalContext;
    Swagger2MarkupExtensionRegistry extensionRegistry;
    Swagger2MarkupConfig config;
    MarkupDocBuilder markupDocBuilder;
    Path outputPath;
    ResourceBundle labels;

    MarkupDocument(Swagger2MarkupConverter.Context globalContext, Path outputPath) {
        this.globalContext = globalContext;
        this.extensionRegistry = globalContext.getExtensionRegistry();
        this.config = globalContext.getConfig();
        this.outputPath = outputPath;
        this.markupDocBuilder = MarkupDocBuilders.documentBuilder(config.getMarkupLanguage(), config.getLineSeparator()).withAnchorPrefix(config.getAnchorPrefix());
        labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
    }

    MarkupDocBuilder copyMarkupDocBuilder() {
        return markupDocBuilder.copy(false);
    }
}
