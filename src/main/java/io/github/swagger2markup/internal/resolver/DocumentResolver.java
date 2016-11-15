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
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import javaslang.Function1;

/**
 * A functor to return the document part of an inter-document cross-references, depending on the context.
 */
public abstract class DocumentResolver implements Function1<String, String> {

    Swagger2MarkupConverter.Context context;
    MarkupDocBuilder markupDocBuilder;
    Swagger2MarkupConfig config;

    public DocumentResolver(Swagger2MarkupConverter.Context context) {
        this.context = context;
        this.markupDocBuilder = context.createMarkupDocBuilder();
        this.config = context.getConfig();
    }
}