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
import io.github.swagger2markup.extension.MarkupComponent;
import io.github.swagger2markup.internal.helper.OpenApiHelpers;
import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.swagger.v3.oas.models.links.Link;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.Map;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;

public class LinkComponent extends MarkupComponent<StructuralNode, LinkComponent.Parameters, Document> {

    public LinkComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static LinkComponent.Parameters parameters(Map<String, Link> links) {
        return new LinkComponent.Parameters(links);
    }

    public Document apply(StructuralNode parent, Map<String, Link> links) {
        return apply(parent, parameters(links));
    }

    @Override
    public Document apply(StructuralNode parent, LinkComponent.Parameters parameters) {
        DocumentImpl linksDocument = new DocumentImpl(parent);
        ParagraphBlockImpl linkParagraph = new ParagraphBlockImpl(linksDocument);

        Map<String, Link> links = parameters.links;
        if (null == links || links.isEmpty()) {
            linkParagraph.setSource(OpenApiHelpers.LABEL_NO_LINKS);
        } else {
            StringBuilder sb = new StringBuilder();
            links.forEach((name, link) -> {
                sb.append(name).append(" +").append(LINE_SEPARATOR);
                sb.append(OpenApiHelpers.italicUnconstrained(OpenApiHelpers.LABEL_OPERATION)).append(' ')
                        .append(OpenApiHelpers.italicUnconstrained(link.getOperationId())).append(" +").append(LINE_SEPARATOR);
                Map<String, String> linkParameters = link.getParameters();
                if (null != linkParameters && !linkParameters.isEmpty()) {
                    sb.append(OpenApiHelpers.italicUnconstrained(OpenApiHelpers.LABEL_PARAMETERS)).append(" {").append(" +").append(LINE_SEPARATOR);
                    linkParameters.forEach((param, value) ->
                            sb.append('"').append(param).append("\": \"").append(value).append('"').append(" +").append(LINE_SEPARATOR)
                    );
                    sb.append('}').append(" +").append(LINE_SEPARATOR);
                }
            });
            linkParagraph.setSource(sb.toString());
        }
        linksDocument.append(linkParagraph);
        return linksDocument;
    }

    public static class Parameters {
        private final Map<String, Link> links;

        public Parameters(Map<String, Link> links) {
            this.links = links;
        }
    }
}
