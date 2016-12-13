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


import io.github.swagger2markup.Labels;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.MarkupComponent;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.literalText;

public class ConsumesComponent extends MarkupComponent<ConsumesComponent.Parameters> {

    public ConsumesComponent(Swagger2MarkupConverter.Context context) {
        super(context);
    }

    public static ConsumesComponent.Parameters parameters(List<String> consumes,
                                                          int titleLevel) {
        return new ConsumesComponent.Parameters(consumes, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(Labels.CONSUMES));
        markupDocBuilder.unorderedList(params.consumes.stream()
                .map(value -> literalText(markupDocBuilder, value)).collect(Collectors.toList()));
        return markupDocBuilder;
    }

    public static class Parameters {
        private final List<String> consumes;
        private final int titleLevel;

        public Parameters(List<String> consumes,
                          int titleLevel) {
            this.consumes = Validate.notNull(consumes, "Consumes must not be null");
            this.titleLevel = titleLevel;
        }
    }
}
