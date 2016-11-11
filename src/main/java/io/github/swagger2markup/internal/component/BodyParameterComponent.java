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

import static io.github.swagger2markup.internal.component.Labels.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BodyParameterComponent extends MarkupComponent {

    private final PathOperation operation;
    private final DefinitionDocumentResolver definitionDocumentResolver;
    private final Map<String, Model> definitions;
    private final List<ObjectType> inlineDefinitions;

    public BodyParameterComponent(Context context,
                                  PathOperation operation,
                                  Map<String, Model> definitions,
                                  DefinitionDocumentResolver definitionDocumentResolver,
                                  List<ObjectType> inlineDefinitions){
        super(context);
        this.operation = Validate.notNull(operation, "PathOperation must not be null");
        this.definitions = definitions;
        this.inlineDefinitions = inlineDefinitions;
        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DefinitionDocumentResolver must not be null");
    }

    @Override
    public MarkupDocBuilder render() {
        if (config.isFlatBodyEnabled()) {
            List<Parameter> parameters = operation.getOperation().getParameters();
            if (CollectionUtils.isNotEmpty(parameters)) {
                for (Parameter parameter : parameters) {
                    if (StringUtils.equals(parameter.getIn(), "body")) {
                        Type type = ParameterUtils.getType(parameter, definitions, definitionDocumentResolver);

                        if (!(type instanceof ObjectType)) {
                            type = createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), inlineDefinitions);
                        }

                        buildSectionTitle(labels.getString(BODY_PARAMETER));
                        String description = parameter.getDescription();
                        if (isNotBlank(description)) {
                            if (isNotBlank(description)) {
                                markupDocBuilder.paragraph(markupDescription(description));
                            }
                        }

                        MarkupDocBuilder typeInfos = copyMarkupDocBuilder();
                        typeInfos.italicText(labels.getString(NAME_COLUMN)).textLine(COLON + parameter.getName());
                        typeInfos.italicText(labels.getString(FLAGS_COLUMN)).textLine(COLON + (BooleanUtils.isTrue(parameter.getRequired()) ? labels.getString(FLAGS_REQUIRED).toLowerCase() : labels.getString(FLAGS_OPTIONAL).toLowerCase()));

                        if (!(type instanceof ObjectType)) {
                            typeInfos.italicText(labels.getString(TYPE_COLUMN)).textLine(COLON + type.displaySchema(markupDocBuilder));
                        }

                        markupDocBuilder.paragraph(typeInfos.toString(), true);

                        if (type instanceof ObjectType) {
                            List<ObjectType> localDefinitions = new ArrayList<>();
                            new PropertiesTableComponent(
                                    context,
                                    ((ObjectType) type).getProperties(),
                                    operation.getId(),
                                    definitionDocumentResolver,
                                    localDefinitions).render();

                            inlineDefinitions.addAll(localDefinitions);
                        }
                    }
                }
            }
        }
        return markupDocBuilder;
    }

    /**
     * Adds a operation section title to the document.
     *
     * @param title      the section title
     */
    private void buildSectionTitle(String title) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleLevel3(title);
        } else {
            markupDocBuilder.sectionTitleLevel4(title);
        }
    }
}
