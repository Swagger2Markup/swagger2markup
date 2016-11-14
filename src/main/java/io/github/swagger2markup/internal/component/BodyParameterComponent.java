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


import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.ParameterUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.Model;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.swagger2markup.internal.Labels.*;
import static io.github.swagger2markup.internal.utils.InlineSchemaUtils.createInlineType;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BodyParameterComponent extends MarkupComponent<BodyParameterComponent.Parameters> {

    private final DefinitionDocumentResolver definitionDocumentResolver;
    private final Map<String, Model> definitions;
    private final PropertiesTableComponent propertiesTableComponent;

    public BodyParameterComponent(Swagger2MarkupConverter.Context context,
                                  DefinitionDocumentResolver definitionDocumentResolver){
        super(context);
        this.definitions = context.getSwagger().getDefinitions();

        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DefinitionDocumentResolver must not be null");
        this.propertiesTableComponent = new PropertiesTableComponent(context, definitionDocumentResolver);
    }

    public static BodyParameterComponent.Parameters parameters(PathOperation operation,
                                                               List<ObjectType> inlineDefinitions){
        return new BodyParameterComponent.Parameters(operation, inlineDefinitions);
    }

    public static class Parameters{
        private PathOperation operation;
        private final List<ObjectType> inlineDefinitions;
        public Parameters(PathOperation operation,
                          List<ObjectType> inlineDefinitions){
            Validate.notNull(operation, "Operation must not be null");
            this.operation = operation;
            this.inlineDefinitions = inlineDefinitions;
        }
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        PathOperation operation = params.operation;
        if (config.isFlatBodyEnabled()) {
            List<Parameter> parameters = operation.getOperation().getParameters();
            if (CollectionUtils.isNotEmpty(parameters)) {
                for (Parameter parameter : parameters) {
                    if (StringUtils.equals(parameter.getIn(), "body")) {
                        Type type = ParameterUtils.getType(parameter, definitions, definitionDocumentResolver);

                        if (!(type instanceof ObjectType)) {
                            if (config.isInlineSchemaEnabled()) {
                                type = createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), params.inlineDefinitions);
                            }
                        }

                        buildSectionTitle(markupDocBuilder, labels.getString(BODY_PARAMETER));
                        String description = parameter.getDescription();
                        if (isNotBlank(description)) {
                            if (isNotBlank(description)) {
                                markupDocBuilder.paragraph(markupDescription(markupDocBuilder, description));
                            }
                        }

                        MarkupDocBuilder typeInfos = copyMarkupDocBuilder(markupDocBuilder);
                        typeInfos.italicText(labels.getString(NAME_COLUMN)).textLine(COLON + parameter.getName());
                        typeInfos.italicText(labels.getString(FLAGS_COLUMN)).textLine(COLON + (BooleanUtils.isTrue(parameter.getRequired()) ? labels.getString(FLAGS_REQUIRED).toLowerCase() : labels.getString(FLAGS_OPTIONAL).toLowerCase()));

                        if (!(type instanceof ObjectType)) {
                            typeInfos.italicText(labels.getString(TYPE_COLUMN)).textLine(COLON + type.displaySchema(markupDocBuilder));
                        }

                        markupDocBuilder.paragraph(typeInfos.toString(), true);

                        if (type instanceof ObjectType) {
                            List<ObjectType> localDefinitions = new ArrayList<>();

                            propertiesTableComponent.apply(markupDocBuilder, PropertiesTableComponent.parameters(
                                    ((ObjectType) type).getProperties(),
                                    operation.getId(),
                                    localDefinitions
                            ));
                        }
                    }
                }
            }
        }
        return markupDocBuilder;
    }

    private void buildSectionTitle(MarkupDocBuilder markupDocBuilder, String title) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleLevel3(title);
        } else {
            markupDocBuilder.sectionTitleLevel4(title);
        }
    }
}
