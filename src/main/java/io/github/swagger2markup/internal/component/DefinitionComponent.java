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


import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.ObjectTypePolymorphism;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.ModelUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.DefinitionsDocumentExtension;
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Model;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;

import static io.github.swagger2markup.Labels.*;
import static io.github.swagger2markup.internal.utils.InlineSchemaUtils.createInlineType;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static io.github.swagger2markup.spi.DefinitionsDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DefinitionComponent extends MarkupComponent<DefinitionComponent.Parameters> {

    /* Discriminator is only displayed for inheriting definitions */
    private static final boolean ALWAYS_DISPLAY_DISCRIMINATOR = false;

    private final Map<String, Model> definitions;
    private final Map<ObjectTypePolymorphism.Nature, String> POLYMORPHISM_NATURE;
    private final DocumentResolver definitionsDocumentResolver;
    private PropertiesTableComponent propertiesTableComponent;

    public DefinitionComponent(Swagger2MarkupConverter.Context context,
                               DocumentResolver definitionsDocumentResolver) {
        super(context);
        this.definitions = context.getSwagger().getDefinitions();
        this.definitionsDocumentResolver = definitionsDocumentResolver;
        POLYMORPHISM_NATURE = new HashMap<ObjectTypePolymorphism.Nature, String>() {{
            put(ObjectTypePolymorphism.Nature.COMPOSITION, labels.getLabel(POLYMORPHISM_NATURE_COMPOSITION));
            put(ObjectTypePolymorphism.Nature.INHERITANCE, labels.getLabel(POLYMORPHISM_NATURE_INHERITANCE));
        }};
        propertiesTableComponent = new PropertiesTableComponent(context, definitionsDocumentResolver);
    }

    public static DefinitionComponent.Parameters parameters(String definitionName,
                                                            Model model,
                                                            int titleLevel) {
        return new DefinitionComponent.Parameters(definitionName, model, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        String definitionName = params.definitionName;
        Model model = params.model;
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(Position.DEFINITION_BEFORE, markupDocBuilder, definitionName, model));
        markupDocBuilder.sectionTitleWithAnchorLevel(params.titleLevel, definitionName, definitionName);
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(Position.DEFINITION_BEGIN, markupDocBuilder, definitionName, model));
        String description = model.getDescription();
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(markupDescription(config.getSwaggerMarkupLanguage(), markupDocBuilder, description));
        }
        inlineDefinitions(markupDocBuilder, typeSection(markupDocBuilder, definitionName, model), definitionName);
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(Position.DEFINITION_END, markupDocBuilder, definitionName, model));
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(Position.DEFINITION_AFTER, markupDocBuilder, definitionName, model));

        return markupDocBuilder;
    }

    /**
     * Builds the title of an inline schema.
     * Inline definitions should never been referenced in TOC because they have no real existence, so they are just text.
     *
     * @param title      inline schema title
     * @param anchor     inline schema anchor
     * @param docBuilder the docbuilder do use for output
     */
    private void addInlineDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.anchor(anchor, null);
        docBuilder.newLine();
        docBuilder.boldTextLine(title);
    }

    /**
     * Builds inline schema definitions
     *
     * @param markupDocBuilder the docbuilder do use for output
     * @param definitions      all inline definitions to display
     * @param uniquePrefix     unique prefix to prepend to inline object names to enforce unicity
     */
    private void inlineDefinitions(MarkupDocBuilder markupDocBuilder, List<ObjectType> definitions, String uniquePrefix) {
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (ObjectType definition : definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), markupDocBuilder);

                List<ObjectType> localDefinitions = new ArrayList<>();
                propertiesTableComponent.apply(markupDocBuilder, new PropertiesTableComponent.Parameters(definition.getProperties(), uniquePrefix, localDefinitions));
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(markupDocBuilder, Collections.singletonList(localDefinition), localDefinition.getUniqueName());
            }
        }
    }

    /**
     * Builds the type informations of a definition
     *
     * @param markupDocBuilder the docbuilder do use for output
     * @param definitionName   name of the definition to display
     * @param model            model of the definition to display
     * @return a list of inlined types.
     */
    private List<ObjectType> typeSection(MarkupDocBuilder markupDocBuilder, String definitionName, Model model) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();
        Type modelType = ModelUtils.resolveRefType(ModelUtils.getType(model, definitions, definitionsDocumentResolver));

        if (!(modelType instanceof ObjectType) && config.isInlineSchemaEnabled()) {
            modelType = createInlineType(modelType, definitionName, definitionName + " " + "inline", inlineDefinitions);
        }

        if (modelType instanceof ObjectType) {
            ObjectType objectType = (ObjectType) modelType;
            MarkupDocBuilder typeInfos = copyMarkupDocBuilder(markupDocBuilder);
            switch (objectType.getPolymorphism().getNature()) {
                case COMPOSITION:
                    typeInfos.italicText(labels.getLabel(POLYMORPHISM_COLUMN)).textLine(COLON + POLYMORPHISM_NATURE.get(objectType.getPolymorphism().getNature()));
                    break;
                case INHERITANCE:
                    typeInfos.italicText(labels.getLabel(POLYMORPHISM_COLUMN)).textLine(COLON + POLYMORPHISM_NATURE.get(objectType.getPolymorphism().getNature()));
                    typeInfos.italicText(labels.getLabel(POLYMORPHISM_DISCRIMINATOR_COLUMN)).textLine(COLON + objectType.getPolymorphism().getDiscriminator());
                    break;
                case NONE:
                    if (ALWAYS_DISPLAY_DISCRIMINATOR && isNotBlank(objectType.getPolymorphism().getDiscriminator()))
                        typeInfos.italicText(labels.getLabel(POLYMORPHISM_DISCRIMINATOR_COLUMN)).textLine(COLON + objectType.getPolymorphism().getDiscriminator());
                    break;
                default:
                    break;
            }

            String typeInfosString = typeInfos.toString();
            if (isNotBlank(typeInfosString))
                markupDocBuilder.paragraph(typeInfosString, true);

            propertiesTableComponent.apply(markupDocBuilder,
                    PropertiesTableComponent.parameters(
                            ((ObjectType) modelType).getProperties(),
                            definitionName,
                            inlineDefinitions));
        } else if (modelType != null) {
            MarkupDocBuilder typeInfos = copyMarkupDocBuilder(markupDocBuilder);
            typeInfos.italicText(labels.getLabel(TYPE_COLUMN)).textLine(COLON + modelType.displaySchema(markupDocBuilder));

            markupDocBuilder.paragraph(typeInfos.toString());
        }

        return inlineDefinitions;
    }

    /**
     * Apply extension context to all DefinitionsContentExtension
     *
     * @param context context
     */
    private void applyDefinitionsDocumentExtension(DefinitionsDocumentExtension.Context context) {
        extensionRegistry.getDefinitionsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final String definitionName;
        private final Model model;
        private final int titleLevel;

        public Parameters(String definitionName,
                          Model model,
                          int titleLevel) {
            this.definitionName = Validate.notBlank(definitionName, "DefinitionName must not be empty");
            this.model = Validate.notNull(model, "Model must not be null");
            this.titleLevel = titleLevel;
        }
    }


}
