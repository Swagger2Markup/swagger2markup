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
import io.github.swagger2markup.adoc.ast.impl.DescriptionListEntryImpl;
import io.github.swagger2markup.adoc.ast.impl.DescriptionListImpl;
import io.github.swagger2markup.adoc.ast.impl.ListItemImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.examples.Example;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;

import java.util.Collections;
import java.util.Map;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.*;

public class ExamplesComponent extends MarkupComponent<StructuralNode, ExamplesComponent.Parameters, StructuralNode> {

    private final MediaTypeExampleComponent mediaTypeExampleComponent;

    public ExamplesComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.mediaTypeExampleComponent = new MediaTypeExampleComponent(context);
    }

    public static ExamplesComponent.Parameters parameters(Map<String, Example> examples) {
        return new ExamplesComponent.Parameters(examples);
    }

    public StructuralNode apply(StructuralNode node, Map<String, Example> examples) {
        return apply(node, parameters(examples));
    }

    @Override
    public StructuralNode apply(StructuralNode node, ExamplesComponent.Parameters parameters) {
        Map<String, Example> examples = parameters.examples;
        if (examples == null || examples.isEmpty()) return node;

        DescriptionListImpl examplesList = new DescriptionListImpl(node);
        examplesList.setTitle(LABEL_EXAMPLES);

        examples.forEach((name, example) -> {
            DescriptionListEntryImpl exampleEntry = new DescriptionListEntryImpl(examplesList, Collections.singletonList(new ListItemImpl(examplesList, name)));
            ListItemImpl tagDesc = new ListItemImpl(exampleEntry, "");

            ParagraphBlockImpl exampleBlock = new ParagraphBlockImpl(tagDesc);

            appendDescription(exampleBlock, example.getSummary());
            appendDescription(exampleBlock, example.getDescription());
            mediaTypeExampleComponent.apply(tagDesc, example.getValue());

            ParagraphBlockImpl paragraphBlock = new ParagraphBlockImpl(tagDesc);
            String source = "";
            generateRefLink(source, example.getExternalValue(), LABEL_EXTERNAL_VALUE);
            generateRefLink(source, example.get$ref(), "");
            if(StringUtils.isNotBlank(source)){
                paragraphBlock.setSource(source);
                tagDesc.append(paragraphBlock);
            }

            exampleEntry.setDescription(tagDesc);

            examplesList.addEntry(exampleEntry);
        });
        node.append(examplesList);

        return node;
    }

    private String generateRefLink(String source, String ref, String alt) {
        if (StringUtils.isNotBlank(ref)) {
            if (StringUtils.isBlank(alt)) {
                alt = ref.substring(ref.lastIndexOf('/') + 1);
            }
            String anchor = ref.replaceFirst("#", "").replaceAll("/", "_");
            source += "<<" + anchor + "," + alt + ">>" + LINE_SEPARATOR;
        }
        return source;
    }


    public static class Parameters {

        private final Map<String, Example> examples;

        public Parameters(Map<String, Example> examples) {
            this.examples = examples;
        }
    }
}
