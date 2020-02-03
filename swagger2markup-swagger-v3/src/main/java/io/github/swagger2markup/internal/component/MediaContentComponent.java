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
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.media.Content;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.Collections;

import static io.github.swagger2markup.config.OpenAPILabels.LABEL_CONTENT;

public class MediaContentComponent extends MarkupComponent<StructuralNode, MediaContentComponent.Parameters, StructuralNode> {

    private final MediaTypeExampleComponent mediaTypeExampleComponent;
    private final ExamplesComponent examplesComponent;
    private final SchemaComponent schemaComponent;
    private final EncodingComponent encodingComponent;

    public MediaContentComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.mediaTypeExampleComponent = new MediaTypeExampleComponent(context);
        this.examplesComponent = new ExamplesComponent(context);
        this.schemaComponent = new SchemaComponent(context);
        this.encodingComponent = new EncodingComponent(context);
    }

    public static MediaContentComponent.Parameters parameters(Content content) {
        return new MediaContentComponent.Parameters(content);
    }

    public StructuralNode apply(StructuralNode node, Content content) {
        return apply(node, parameters(content));
    }

    @Override
    public StructuralNode apply(StructuralNode node, MediaContentComponent.Parameters parameters) {
        Content content = parameters.content;
        if (content == null || content.isEmpty()) return node;

        DescriptionListImpl mediaContentList = new DescriptionListImpl(node);
        mediaContentList.setTitle(labels.getLabel(LABEL_CONTENT));

        content.forEach((type, mediaType) -> {
            DescriptionListEntryImpl tagEntry = new DescriptionListEntryImpl(mediaContentList, Collections.singletonList(new ListItemImpl(mediaContentList, type)));
            ListItemImpl tagDesc = new ListItemImpl(tagEntry, "");

            Document tagDescDocument = schemaComponent.apply(mediaContentList, mediaType.getSchema());
            mediaTypeExampleComponent.apply(tagDescDocument, mediaType.getExample());
            examplesComponent.apply(tagDescDocument, mediaType.getExamples());
            encodingComponent.apply(tagDescDocument, mediaType.getEncoding());
            tagDesc.append(tagDescDocument);

            tagEntry.setDescription(tagDesc);
            mediaContentList.addEntry(tagEntry);
        });
        node.append(mediaContentList);
        return node;
    }

    public static class Parameters {

        private final Content content;

        public Parameters(Content content) {
            this.content = content;
        }
    }
}
