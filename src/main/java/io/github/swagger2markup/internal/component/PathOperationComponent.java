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
import io.github.swagger2markup.internal.utils.ExamplesUtil;
import io.github.swagger2markup.markup.builder.MarkupAdmonition;
import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.Model;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.util.Json;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.swagger2markup.internal.component.Labels.*;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PathOperationComponent extends MarkupComponent {

    private final PathOperation operation;
    private final DefinitionDocumentResolver definitionDocumentResolver;
    private final DefinitionDocumentResolver securityDocumentResolver;
    private final Map<String, Model> definitions;
    private final Map<String, SecuritySchemeDefinition> securityDefinitions;

    public PathOperationComponent(Context context,
                                  PathOperation operation,
                                  Map<String, Model> definitions,
                                  Map<String, SecuritySchemeDefinition> securityDefinitions,
                                  DefinitionDocumentResolver definitionDocumentResolver,
                                  DefinitionDocumentResolver securityDocumentResolver){
        super(context);
        this.operation = Validate.notNull(operation, "PathOperation must not be null");
        this.definitions = definitions;
        this.securityDefinitions = securityDefinitions;
        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DefinitionDocumentResolver must not be null");
        this.securityDocumentResolver = Validate.notNull(securityDocumentResolver, "SecurityDocumentResolver must not be null");
    }

    @Override
    public MarkupDocBuilder render() {
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_BEFORE, markupDocBuilder, operation));
        buildOperationTitle(operation);
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_BEGIN, markupDocBuilder, operation));
        buildDeprecatedSection(operation);
        buildDescriptionSection(operation);
        inlineDefinitions(buildParametersSection(operation), operation.getPath() + " " + operation.getMethod(), markupDocBuilder);
        inlineDefinitions(buildBodyParameterSection(operation), operation.getPath() + " " + operation.getMethod(), markupDocBuilder);
        inlineDefinitions(buildResponsesSection(operation), operation.getPath() + " " + operation.getMethod(), markupDocBuilder);
        buildConsumesSection(operation);
        buildProducesSection(operation);
        buildTagsSection(operation);
        buildSecuritySchemeSection(operation);
        buildExamplesSection(operation);
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_END, markupDocBuilder, operation));
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_AFTER, markupDocBuilder, operation));
        return markupDocBuilder;
    }


    /**
     * Adds the operation title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the operation.
     *
     * @param operation  the Swagger Operation
     */
    private void buildOperationTitle(PathOperation operation) {
        buildOperationTitle(operation.getTitle(), operation.getId());
        if (operation.getTitle().equals(operation.getOperation().getSummary())) {
            markupDocBuilder.block(operation.getMethod() + " " + operation.getPath(), MarkupBlockStyle.LITERAL);
        }
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title      the operation title
     * @param anchor     optional anchor (null => auto-generate from title)
     */
    private void buildOperationTitle(String title, String anchor) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            markupDocBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }

    /**
     * Builds a warning if method is deprecated.
     *
     * @param operation  the Swagger Operation
     */
    private void buildDeprecatedSection(PathOperation operation) {
        if (BooleanUtils.isTrue(operation.getOperation().isDeprecated())) {
            markupDocBuilder.block(DEPRECATED_OPERATION, MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.CAUTION);
        }
    }

    /**
     * Adds a operation description to the document.
     *
     * @param operation  the Swagger Operation
     */
    private void buildDescriptionSection(PathOperation operation) {
        MarkupDocBuilder descriptionBuilder = copyMarkupDocBuilder();
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_BEGIN, descriptionBuilder, operation));
        String description = operation.getOperation().getDescription();
        if (isNotBlank(description)) {
            descriptionBuilder.paragraph(markupDescription(description));
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_END, descriptionBuilder, operation));
        String descriptionContent = descriptionBuilder.toString();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_BEFORE, markupDocBuilder, operation));
        if (isNotBlank(descriptionContent)) {
            buildSectionTitle(labels.getString(Labels.DESCRIPTION));
            markupDocBuilder.text(descriptionContent);
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_AFTER, markupDocBuilder, operation));
    }

    /**
     * Builds the parameters section
     *
     * @param operation  the Swagger Operation
     */
    private List<ObjectType> buildParametersSection(PathOperation operation) {

        List<ObjectType> inlineDefinitions = new ArrayList<>();

        new ParameterTableComponent(context,
                operation,
                definitions,
                definitionDocumentResolver,
                inlineDefinitions,
                getSectionTitleLevel()
        ).render();

        return inlineDefinitions;
    }

    /**
     * Builds the body parameter section
     *
     * @param operation  the Swagger Operation
     * @return a list of inlined types.
     */
    private List<ObjectType> buildBodyParameterSection(PathOperation operation) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();
        new BodyParameterComponent(context,
                operation,
                definitions,
                definitionDocumentResolver,
                inlineDefinitions
        ).render();

        return inlineDefinitions;
    }

    private List<ObjectType> buildResponsesSection(PathOperation operation) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        new ResponseComponent(
                context,
                operation,
                definitionDocumentResolver,
                inlineDefinitions,
                getSectionTitleLevel()
        ).render();

        return inlineDefinitions;
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



    /**
     * Builds the title of an inline schema.
     * Inline definitions should never been referenced in TOC because they have no real existence, so they are just text.
     *
     * @param title      inline schema title
     * @param anchor     inline schema anchor
     * @param docBuilder the docbuilder do use for output
     */
    private void addInlineDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.anchor(anchor);
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
                                definitionDocumentResolver,
                                localDefinitions).render();
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), localDefinition.getUniqueName(), docBuilder);
            }
        }

    }

    private void buildConsumesSection(PathOperation operation) {
        List<String> consumes = operation.getOperation().getConsumes();
        if (CollectionUtils.isNotEmpty(consumes)) {
            new ConsumesComponent(context,
                    consumes,
                    getSectionTitleLevel())
                    .render();
        }

    }

    private void buildProducesSection(PathOperation operation) {
        List<String> produces = operation.getOperation().getProduces();
        if (CollectionUtils.isNotEmpty(produces)) {
            new ProducesComponent(context,
                    produces,
                    getSectionTitleLevel())
                    .render();
        }
    }

    private void buildTagsSection(PathOperation operation) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            List<String> tags = operation.getOperation().getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                buildSectionTitle(labels.getString(TAGS));
                if (config.getTagOrdering() != null) {
                    Collections.sort(tags, config.getTagOrdering());
                }
                markupDocBuilder.unorderedList(tags);
            }
        }
    }

    /**
     * Builds the security section of a Swagger Operation.
     *
     * @param operation  the Swagger Operation
     */
    private void buildSecuritySchemeSection(PathOperation operation) {
        if (config.isPathSecuritySectionEnabled()) {
            new SecuritySchemeComponent(
                    context,
                    operation,
                    securityDefinitions,
                    securityDocumentResolver,
                    getSectionTitleLevel()
            ).render();
        }
    }

    /**
     * Retrieves the title level for sections
     */
    private int getSectionTitleLevel() {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            return 3;
        } else {
            return 4;
        }
    }


    /**
     * Builds the example section of a Swagger Operation.
     *
     * @param operation  the Swagger Operation
     */
    private void buildExamplesSection(PathOperation operation) {

        Map<String, Object> generatedRequestExampleMap = ExamplesUtil.generateRequestExampleMap(config.isGeneratedExamplesEnabled(), operation, definitions, definitionDocumentResolver, markupDocBuilder);
        Map<String, Object> generatedResponseExampleMap = ExamplesUtil.generateResponseExampleMap(config.isGeneratedExamplesEnabled(), operation, definitions, definitionDocumentResolver, markupDocBuilder);

        exampleMap(generatedRequestExampleMap, labels.getString(EXAMPLE_REQUEST), labels.getString(REQUEST));
        exampleMap(generatedResponseExampleMap, labels.getString(EXAMPLE_RESPONSE), labels.getString(RESPONSE));
    }

    private void exampleMap(Map<String, Object> exampleMap, String operationSectionTitle, String sectionTitle) {
        if (exampleMap.size() > 0) {
            buildSectionTitle(operationSectionTitle);
            for (Map.Entry<String, Object> entry : exampleMap.entrySet()) {
                buildExampleTitle(sectionTitle + " " + entry.getKey());
                markupDocBuilder.listingBlock(Json.pretty(entry.getValue()), "json");
            }
        }
    }

    /**
     * Adds a example title to the document.
     *
     * @param title      the section title
     */
    private void buildExampleTitle(String title) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleLevel4(title);
        } else {
            markupDocBuilder.sectionTitleLevel5(title);
        }
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
