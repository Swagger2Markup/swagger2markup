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
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;


public class ExternalDocumentationComponent extends MarkupComponent<StructuralNode, ExternalDocumentationComponent.Parameters, StructuralNode> {

    public ExternalDocumentationComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static Parameters parameters(ExternalDocumentation externalDocs) {
        return new Parameters(externalDocs);
    }

    public StructuralNode apply(StructuralNode node, ExternalDocumentation externalDocs) {
        return apply(node, parameters(externalDocs));
    }

    @Override
    public StructuralNode apply(StructuralNode node, Parameters params) {
        ExternalDocumentation externalDocs = params.externalDocs;
        if (externalDocs == null) return node;

        String url = externalDocs.getUrl();
        if (StringUtils.isNotBlank(url)) {
            Block paragraph = new ParagraphBlockImpl(node);
            String desc = externalDocs.getDescription();
            paragraph.setSource(url + (StringUtils.isNotBlank(desc) ? "[" + desc + "]" : ""));
            node.append(paragraph);
        }

        return node;
    }

    public static class Parameters {
        private final ExternalDocumentation externalDocs;

        public Parameters(ExternalDocumentation externalDocs) {
            this.externalDocs = externalDocs;
        }
    }
}
