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
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.github.swagger2markup.config.OpenAPILabels.*;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.*;

public class SecurityRequirementTableComponent extends MarkupComponent<StructuralNode, SecurityRequirementTableComponent.Parameters, StructuralNode> {

    public SecurityRequirementTableComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static SecurityRequirementTableComponent.Parameters parameters(List<SecurityRequirement> securityRequirements, boolean addTitle) {
        return new SecurityRequirementTableComponent.Parameters(securityRequirements, addTitle);
    }

    public StructuralNode apply(StructuralNode document, List<SecurityRequirement> securityRequirements, boolean addTitle) {
        return apply(document, parameters(securityRequirements, addTitle));
    }

    @Override
    public StructuralNode apply(StructuralNode node, SecurityRequirementTableComponent.Parameters parameters) {
        List<SecurityRequirement> securityRequirements = parameters.securityRequirements;

        if (securityRequirements == null || securityRequirements.isEmpty()) return node;

        TableImpl securityRequirementsTable = new TableImpl(node, new HashMap<>(), new ArrayList<>());
        securityRequirementsTable.setOption("header");
        securityRequirementsTable.setAttribute("caption", "", true);
        securityRequirementsTable.setAttribute("cols", ".^3a,.^4a,.^13a", true);
        if (parameters.addTitle) {
            securityRequirementsTable.setTitle(labels.getLabel(TABLE_TITLE_SECURITY));
        }
        securityRequirementsTable.setHeaderRow(
                labels.getLabel(TABLE_HEADER_TYPE),
                labels.getLabel(TABLE_HEADER_NAME),
                labels.getLabel(TABLE_HEADER_SCOPES));

        securityRequirements.forEach(securityRequirement ->
                securityRequirement.forEach((name, scopes) ->
                        securityRequirementsTable.addRow(
                                generateInnerDoc(securityRequirementsTable, boldUnconstrained(scopes.isEmpty() ? "apiKey" : "oauth2")),
                                generateInnerDoc(securityRequirementsTable, name),
                                generateInnerDoc(securityRequirementsTable, String.join(", ", scopes))
                        )
                )
        );
        node.append(securityRequirementsTable);
        return node;
    }

    public static class Parameters {

        private final List<SecurityRequirement> securityRequirements;
        private final boolean addTitle;

        public Parameters(List<SecurityRequirement> securityRequirements, boolean addTitle) {
            this.securityRequirements = securityRequirements;
            this.addTitle = addTitle;
        }
    }
}
