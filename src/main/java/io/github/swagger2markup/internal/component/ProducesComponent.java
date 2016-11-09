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


import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import org.jsoup.helper.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class ProducesComponent extends MarkupComponent {

    private final List<String> produces;
    private final int titleLevel;

    public ProducesComponent(Context context,
                             List<String> produces,
                             int titleLevel){
        super(context);
        Validate.notNull(produces, "Produces must not be null");
        this.produces = produces;
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(Labels.PRODUCES));
        markupDocBuilder.unorderedList(produces.stream()
                .map(this::literalText).collect(Collectors.toList()));
        return markupDocBuilder;
    }
}
