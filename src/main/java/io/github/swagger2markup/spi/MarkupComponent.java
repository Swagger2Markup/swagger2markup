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

package io.github.swagger2markup.spi;

import io.github.swagger2markup.Labels;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import javaslang.Function2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public abstract class MarkupComponent <T> implements Function2<MarkupDocBuilder, T, MarkupDocBuilder> {

    public Logger logger = LoggerFactory.getLogger(getClass());

    public static final String COLON = " : ";

    public Swagger2MarkupConverter.Context context;
    public Labels labels;
    public Swagger2MarkupConfig config;
    public Swagger2MarkupExtensionRegistry extensionRegistry;

    public MarkupComponent(Swagger2MarkupConverter.Context context){
        this.context = context;
        this.config = context.getConfig();
        this.extensionRegistry = context.getExtensionRegistry();
        this.labels = context.getLabels();
    }

    public MarkupDocBuilder copyMarkupDocBuilder(MarkupDocBuilder markupDocBuilder) {
        return markupDocBuilder.copy(false);
    }

    public String literalText(MarkupDocBuilder markupDocBuilder, String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).literalText(text).toString();
    }
    public String boldText(MarkupDocBuilder markupDocBuilder, String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).boldText(text).toString();
    }

    public String italicText(MarkupDocBuilder markupDocBuilder, String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).italicText(text).toString();
    }

    public String crossReference(MarkupDocBuilder markupDocBuilder, String document, String anchor, String text) {
        return copyMarkupDocBuilder(markupDocBuilder)
                .crossReference(document, anchor, text).toString();
    }

    public String markupDescription(MarkupDocBuilder markupDocBuilder, String markupText) {
        if (StringUtils.isBlank(markupText)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).importMarkup(new StringReader(markupText), config.getSwaggerMarkupLanguage()).toString().trim();
    }
}
