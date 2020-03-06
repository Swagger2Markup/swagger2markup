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
import io.swagger.v3.oas.models.media.Schema;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;
import static io.github.swagger2markup.config.OpenAPILabels.*;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.*;

public class PropertiesTableComponent extends MarkupComponent<StructuralNode, PropertiesTableComponent.Parameters, StructuralNode> {

    private final SchemaComponent schemaComponent;

    PropertiesTableComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.schemaComponent = new SchemaComponent(context);
    }

    public static Parameters parameters(@SuppressWarnings("rawtypes") Map<String, Schema> properties, List<String> schemaRequired) {
        return new Parameters(properties, schemaRequired);
    }

    public StructuralNode apply(StructuralNode parent, @SuppressWarnings("rawtypes") Map<String, Schema> properties, List<String> schemaRequired) {
        return apply(parent, parameters(properties, schemaRequired));
    }

    public StructuralNode apply(StructuralNode parent, Parameters params) {
        @SuppressWarnings("rawtypes") Map<String, Schema> properties = params.properties;
        List<String> schemaRequired = params.schemaRequired;

        if (null == properties || properties.isEmpty()) return parent;

        List<String> finalSchemaRequired = (null == schemaRequired) ? new ArrayList<>() : schemaRequired;

        TableImpl propertiesTable = new TableImpl(parent, new HashMap<>(), new ArrayList<>());
        propertiesTable.setOption("header");
        propertiesTable.setAttribute("caption", "", true);
        propertiesTable.setAttribute("cols", ".^4a,.^16a,.^4a", true);
        propertiesTable.setTitle(labels.getLabel(TABLE_TITLE_PROPERTIES));
        propertiesTable.setHeaderRow(
                labels.getLabel(TABLE_HEADER_NAME),
                labels.getLabel(TABLE_HEADER_DESCRIPTION),
                labels.getLabel(TABLE_HEADER_SCHEMA));

        properties.forEach((name, schema) -> propertiesTable.addRow(
                    generateInnerDoc(propertiesTable, name + LINE_SEPARATOR + requiredIndicator(finalSchemaRequired.contains(name),
                            labels.getLabel(LABEL_REQUIRED), labels.getLabel(LABEL_OPTIONAL))),
                schemaComponent.apply(propertiesTable, schema),
                generateInnerDoc(propertiesTable, getSchemaTypeAsString(schema))
            ));
        parent.append(propertiesTable);
        return parent;
    }

    @SuppressWarnings("rawtypes")
    public static class Parameters {
        private final Map<String, Schema> properties;
        private final List<String> schemaRequired;

        public Parameters(Map<String, Schema> properties, List<String> schemaRequired) {

            this.properties = properties;
            this.schemaRequired = schemaRequired;
        }
    }
}
