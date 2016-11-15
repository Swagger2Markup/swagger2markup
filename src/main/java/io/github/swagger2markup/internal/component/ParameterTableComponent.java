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
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Labels;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.ParameterAdapter;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.MarkupComponent;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.Model;
import io.swagger.models.parameters.Parameter;
import io.swagger.util.Json;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.Labels.*;
import static io.github.swagger2markup.internal.utils.InlineSchemaUtils.createInlineType;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ParameterTableComponent extends MarkupComponent<ParameterTableComponent.Parameters> {


    private final DefinitionDocumentResolver definitionDocumentResolver;
    private final Map<String, Model> definitions;
    private final TableComponent tableComponent;

    public ParameterTableComponent(Swagger2MarkupConverter.Context context,
                                   DefinitionDocumentResolver definitionDocumentResolver){
        super(context);
        this.definitions = context.getSwagger().getDefinitions();
        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DefinitionDocumentResolver must not be null");
        this.tableComponent = new TableComponent(context);

    }

    public static ParameterTableComponent.Parameters parameters(PathOperation operation,
                                                                List<ObjectType> inlineDefinitions,
                                                                int titleLevel){
        return new ParameterTableComponent.Parameters(operation, inlineDefinitions, titleLevel);
    }

    public static class Parameters {
        private final PathOperation operation;
        private final int titleLevel;
        private final List<ObjectType> inlineDefinitions;

        public Parameters(PathOperation operation,
                          List<ObjectType> inlineDefinitions,
                          int titleLevel){
            this.operation = Validate.notNull(operation, "PathOperation must not be null");
            this.inlineDefinitions = Validate.notNull(inlineDefinitions, "InlineDefinitions must not be null");
            this.titleLevel = titleLevel;
        }
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        PathOperation operation = params.operation;
        List<ObjectType> inlineDefinitions = params.inlineDefinitions;
        List<Parameter> parameters = operation.getOperation().getParameters();
        if (config.getParameterOrdering() != null)
            Collections.sort(parameters, config.getParameterOrdering());

        // Filter parameters to display in parameters section
        List<Parameter> filteredParameters = parameters.stream()
                .filter(this::filterParameter).collect(Collectors.toList());

        MarkupDocBuilder parametersBuilder = copyMarkupDocBuilder(markupDocBuilder);
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_DESCRIPTION_BEGIN, parametersBuilder, operation));
        if (CollectionUtils.isNotEmpty(filteredParameters)) {
            StringColumn.Builder typeColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(TYPE_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2");
            StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(NAME_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "3");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(DESCRIPTION_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "9")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder schemaColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(SCHEMA_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "4")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder defaultColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(DEFAULT_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");

            for (Parameter parameter : filteredParameters) {
                ParameterAdapter parameterAdapter = new ParameterAdapter(parameter);

                Type type = parameterAdapter.getType(definitions, definitionDocumentResolver);
                if (config.isInlineSchemaEnabled()){
                    type = createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), inlineDefinitions);
                }

                typeColumnBuilder.add(boldText(markupDocBuilder, parameterAdapter.getIn()));
                nameColumnBuilder.add(getParameterNameColumnContent(markupDocBuilder, parameter));
                descriptionColumnBuilder.add(markupDescription(markupDocBuilder, parameter.getDescription()));
                schemaColumnBuilder.add(type.displaySchema(markupDocBuilder));
                defaultColumnBuilder.add(parameterAdapter.getDefaultValue().map(value -> literalText(markupDocBuilder, Json.pretty(value))).orElse(""));
            }

            parametersBuilder = tableComponent.apply(parametersBuilder, TableComponent.parameters(
                    typeColumnBuilder.build(),
                    nameColumnBuilder.build(),
                    descriptionColumnBuilder.build(),
                    schemaColumnBuilder.build(),
                    defaultColumnBuilder.build()));
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_DESCRIPTION_END, parametersBuilder, operation));
        String parametersContent = parametersBuilder.toString();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_PARAMETERS_BEFORE, markupDocBuilder, operation));
        if (isNotBlank(parametersContent)) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(Labels.PARAMETERS));
            markupDocBuilder.text(parametersContent);
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_PARAMETERS_AFTER, markupDocBuilder, operation));

        return markupDocBuilder;
    }

    private String getParameterNameColumnContent(MarkupDocBuilder markupDocBuilder, Parameter parameter){
        MarkupDocBuilder parameterNameContent = copyMarkupDocBuilder(markupDocBuilder);

        parameterNameContent.boldTextLine(parameter.getName(), true);
        if (parameter.getRequired())
            parameterNameContent.italicText(labels.getLabel(FLAGS_REQUIRED).toLowerCase());
        else
            parameterNameContent.italicText(labels.getLabel(FLAGS_OPTIONAL).toLowerCase());
        return parameterNameContent.toString();
    }

    /**
     * Filter parameters to display in parameters section
     *
     * @param parameter parameter to filter
     * @return true if parameter can be displayed
     */
    private boolean filterParameter(Parameter parameter) {
        return (!config.isFlatBodyEnabled() || !StringUtils.equals(parameter.getIn(), "body"));
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
