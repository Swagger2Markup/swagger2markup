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
package io.github.swagger2markup.internal.utils;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;

public class MarkupDocBuilderUtils {

    public static MarkupDocBuilder copyMarkupDocBuilder(MarkupDocBuilder markupDocBuilder) {
        return markupDocBuilder.copy(false);
    }

    public static String literalText(MarkupDocBuilder markupDocBuilder, String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).literalText(text).toString();
    }

    public static String boldText(MarkupDocBuilder markupDocBuilder, String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).boldText(text).toString();
    }

    public static String italicText(MarkupDocBuilder markupDocBuilder, String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).italicText(text).toString();
    }

    public static String crossReference(MarkupDocBuilder markupDocBuilder, String document, String anchor, String text) {
        return copyMarkupDocBuilder(markupDocBuilder)
                .crossReference(document, anchor, text).toString();
    }

    public static String markupDescription(MarkupLanguage swaggerMarkupLanguage, MarkupDocBuilder markupDocBuilder, String markupText) {
        if (StringUtils.isBlank(markupText)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder(markupDocBuilder).importMarkup(new StringReader(markupText), swaggerMarkupLanguage).toString().trim();
    }
}
