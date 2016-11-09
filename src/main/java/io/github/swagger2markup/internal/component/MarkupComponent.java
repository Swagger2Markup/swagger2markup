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

package io.github.swagger2markup.internal.component;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

import java.util.ResourceBundle;

public abstract class MarkupComponent {

    static final String COLON = " : ";

    ResourceBundle labels;
    MarkupDocBuilder markupDocBuilder;
    Swagger2MarkupConfig config;

    MarkupComponent(Context context){
        this.config = context.config;
        this.markupDocBuilder = context.markupDocBuilder;
        this.labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
    }

    abstract MarkupDocBuilder render();

    MarkupDocBuilder copyMarkupDocBuilder() {
        return markupDocBuilder.copy(false);
    }

    String literalText(String text) {
        return copyMarkupDocBuilder().literalText(text).toString();
    }

    String boldText(String text) {
        return copyMarkupDocBuilder().boldText(text).toString();
    }

    String italicText(String text) {
        return copyMarkupDocBuilder().italicText(text).toString();
    }

    public static class Context {
        public final Swagger2MarkupConfig config;
        public final MarkupDocBuilder markupDocBuilder;

        public Context(Swagger2MarkupConfig config, MarkupDocBuilder markupDocBuilder){
            this.config = config;
            this.markupDocBuilder = markupDocBuilder;
        }
    }
}
