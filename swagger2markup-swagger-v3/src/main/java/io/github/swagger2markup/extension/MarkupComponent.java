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

package io.github.swagger2markup.extension;

import io.github.swagger2markup.config.builder.OpenAPI2MarkupConfigBuilder;
import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.config.Labels;
import io.swagger.v3.oas.models.OpenAPI;
import io.vavr.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MarkupComponent<D, T, R> implements Function2<D, T, R> {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected OpenAPI2MarkupConverter.Context<OpenAPI> context;
    protected Labels labels;
    protected OpenAPI2MarkupConfigBuilder.OpenSchema2MarkupConfig config;
    protected OpenAPI2MarkupExtensionRegistry extensionRegistry;

    public MarkupComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        this.context = context;
        this.config = context.getConfig();
        this.extensionRegistry = context.getExtensionRegistry();
        this.labels = context.getLabels();
    }
}
