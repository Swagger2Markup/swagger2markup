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


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.PageBreakLocations;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.utils.ExamplesUtil;
import io.github.swagger2markup.markup.builder.MarkupAdmonition;
import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.MarkupComponent;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.Model;
import io.swagger.util.Json;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.Map.Entry;

import static io.github.swagger2markup.Labels.*;
import static io.github.swagger2markup.PageBreakLocations.*;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PathOperationComponent extends MarkupComponent<PathOperationComponent.Parameters> {

    private final DocumentResolver definitionDocumentResolver;
    private final Map<String, Model> definitions;
    private final PropertiesTableComponent propertiesTableComponent;
    private final ParameterTableComponent parameterTableComponent;
    private final ConsumesComponent consumesComponent;
    private final ProducesComponent producesComponent;
    private final SecuritySchemeComponent securitySchemeComponent;
    private final BodyParameterComponent bodyParameterComponent;
    private final ResponseComponent responseComponent;

    public PathOperationComponent(Swagger2MarkupConverter.Context context,
                                  DocumentResolver definitionDocumentResolver,
                                  DocumentResolver securityDocumentResolver) {
        super(context);
        this.definitions = context.getSwagger().getDefinitions();
        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DocumentResolver must not be null");
        this.propertiesTableComponent = new PropertiesTableComponent(context, definitionDocumentResolver);
        this.parameterTableComponent = new ParameterTableComponent(context, definitionDocumentResolver);
        this.consumesComponent = new ConsumesComponent(context);
        this.producesComponent = new ProducesComponent(context);
        this.securitySchemeComponent = new SecuritySchemeComponent(context, securityDocumentResolver);
        this.bodyParameterComponent = new BodyParameterComponent(context, definitionDocumentResolver);
        this.responseComponent = new ResponseComponent(context, definitionDocumentResolver);
    }

    public static PathOperationComponent.Parameters parameters(PathOperation operation) {
        return new PathOperationComponent.Parameters(operation);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        PathOperation operation = params.operation;
        List<PageBreakLocations> locations = config.getPageBreakLocations();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_BEFORE, markupDocBuilder, operation));

        if (locations.contains(BEFORE_OPERATION)) markupDocBuilder.pageBreak();
        buildOperationTitle(markupDocBuilder, operation);

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_BEGIN, markupDocBuilder, operation));
        buildDeprecatedSection(markupDocBuilder, operation);

        if (locations.contains(BEFORE_OPERATION_DESCRIPTION)) markupDocBuilder.pageBreak();
        buildDescriptionSection(markupDocBuilder, operation);
        if (locations.contains(AFTER_OPERATION_DESCRIPTION)) markupDocBuilder.pageBreak();

        if (locations.contains(BEFORE_OPERATION_PARAMETERS)) markupDocBuilder.pageBreak();
        inlineDefinitions(markupDocBuilder, buildParametersSection(markupDocBuilder, operation), operation.getPath() + " " + operation.getMethod());
        if (locations.contains(AFTER_OPERATION_PARAMETERS)) markupDocBuilder.pageBreak();

        inlineDefinitions(markupDocBuilder, buildBodyParameterSection(markupDocBuilder, operation), operation.getPath() + " " + operation.getMethod());

        if (locations.contains(BEFORE_OPERATION_RESPONSES)) markupDocBuilder.pageBreak();
        inlineDefinitions(markupDocBuilder, buildResponsesSection(markupDocBuilder, operation), operation.getPath() + " " + operation.getMethod());
        if (locations.contains(AFTER_OPERATION_RESPONSES)) markupDocBuilder.pageBreak();

        if (locations.contains(BEFORE_OPERATION_CONSUMES)) markupDocBuilder.pageBreak();
        buildConsumesSection(markupDocBuilder, operation);
        if (locations.contains(AFTER_OPERATION_CONSUMES)) markupDocBuilder.pageBreak();

        if (locations.contains(BEFORE_OPERATION_PRODUCES)) markupDocBuilder.pageBreak();
        buildProducesSection(markupDocBuilder, operation);
        if (locations.contains(AFTER_OPERATION_PRODUCES)) markupDocBuilder.pageBreak();

        buildTagsSection(markupDocBuilder, operation);
        buildSecuritySchemeSection(markupDocBuilder, operation);
        buildExamplesSection(markupDocBuilder, operation, locations);
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_END, markupDocBuilder, operation));
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_AFTER, markupDocBuilder, operation));

        if (locations.contains(AFTER_OPERATION)) markupDocBuilder.pageBreak();

        return markupDocBuilder;
    }

    /**
     * Adds the operation title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the operation.
     *
     * @param operation the Swagger Operation
     */
    private void buildOperationTitle(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        buildOperationTitle(markupDocBuilder, operation.getTitle(), operation.getId());
        if (operation.getTitle().equals(operation.getOperation().getSummary())) {
            markupDocBuilder.block(operation.getMethod() + " " + operation.getPath(), MarkupBlockStyle.LITERAL);
        }
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title  the operation title
     * @param anchor optional anchor (null => auto-generate from title)
     */
    private void buildOperationTitle(MarkupDocBuilder markupDocBuilder, String title, String anchor) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            markupDocBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }

    /**
     * Builds a warning if method is deprecated.
     *
     * @param operation the Swagger Operation
     */
    private void buildDeprecatedSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        if (BooleanUtils.isTrue(operation.getOperation().isDeprecated())) {
            markupDocBuilder.block(labels.getLabel(DEPRECATED_OPERATION), MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.CAUTION);
        }
    }

    /**
     * Adds a operation description to the document.
     *
     * @param operation the Swagger Operation
     */
    private void buildDescriptionSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        MarkupDocBuilder descriptionBuilder = copyMarkupDocBuilder(markupDocBuilder);
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_BEGIN, descriptionBuilder, operation));
        String description = operation.getOperation().getDescription();
        if (isNotBlank(description)) {
            descriptionBuilder.paragraph(markupDescription(config.getSwaggerMarkupLanguage(), markupDocBuilder, description));
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_END, descriptionBuilder, operation));
        String descriptionContent = descriptionBuilder.toString();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_BEFORE, markupDocBuilder, operation));
        if (isNotBlank(descriptionContent)) {
            buildSectionTitle(markupDocBuilder, labels.getLabel(DESCRIPTION));
            markupDocBuilder.text(descriptionContent);
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(Position.OPERATION_DESCRIPTION_AFTER, markupDocBuilder, operation));
    }

    /**
     * Builds the parameters section
     *
     * @param operation the Swagger Operation
     */
    private List<ObjectType> buildParametersSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {

        List<ObjectType> inlineDefinitions = new ArrayList<>();

        parameterTableComponent.apply(markupDocBuilder, ParameterTableComponent.parameters(
                operation,
                inlineDefinitions,
                getSectionTitleLevel()
        ));

        return inlineDefinitions;
    }

    /**
     * Builds the body parameter section
     *
     * @param operation the Swagger Operation
     * @return a list of inlined types.
     */
    private List<ObjectType> buildBodyParameterSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        bodyParameterComponent.apply(markupDocBuilder, BodyParameterComponent.parameters(
                operation,
                inlineDefinitions
        ));

        return inlineDefinitions;
    }

    private List<ObjectType> buildResponsesSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        responseComponent.apply(markupDocBuilder, ResponseComponent.parameters(
                operation,
                getSectionTitleLevel(),
                inlineDefinitions
        ));

        return inlineDefinitions;
    }

    /**
     * Adds a operation section title to the document.
     *
     * @param title the section title
     */
    private void buildSectionTitle(MarkupDocBuilder markupDocBuilder, String title) {
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
     * @param title  inline schema title
     * @param anchor inline schema anchor
     */
    private void addInlineDefinitionTitle(MarkupDocBuilder markupDocBuilder, String title, String anchor) {
        markupDocBuilder.anchor(anchor);
        markupDocBuilder.newLine();
        markupDocBuilder.boldTextLine(title);
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
                addInlineDefinitionTitle(markupDocBuilder, definition.getName(), definition.getUniqueName());

                List<ObjectType> localDefinitions = new ArrayList<>();
                propertiesTableComponent.apply(markupDocBuilder, PropertiesTableComponent.parameters(
                        definition.getProperties(),
                        uniquePrefix,
                        localDefinitions
                ));
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(markupDocBuilder, Collections.singletonList(localDefinition), localDefinition.getUniqueName());
            }
        }

    }

    private void buildConsumesSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        List<String> consumes = operation.getOperation().getConsumes();
        if (CollectionUtils.isNotEmpty(consumes)) {
            consumesComponent.apply(markupDocBuilder, ConsumesComponent.parameters(consumes,
                    getSectionTitleLevel()));
        }

    }

    private void buildProducesSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        List<String> produces = operation.getOperation().getProduces();
        if (CollectionUtils.isNotEmpty(produces)) {
            producesComponent.apply(markupDocBuilder, ProducesComponent.parameters(produces,
                    getSectionTitleLevel()));
        }
    }

    private void buildTagsSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            List<String> tags = operation.getOperation().getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                buildSectionTitle(markupDocBuilder, labels.getLabel(TAGS));
                if (config.getTagOrdering() != null) {
                    tags.sort(config.getTagOrdering());
                }
                markupDocBuilder.unorderedList(tags);
            }
        }
    }

    /**
     * Builds the security section of a Swagger Operation.
     *
     * @param operation the Swagger Operation
     */
    private void buildSecuritySchemeSection(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        if (config.isPathSecuritySectionEnabled()) {
            securitySchemeComponent.apply(markupDocBuilder, SecuritySchemeComponent.parameters(
                    operation,
                    getSectionTitleLevel()
            ));
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
     * @param operation the Swagger Operation
     */
    private void buildExamplesSection(MarkupDocBuilder markupDocBuilder, PathOperation operation, List<PageBreakLocations> locations) {

        Map<String, Object> generatedRequestExampleMap = ExamplesUtil.generateRequestExampleMap(config.isGeneratedExamplesEnabled(), operation, definitions, definitionDocumentResolver, markupDocBuilder);
        Map<String, Object> generatedResponseExampleMap = ExamplesUtil.generateResponseExampleMap(config.isGeneratedExamplesEnabled(), operation, definitions, definitionDocumentResolver, markupDocBuilder);

        boolean beforeExampleRequestBreak = locations.contains(BEFORE_OPERATION_EXAMPLE_REQUEST);
        boolean afterExampleRequestBreak = locations.contains(AFTER_OPERATION_EXAMPLE_REQUEST);
        boolean beforeExampleResponseBreak = locations.contains(BEFORE_OPERATION_EXAMPLE_RESPONSE);
        boolean afterExampleResponseBreak = locations.contains(AFTER_OPERATION_EXAMPLE_RESPONSE);

        exampleMap(markupDocBuilder, generatedRequestExampleMap, labels.getLabel(EXAMPLE_REQUEST), labels.getLabel(REQUEST), beforeExampleRequestBreak, afterExampleRequestBreak);
        exampleMap(markupDocBuilder, generatedResponseExampleMap, labels.getLabel(EXAMPLE_RESPONSE), labels.getLabel(RESPONSE), beforeExampleResponseBreak, afterExampleResponseBreak);
    }

    private void exampleMap(MarkupDocBuilder markupDocBuilder, Map<String, Object> exampleMap, String operationSectionTitle, String sectionTitle, boolean beforeBreak, boolean afterBreak) {
        if (exampleMap.size() > 0) {
            if (beforeBreak) markupDocBuilder.pageBreak();
            buildSectionTitle(markupDocBuilder, operationSectionTitle);
            for (Map.Entry<String, Object> entry : exampleMap.entrySet()) {

                // Example title, like "Response 200" or "Request Body"
                buildExampleTitle(markupDocBuilder, sectionTitle + " " + entry.getKey());

                if (NumberUtils.isNumber(entry.getKey())) {
                    // Section header is an HTTP status code (numeric)
                    JsonNode rootNode = parseExample(entry.getValue());
                    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();



                    if (!fieldsIterator.hasNext()) {
                        // rootNode contains a single example, no need to further iterate.
                        String example = Json.pretty(rootNode);
//                        String example = Json.pretty(stripExampleQuotes(rootNode.toString()));
//                        example = Json.pretty(example);
                        markupDocBuilder.listingBlock(example, "json");
                    }
                    while (fieldsIterator.hasNext()) {
                        Map.Entry<String, JsonNode> field = fieldsIterator.next();

                        if (field.getKey().equals("application/json")) {
                            String example = Json.pretty(field.getValue());
                            example = stripExampleQuotes(StringEscapeUtils.unescapeJson(example));

                            markupDocBuilder.listingBlock(example, "json");

                        } else if (field.getKey().equals("application/xml")) {

                            String example = stripExampleQuotes(field.getValue().toString());
                            example = StringEscapeUtils.unescapeJava(example);

                            //TODO: pretty print XML

                            markupDocBuilder.listingBlock(example, "xml");
                        } else {
                            String example = Json.pretty(entry.getValue());
                            markupDocBuilder.listingBlock(example, "json");
                            break; // No need to print the same example multiple times
                        }
                    }
                } else if (entry.getKey().equals("path")) {
                    // Path shouldn't have quotes around it
                    markupDocBuilder.listingBlock(entry.getValue().toString());
                } else if (entry.getKey().equals("header")){
                    // Header should have format: apikey:"string"
                    markupDocBuilder.listingBlock(entry.getValue().toString(), "json");
                } else {
                    
                    Object value = entry.getValue();
                    
                    if (value instanceof Map) {
                        
                        @SuppressWarnings("unchecked")
                        Map<String, String> examplesByContentType = (Map<String, String>) value;
                        
                        for (Entry<String, String> entryByType : examplesByContentType.entrySet()) {
                            if (entryByType.getKey().equals("application/json")) {
                                String example = Json.pretty(entryByType.getValue());
                                example = stripExampleQuotes(StringEscapeUtils.unescapeJson(example));
    
                                markupDocBuilder.listingBlock(example, "json");
    
                            } else if (entryByType.getKey().equals("application/xml")) {
    
                                String example = stripExampleQuotes(entryByType.getValue().toString());
                                example = StringEscapeUtils.unescapeJava(example);
    
                                //TODO: pretty print XML
    
                                markupDocBuilder.listingBlock(example, "xml");
                            } else {
                                String example = Json.pretty(entry.getValue());
                                markupDocBuilder.listingBlock(example, "json");
                                break; // No need to print the same example multiple times
                            }
                        }
                    } else {
                        markupDocBuilder.listingBlock(Json.pretty(value), "json");
                    }
                }
            }
            if (afterBreak) markupDocBuilder.pageBreak();
        }
    }

    /**
     * Strip leading and trailing quotes from a string
     *
     * @param raw String containing leading or trailing quotes
     * @return parsed String
     */
    private String stripExampleQuotes(String raw) {
        return raw
                .replaceAll("^\"+", "")  // Strip leading quotes
                .replaceAll("\"+$", ""); // Strip trailing quotes
    }

    /**
     * Parse a JSON array
     *
     * @param raw Object containing a JSON string
     * @return JsonNode[contentType, example]
     * @throws RuntimeException when the given JSON string cannot be parsed
     */
    private JsonNode parseExample(Object raw) throws RuntimeException {
        try {
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            return mapper.readTree(Json.pretty(raw));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read example", ex);
        }
    }

    /**
     * Adds a example title to the document.
     *
     * @param title the section title
     */
    private void buildExampleTitle(MarkupDocBuilder markupDocBuilder, String title) {
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

    public static class Parameters {

        private final PathOperation operation;

        public Parameters(PathOperation operation) {
            this.operation = Validate.notNull(operation, "PathOperation must not be null");
        }
    }
}
