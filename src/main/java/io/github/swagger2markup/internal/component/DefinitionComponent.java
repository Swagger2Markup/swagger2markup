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


import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.ObjectTypePolymorphism;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.ModelUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.DefinitionsDocumentExtension;
import io.swagger.models.Model;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;

import static io.github.swagger2markup.internal.component.Labels.*;
import static io.github.swagger2markup.spi.DefinitionsDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DefinitionComponent extends MarkupComponent {

    /* Discriminator is only displayed for inheriting definitions */
    private static final boolean ALWAYS_DISPLAY_DISCRIMINATOR = false;

    private final Map<String, Model> definitions;
    private final String definitionName;
    private final Model model;
    private final int titleLevel;
    private final Map<ObjectTypePolymorphism.Nature, String> POLYMORPHISM_NATURE;
    private final DefinitionDocumentResolver definitionsDocumentResolver;

    public DefinitionComponent(Context context,
                               Map<String, Model> definitions,
                               String definitionName,
                               Model model,
                               DefinitionDocumentResolver definitionsDocumentResolver,
                               int titleLevel) {
        super(context);
        this.definitions = Validate.notNull(definitions, "Definitions must not be empty");
        this.definitionName = Validate.notBlank(definitionName, "DefinitionName must not be empty");
        this.model = Validate.notNull(model, "Model must not be null");
        this.titleLevel = titleLevel;
        this.definitionsDocumentResolver = definitionsDocumentResolver;

        POLYMORPHISM_NATURE = new HashMap<ObjectTypePolymorphism.Nature, String>() {{
            put(ObjectTypePolymorphism.Nature.COMPOSITION, labels.getString(Labels.POLYMORPHISM_NATURE_COMPOSITION));
            put(ObjectTypePolymorphism.Nature.INHERITANCE, labels.getString(Labels.POLYMORPHISM_NATURE_INHERITANCE));
        }};
    }

    @Override
    public MarkupDocBuilder render() {
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(Position.DEFINITION_BEFORE, markupDocBuilder, definitionName, model));
        markupDocBuilder.sectionTitleWithAnchorLevel(titleLevel, definitionName, definitionName);
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(Position.DEFINITION_BEGIN, markupDocBuilder, definitionName, model));
        String description = model.getDescription();
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(markupDescription(description));
        }
        inlineDefinitions(typeSection(definitionName, model, markupDocBuilder), definitionName, markupDocBuilder);
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
     * @param definitions  all inline definitions to display
     * @param uniquePrefix unique prefix to prepend to inline object names to enforce unicity
     * @param docBuilder   the docbuilder do use for output
     */
    private void inlineDefinitions(List<ObjectType> definitions, String uniquePrefix, MarkupDocBuilder docBuilder) {
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (ObjectType definition : definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), docBuilder);

                List<ObjectType> localDefinitions = new ArrayList<>();
                new PropertiesTableComponent(
                        new Context(config, docBuilder, extensionRegistry),
                        definition.getProperties(),
                        uniquePrefix,
                        definitionsDocumentResolver,
                        localDefinitions).render();
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), localDefinition.getUniqueName(), docBuilder);
            }
        }
    }

    /**
     * Builds the type informations of a definition
     *
     * @param definitionName name of the definition to display
     * @param model          model of the definition to display
     * @param docBuilder     the docbuilder do use for output
     * @return a list of inlined types.
     */
    private List<ObjectType> typeSection(String definitionName, Model model, MarkupDocBuilder docBuilder) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();
        Type modelType = ModelUtils.resolveRefType(ModelUtils.getType(model, definitions, definitionsDocumentResolver));

        if (!(modelType instanceof ObjectType)) {
            modelType = createInlineType(modelType, definitionName, definitionName + " " + "inline", inlineDefinitions);
        }

        if (modelType instanceof ObjectType) {
            ObjectType objectType = (ObjectType) modelType;
            MarkupDocBuilder typeInfos = copyMarkupDocBuilder();
            switch (objectType.getPolymorphism().getNature()) {
                case COMPOSITION:
                    typeInfos.italicText(labels.getString(Labels.POLYMORPHISM_COLUMN)).textLine(COLON + POLYMORPHISM_NATURE.get(objectType.getPolymorphism().getNature()));
                    break;
                case INHERITANCE:
                    typeInfos.italicText(labels.getString(POLYMORPHISM_COLUMN)).textLine(COLON + POLYMORPHISM_NATURE.get(objectType.getPolymorphism().getNature()));
                    typeInfos.italicText(labels.getString(POLYMORPHISM_DISCRIMINATOR_COLUMN)).textLine(COLON + objectType.getPolymorphism().getDiscriminator());
                    break;
                case NONE:
                    if (ALWAYS_DISPLAY_DISCRIMINATOR) {
                        if (StringUtils.isNotBlank(objectType.getPolymorphism().getDiscriminator()))
                            typeInfos.italicText(labels.getString(POLYMORPHISM_DISCRIMINATOR_COLUMN)).textLine(COLON + objectType.getPolymorphism().getDiscriminator());
                    }

                default: break;
            }

            String typeInfosString = typeInfos.toString();
            if (StringUtils.isNotBlank(typeInfosString))
                docBuilder.paragraph(typeInfosString, true);

            new PropertiesTableComponent(
                    new Context(config, docBuilder, extensionRegistry),
                    ((ObjectType) modelType).getProperties(),
                    definitionName,
                    definitionsDocumentResolver,
                    inlineDefinitions).render();
        } else if (modelType != null) {
            MarkupDocBuilder typeInfos = copyMarkupDocBuilder();
            typeInfos.italicText(labels.getString(TYPE_COLUMN)).textLine(COLON + modelType.displaySchema(docBuilder));

            docBuilder.paragraph(typeInfos.toString());
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


}
