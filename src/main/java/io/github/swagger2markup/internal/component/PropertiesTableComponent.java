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

import ch.netzwerg.paleo.ColumnIds;
import ch.netzwerg.paleo.StringColumn;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.PropertyWrapper;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.swagger2markup.internal.component.Labels.*;
import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class PropertiesTableComponent extends MarkupComponent{

    private final Map<String, Property> properties;
    private final String uniquePrefix;
    private final DefinitionDocumentResolver definitionDocumentResolver;
    private final List<ObjectType> inlineDefinitions;

    /**
     * Build a generic property table
     *
     * @param properties                 properties to display
     * @param uniquePrefix               unique prefix to prepend to inline object names to enforce unicity
     * @param definitionDocumentResolver definition document resolver to apply to property type cross-reference
     */
    public PropertiesTableComponent(Context context,
                                    Map<String, Property> properties,
                                    String uniquePrefix,
                                    DefinitionDocumentResolver definitionDocumentResolver,
                                    List<ObjectType> inlineDefinitions){
        super(context);
        this.properties = Validate.notNull(properties, "Properties must not be null");
        this.uniquePrefix = uniquePrefix;
        this.definitionDocumentResolver = definitionDocumentResolver;
        this.inlineDefinitions = inlineDefinitions;

    }

    public MarkupDocBuilder render() {
        StringColumn.Builder nameColumnBuilder = StringColumn.builder(ColumnIds.StringColumnId.of(labels.getString(NAME_COLUMN)))
                .putMetaData(TableComponent.WIDTH_RATIO, "3");
        StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(ColumnIds.StringColumnId.of(labels.getString(DESCRIPTION_COLUMN)))
                .putMetaData(TableComponent.WIDTH_RATIO, "11")
                .putMetaData(TableComponent.HEADER_COLUMN, "true");
        StringColumn.Builder schemaColumnBuilder = StringColumn.builder(ColumnIds.StringColumnId.of(labels.getString(SCHEMA_COLUMN)))
                .putMetaData(TableComponent.WIDTH_RATIO, "4")
                .putMetaData(TableComponent.HEADER_COLUMN, "true");
        if (MapUtils.isNotEmpty(properties)) {
            Map<String, Property> sortedProperties = toSortedMap(properties, config.getPropertyOrdering());
            sortedProperties.forEach((String propertyName, Property property) -> {
                PropertyWrapper propertyWrapper = new PropertyWrapper(property);
                Type propertyType = propertyWrapper.getType(definitionDocumentResolver);

                propertyType = createInlineType(propertyType, propertyName, uniquePrefix + " " + propertyName, inlineDefinitions);

                Optional<Object> optionalExample = propertyWrapper.getExample(config.isGeneratedExamplesEnabled(), markupDocBuilder);
                Optional<Object> optionalDefaultValue = propertyWrapper.getDefaultValue();
                Optional<Integer> optionalMaxLength = propertyWrapper.getMaxlength();
                Optional<Integer> optionalMinLength = propertyWrapper.getMinlength();
                Optional<String> optionalPattern = propertyWrapper.getPattern();
                Optional<Number> optionalMinValue = propertyWrapper.getMin();
                boolean exclusiveMin = propertyWrapper.getExclusiveMin();
                Optional<Number> optionalMaxValue = propertyWrapper.getMax();
                boolean exclusiveMax = propertyWrapper.getExclusiveMax();

                MarkupDocBuilder propertyNameContent = copyMarkupDocBuilder();
                propertyNameContent.boldTextLine(propertyName, true);
                if (property.getRequired())
                    propertyNameContent.italicText(labels.getString(FLAGS_REQUIRED).toLowerCase());
                else
                    propertyNameContent.italicText(labels.getString(FLAGS_OPTIONAL).toLowerCase());
                if (propertyWrapper.getReadOnly()) {
                    propertyNameContent.newLine(true);
                    propertyNameContent.italicText(labels.getString(FLAGS_READ_ONLY).toLowerCase());
                }

                MarkupDocBuilder descriptionContent = copyMarkupDocBuilder();
                String description = markupDescription(property.getDescription());
                if (isNotBlank(description))
                    descriptionContent.text(description);

                if (optionalDefaultValue.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    descriptionContent.boldText(labels.getString(DEFAULT_COLUMN)).text(COLON).literalText(Json.pretty(optionalDefaultValue.get()));
                }

                if (optionalMinLength.isPresent() && optionalMaxLength.isPresent()) {
                    // combination of minlength/maxlength
                    Integer minLength = optionalMinLength.get();
                    Integer maxLength = optionalMaxLength.get();

                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }

                    String lengthRange = minLength + " - " + maxLength;
                    if (minLength.equals(maxLength)) {
                        lengthRange = minLength.toString();
                    }

                    descriptionContent.boldText(labels.getString(LENGTH_COLUMN)).text(COLON).literalText(lengthRange);

                } else {
                    if (optionalMinLength.isPresent()) {
                        if (isNotBlank(descriptionContent.toString())) {
                            descriptionContent.newLine(true);
                        }
                        descriptionContent.boldText(labels.getString(MINLENGTH_COLUMN)).text(COLON).literalText(optionalMinLength.get().toString());
                    }

                    if (optionalMaxLength.isPresent()) {
                        if (isNotBlank(descriptionContent.toString())) {
                            descriptionContent.newLine(true);
                        }
                        descriptionContent.boldText(labels.getString(MAXLENGTH_COLUMN)).text(COLON).literalText(optionalMaxLength.get().toString());
                    }
                }

                if (optionalPattern.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    descriptionContent.boldText(labels.getString(PATTERN_COLUMN)).text(COLON).literalText(Json.pretty(optionalPattern.get()));
                }

                if (optionalMinValue.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    String minValueColumn = exclusiveMin ? labels.getString(MINVALUE_EXCLUSIVE_COLUMN) : labels.getString(MINVALUE_COLUMN);
                    descriptionContent.boldText(minValueColumn).text(COLON).literalText(optionalMinValue.get().toString());
                }

                if (optionalMaxValue.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    String maxValueColumn = exclusiveMax ? labels.getString(MAXVALUE_EXCLUSIVE_COLUMN) : labels.getString(MAXVALUE_COLUMN);
                    descriptionContent.boldText(maxValueColumn).text(COLON).literalText(optionalMaxValue.get().toString());
                }

                if (optionalExample.isPresent()) {
                    if (isNotBlank(description) || optionalDefaultValue.isPresent()) {
                        descriptionContent.newLine(true);
                    }
                    descriptionContent.boldText(labels.getString(EXAMPLE_COLUMN)).text(COLON).literalText(Json.pretty(optionalExample.get()));
                }

                nameColumnBuilder.add(propertyNameContent.toString());
                descriptionColumnBuilder.add(descriptionContent.toString());
                schemaColumnBuilder.add(propertyType.displaySchema(markupDocBuilder));
            });
        }

        return new TableComponent(new MarkupComponent.Context(config, markupDocBuilder, extensionRegistry),
                nameColumnBuilder.build(),
                descriptionColumnBuilder.build(),
                schemaColumnBuilder.build()).render();
    }
}
