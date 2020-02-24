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
import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.github.swagger2markup.internal.helper.OpenApiHelpers;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;
import static io.github.swagger2markup.config.OpenAPILabels.*;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.boldUnconstrained;

public class SchemaComponent extends MarkupComponent<StructuralNode, SchemaComponent.Parameters, StructuralNode> {

    private final OpenAPI2MarkupConverter.OpenAPIContext context;

    public SchemaComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.context = context;
    }

    public static SchemaComponent.Parameters parameters(@SuppressWarnings("rawtypes") Schema schema) {
        return new SchemaComponent.Parameters(schema);
    }

    public Document apply(StructuralNode parent, @SuppressWarnings("rawtypes") Schema schema) {
        return apply(parent, parameters(schema));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Document apply(StructuralNode parent, SchemaComponent.Parameters parameters) {
        Document schemaDocument = new DocumentImpl(parent);
        Schema schema = parameters.schema;
        if (null == schema) return schemaDocument;

        OpenApiHelpers.appendDescription(schemaDocument, schema.getDescription());

        Map<String, Boolean> schemasBooleanProperties = new HashMap<String, Boolean>() {{
            put(labels.getLabel(LABEL_DEPRECATED), schema.getDeprecated());
            put(labels.getLabel(LABEL_NULLABLE), schema.getNullable());
            put(labels.getLabel(LABEL_READ_ONLY), schema.getReadOnly());
            put(labels.getLabel(LABEL_WRITE_ONLY), schema.getWriteOnly());
            put(labels.getLabel(LABEL_UNIQUE_ITEMS), schema.getUniqueItems());
            put(labels.getLabel(LABEL_EXCLUSIVE_MAXIMUM), schema.getExclusiveMaximum());
            put(labels.getLabel(LABEL_EXCLUSIVE_MINIMUM), schema.getExclusiveMinimum());
        }};

        Map<String, Object> schemasValueProperties = new HashMap<String, Object>() {{
            put(labels.getLabel(LABEL_TITLE), schema.getTitle());
            put(labels.getLabel(LABEL_DEFAULT), schema.getDefault());
            put(labels.getLabel(LABEL_MAXIMUM), schema.getMaximum());
            put(labels.getLabel(LABEL_MINIMUM), schema.getMinimum());
            put(labels.getLabel(LABEL_MAX_LENGTH), schema.getMaxLength());
            put(labels.getLabel(LABEL_MIN_LENGTH), schema.getMinLength());
            put(labels.getLabel(LABEL_MAX_ITEMS), schema.getMaxItems());
            put(labels.getLabel(LABEL_MIN_ITEMS), schema.getMinItems());
            put(labels.getLabel(LABEL_MAX_PROPERTIES), schema.getMaxProperties());
            put(labels.getLabel(LABEL_MIN_PROPERTIES), schema.getMinProperties());
            put(labels.getLabel(LABEL_MULTIPLE_OF), schema.getMultipleOf());
        }};

        Stream<String> schemaBooleanStream = schemasBooleanProperties.entrySet().stream()
                .filter(e -> null != e.getValue() && e.getValue())
                .map(e -> OpenApiHelpers.italicUnconstrained(e.getKey().toLowerCase()));
        Stream<String> schemaValueStream = schemasValueProperties.entrySet().stream()
                .filter(e -> null != e.getValue() && StringUtils.isNotBlank(e.getValue().toString()))
                .map(e -> boldUnconstrained(e.getKey()) + ": " + e.getValue());

        ParagraphBlockImpl paragraphBlock = new ParagraphBlockImpl(schemaDocument);
        String source = Stream.concat(schemaBooleanStream, schemaValueStream).collect(Collectors.joining(" +" + LINE_SEPARATOR));
        paragraphBlock.setSource(source);

        schemaDocument.append(paragraphBlock);

        Map<String, Schema> properties = schema.getProperties();
        if (null != properties && !properties.isEmpty()) {
            PropertiesTableComponent propertiesTableComponent = new PropertiesTableComponent(context);
            propertiesTableComponent.apply(schemaDocument, properties, schema.getRequired());
        }

        return schemaDocument;
    }

    @SuppressWarnings("rawtypes")
    public static class Parameters {

        private final Schema schema;

        public Parameters(Schema schema) {
            this.schema = schema;
        }
    }
}
