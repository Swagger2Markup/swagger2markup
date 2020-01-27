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
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.swagger.v3.oas.models.headers.Header;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HeadersComponent extends MarkupComponent<StructuralNode, HeadersComponent.Parameters, StructuralNode> {

    private final SchemaComponent schemaComponent;

    public HeadersComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.schemaComponent = new SchemaComponent(context);
    }

    public static HeadersComponent.Parameters parameters(Map<String, Header> headers) {
        return new HeadersComponent.Parameters(headers);
    }

    public StructuralNode apply(StructuralNode node, Map<String, Header> headers) {
     return apply(node, parameters(headers));
    }

    @Override
    public StructuralNode apply(StructuralNode node, HeadersComponent.Parameters parameters) {
        Map<String, Header> headers = parameters.headers;
        if (null == headers || headers.isEmpty()) return node;

        TableImpl responseHeadersTable = new TableImpl(node, new HashMap<>(), new ArrayList<>());
        responseHeadersTable.setOption("header");
        responseHeadersTable.setAttribute("caption", "", true);
        responseHeadersTable.setAttribute("cols", ".^2a,.^14a,.^4a", true);
        responseHeadersTable.setTitle(OpenApiHelpers.TABLE_TITLE_HEADERS);
        responseHeadersTable.setHeaderRow(OpenApiHelpers.TABLE_HEADER_NAME, OpenApiHelpers.TABLE_HEADER_DESCRIPTION, OpenApiHelpers.TABLE_HEADER_SCHEMA);
        headers.forEach((name, header) ->
                responseHeadersTable.addRow(
                        OpenApiHelpers.generateInnerDoc(responseHeadersTable, name),
                        OpenApiHelpers.generateInnerDoc(responseHeadersTable, Optional.ofNullable(header.getDescription()).orElse("")),
                        schemaComponent.apply(responseHeadersTable, header.getSchema())
                ));
        node.append(responseHeadersTable);
        return node;
    }

    public static class Parameters {

        private final Map<String, Header> headers;

        public Parameters(Map<String, Header> headers) {
            this.headers = headers;
        }
    }
}
