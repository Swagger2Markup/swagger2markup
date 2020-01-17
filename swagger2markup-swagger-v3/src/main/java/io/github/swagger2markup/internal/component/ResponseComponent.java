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

import io.github.swagger2markup.extension.MarkupComponent;
import io.github.swagger2markup.internal.helper.OpenApiHelpers;
import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResponseComponent extends MarkupComponent<StructuralNode, ResponseComponent.Parameters, StructuralNode> {

    private final HeadersComponent headersComponent;
    private final LinkComponent linkComponent;
    private final MediaContentComponent mediaContentComponent;

    public ResponseComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.headersComponent = new HeadersComponent(context);
        this.linkComponent = new LinkComponent(context);
        this.mediaContentComponent = new MediaContentComponent(context);
    }

    public static Parameters parameters(Map<String, ApiResponse> apiResponses) {
        return new Parameters(apiResponses);
    }

    public StructuralNode apply(StructuralNode serverSection, Map<String, ApiResponse> apiResponses) {
        return apply(serverSection, parameters(apiResponses));
    }

    @Override
    public StructuralNode apply(StructuralNode serverSection, Parameters params) {
        Map<String, ApiResponse> apiResponses = params.apiResponses;

        if (null == apiResponses || apiResponses.isEmpty()) return serverSection;

        TableImpl pathResponsesTable = new TableImpl(serverSection, new HashMap<>(), new ArrayList<>());
        pathResponsesTable.setOption("header");
        pathResponsesTable.setAttribute("caption", "", true);
        pathResponsesTable.setAttribute("cols", ".^2a,.^14a,.^4a", true);
        pathResponsesTable.setTitle(OpenApiHelpers.TABLE_TITLE_RESPONSES);
        pathResponsesTable.setHeaderRow(OpenApiHelpers.TABLE_HEADER_HTTP_CODE, OpenApiHelpers.TABLE_HEADER_DESCRIPTION, OpenApiHelpers.TABLE_HEADER_LINKS);

        apiResponses.forEach((httpCode, apiResponse) ->
                pathResponsesTable.addRow(
                        OpenApiHelpers.generateInnerDoc(pathResponsesTable, httpCode),
                        getResponseDescriptionColumnDocument(pathResponsesTable, apiResponse),
                        linkComponent.apply(pathResponsesTable, apiResponse.getLinks())
                ));
        serverSection.append(pathResponsesTable);
        return serverSection;
    }

    private Document getResponseDescriptionColumnDocument(Table table, ApiResponse apiResponse) {
        Document document = OpenApiHelpers.generateInnerDoc(table, Optional.ofNullable(apiResponse.getDescription()).orElse(""));
        headersComponent.apply(document, apiResponse.getHeaders());
        mediaContentComponent.apply(document, apiResponse.getContent());
        return document;
    }

    public static class Parameters {
        private final Map<String, ApiResponse> apiResponses;

        public Parameters(Map<String, ApiResponse> apiResponses) {

            this.apiResponses = apiResponses;
        }
    }
}
