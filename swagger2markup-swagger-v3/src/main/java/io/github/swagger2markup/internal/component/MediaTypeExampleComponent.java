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
package io.github.swagger2markup.internal.component;

import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.DELIMITER_BLOCK;
import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.LABEL_EXAMPLE;

public class MediaTypeExampleComponent extends MarkupComponent<StructuralNode, MediaTypeExampleComponent.Parameters, StructuralNode> {

    public MediaTypeExampleComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static MediaTypeExampleComponent.Parameters parameters(Object example) {
        return new MediaTypeExampleComponent.Parameters(example);
    }

    public StructuralNode apply(StructuralNode node, Object example) {
        return apply(node, parameters(example));
    }

    @Override
    public StructuralNode apply(StructuralNode node, MediaTypeExampleComponent.Parameters parameters) {
        Object example = parameters.example;
        if (example == null || StringUtils.isBlank(example.toString())) return node;

        ParagraphBlockImpl sourceBlock = new ParagraphBlockImpl(node);
        sourceBlock.setTitle(LABEL_EXAMPLE);
        sourceBlock.setAttribute("style", "source", true);
        sourceBlock.setSource(DELIMITER_BLOCK + LINE_SEPARATOR + example + LINE_SEPARATOR + DELIMITER_BLOCK);
        node.append(sourceBlock);

        return node;
    }

    public static class Parameters {

        private final Object example;

        public Parameters(Object example) {
            this.example = example;
        }
    }
}
