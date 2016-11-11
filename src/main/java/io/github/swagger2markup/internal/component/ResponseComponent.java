/*
 * Copyright 2016 Robert Winkler
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


import ch.netzwerg.paleo.StringColumn;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.PropertyWrapper;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.Response;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.internal.component.Labels.*;
import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ResponseComponent extends MarkupComponent {

    private final PathOperation operation;
    private final int titleLevel;
    private final DefinitionDocumentResolver definitionDocumentResolver;
    private final List<ObjectType> inlineDefinitions;

    public ResponseComponent(Context context,
                             PathOperation operation,
                             DefinitionDocumentResolver definitionDocumentResolver,
                             List<ObjectType> inlineDefinitions,
                             int titleLevel){
        super(context);
        this.operation = Validate.notNull(operation, "PathOperation must not be null");
        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DefinitionDocumentResolver must not be null");
        this.inlineDefinitions = Validate.notNull(inlineDefinitions, "InlineDefinitions must not be null");
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        Map<String, Response> responses = operation.getOperation().getResponses();

        MarkupDocBuilder responsesBuilder = copyMarkupDocBuilder();
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_BEGIN, responsesBuilder, operation));
        if (MapUtils.isNotEmpty(responses)) {
            StringColumn.Builder httpCodeColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getString(Labels.HTTP_CODE_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getString(DESCRIPTION_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "14")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder schemaColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getString(SCHEMA_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "4")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");

            Map<String, Response> sortedResponses = toSortedMap(responses, config.getResponseOrdering());
            sortedResponses.forEach((String responseName, Response response) -> {
                String schemaContent = labels.getString(NO_CONTENT);
                if (response.getSchema() != null) {
                    Property property = response.getSchema();
                    Type type = new PropertyWrapper(property).getType(definitionDocumentResolver);

                    type = createInlineType(type, labels.getString(Labels.RESPONSE) + " " + responseName, operation.getId() + " " + labels.getString(Labels.RESPONSE) + " " + responseName, inlineDefinitions);

                    schemaContent = type.displaySchema(markupDocBuilder);
                }

                MarkupDocBuilder descriptionBuilder = copyMarkupDocBuilder();

                descriptionBuilder.text(markupDescription(response.getDescription()));

                Map<String, Property> headers = response.getHeaders();
                if (MapUtils.isNotEmpty(headers)) {
                    descriptionBuilder.newLine(true).boldText(labels.getString(Labels.HEADERS_COLUMN)).text(COLON);
                    for (Map.Entry<String, Property> header : headers.entrySet()) {
                        descriptionBuilder.newLine(true);
                        Property headerProperty = header.getValue();
                        PropertyWrapper headerPropertyWrapper = new PropertyWrapper(headerProperty);
                        Type propertyType = headerPropertyWrapper.getType(null);
                        String headerDescription = markupDescription(headerProperty.getDescription());
                        Optional<Object> optionalDefaultValue = headerPropertyWrapper.getDefaultValue();

                        descriptionBuilder
                                .literalText(header.getKey())
                                .text(String.format(" (%s)", propertyType.displaySchema(markupDocBuilder)));

                        if (isNotBlank(headerDescription) || optionalDefaultValue.isPresent()) {
                            descriptionBuilder.text(COLON);

                            if (isNotBlank(headerDescription) && !headerDescription.endsWith("."))
                                headerDescription += ".";

                            descriptionBuilder.text(headerDescription);

                            if (optionalDefaultValue.isPresent()) {
                                descriptionBuilder.text(" ").boldText(labels.getString(DEFAULT_COLUMN)).text(COLON).literalText(Json.pretty(optionalDefaultValue.get()));
                            }
                        }
                    }
                }

                httpCodeColumnBuilder.add(boldText(responseName));
                descriptionColumnBuilder.add(descriptionBuilder.toString());
                schemaColumnBuilder.add(schemaContent);
            });

            responsesBuilder= new TableComponent(new MarkupComponent.Context(config, responsesBuilder, extensionRegistry),
                    httpCodeColumnBuilder.build(),
                    descriptionColumnBuilder.build(),
                    schemaColumnBuilder.build()).render();
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_END, responsesBuilder, operation));
        String responsesContent = responsesBuilder.toString();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_BEFORE, markupDocBuilder, operation));
        if (isNotBlank(responsesContent)) {
            markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(Labels.RESPONSES));
            markupDocBuilder.text(responsesContent);
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_AFTER, markupDocBuilder, operation));
        return markupDocBuilder;
    }

    /**
     * Apply extension context to all OperationsContentExtension.
     *
     * @param context context
     */
    private void applyPathsDocumentExtension(PathsDocumentExtension.Context context) {
        extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.apply(context));
    }
}
