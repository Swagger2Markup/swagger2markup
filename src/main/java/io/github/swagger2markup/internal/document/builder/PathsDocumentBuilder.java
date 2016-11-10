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
package io.github.swagger2markup.internal.document.builder;

import ch.netzwerg.paleo.StringColumn;
import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.component.ConsumesComponent;
import io.github.swagger2markup.internal.component.MarkupComponent;
import io.github.swagger2markup.internal.component.ProducesComponent;
import io.github.swagger2markup.internal.component.TableComponent;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.*;
import io.github.swagger2markup.markup.builder.MarkupAdmonition;
import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Context;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Position;
import static io.github.swagger2markup.utils.IOUtils.normalizeName;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class PathsDocumentBuilder extends MarkupDocumentBuilder {

    private final String RESPONSE;
    private final String REQUEST;
    private final String PATHS;
    private final String RESOURCES;
    private final String PARAMETERS;
    private final String BODY_PARAMETER;
    private final String RESPONSES;
    private final String HEADERS_COLUMN;
    private final String EXAMPLE_REQUEST;
    private final String EXAMPLE_RESPONSE;

    private final String SECURITY;
    private final String TYPE_COLUMN;
    private final String HTTP_CODE_COLUMN;

    private final String DEPRECATED_OPERATION;
    private final String UNKNOWN;

    private static final String PATHS_ANCHOR = "paths";

    private final DefinitionDocumentResolver definitionDocumentResolver;

    public PathsDocumentBuilder(Swagger2MarkupConverter.Context globalContext, Swagger2MarkupExtensionRegistry extensionRegistry, java.nio.file.Path outputPath) {
        super(globalContext, extensionRegistry, outputPath);

        definitionDocumentResolver = new DefinitionDocumentResolverFromOperation();

        ResourceBundle labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
        RESPONSE = labels.getString("response");
        REQUEST = labels.getString("request");
        PATHS = labels.getString("paths");
        RESOURCES = labels.getString("resources");
        PARAMETERS = labels.getString("parameters");
        BODY_PARAMETER = labels.getString("body_parameter");
        RESPONSES = labels.getString("responses");
        HEADERS_COLUMN = labels.getString("headers_column");
        EXAMPLE_REQUEST = labels.getString("example_request");
        EXAMPLE_RESPONSE = labels.getString("example_response");
        SECURITY = labels.getString("security");
        TYPE_COLUMN = labels.getString("type_column");
        HTTP_CODE_COLUMN = labels.getString("http_code_column");
        DEPRECATED_OPERATION = labels.getString("operation.deprecated");
        UNKNOWN = labels.getString("unknown");

        if (config.isGeneratedExamplesEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Generate examples is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Generate examples is disabled.");
            }
        }

        if (config.isSeparatedOperationsEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is enabled.");
            }
            Validate.notNull(outputPath, "Output directory is required for separated operation files!");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is disabled.");
            }
        }
    }

    /**
     * Builds the paths MarkupDocument.
     *
     * @return the paths MarkupDocument
     */
    @Override
    public MarkupDocument build() {
        Map<String, Path> paths = globalContext.getSwagger().getPaths();
        if (MapUtils.isNotEmpty(paths)) {
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_BEFORE, this.markupDocBuilder));
            buildPathsTitle();
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
            buildsPathsSection(paths);
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_AFTER, this.markupDocBuilder));
        }
        return new MarkupDocument(markupDocBuilder);
    }

    /**
     * Builds the paths section. Groups the paths either as-is or by tags.
     *
     * @param paths the Swagger paths
     */
    private void buildsPathsSection(Map<String, Path> paths) {
        Set<PathOperation> pathOperations = toPathOperationsSet(paths, config.getOperationOrdering());
        if (CollectionUtils.isNotEmpty(pathOperations)) {
            if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
                pathOperations.forEach(this::buildOperation);
            } else {
                Validate.notEmpty(globalContext.getSwagger().getTags(), "Tags must not be empty, when operations are grouped by tags");
                // Group operations by tag
                Multimap<String, PathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(pathOperations, config.getOperationOrdering());

                Map<String, Tag> tagsMap = TagUtils.toSortedMap(globalContext.getSwagger().getTags(), config.getTagOrdering());

                tagsMap.forEach((String tagName, Tag tag) -> {
                    markupDocBuilder.sectionTitleWithAnchorLevel2(WordUtils.capitalize(tagName), tagName + "_resource");
                    String description = tag.getDescription();
                    if(StringUtils.isNotBlank(description)){
                        markupDocBuilder.paragraph(description);
                    }
                    operationsGroupedByTag.get(tagName).forEach(this::buildOperation);

                });
            }
        }
    }

    /**
     * Builds the path title depending on the operationsGroupedBy configuration setting.
     */
    private void buildPathsTitle() {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            buildPathsTitle(PATHS);
        } else {
            buildPathsTitle(RESOURCES);
        }
    }

    /**
     * Converts the Swagger paths into a list of PathOperations.
     *
     * @param paths the Swagger paths
     * @param comparator the comparator to use.
     *
     * @return the path operations
     */
    private Set<PathOperation> toPathOperationsSet(Map<String, Path> paths, Comparator<PathOperation> comparator) {
        Set<PathOperation> pathOperations;
        if (comparator != null) {
            pathOperations = new TreeSet<>(comparator);
        } else {
            pathOperations = new LinkedHashSet<>();
        }
        paths.forEach((relativePath, path) -> PathUtils.getOperationMap(path).forEach((httpMethod, operation) -> {
            pathOperations.add(new PathOperation(httpMethod, getPath(relativePath), operation));
        }));
        return pathOperations;
    }

    /**
     * Prepends the basePath to the relative path, if configured
     *
     * @param relativePath the relative operation path
     * @return either the relative or the full path
     */
    private String getPath(String relativePath) {
        if(config.isBasePathPrefixEnabled()){
            return StringUtils.defaultString(globalContext.getSwagger().getBasePath()) + relativePath;
        }
        return relativePath;
    }

    private void buildPathsTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, PATHS_ANCHOR);
    }

    /**
     * Apply extension context to all OperationsContentExtension.
     *
     * @param context context
     */
    private void applyPathsDocumentExtension(Context context) {
        for (PathsDocumentExtension extension : extensionRegistry.getPathsDocumentExtensions()) {
            extension.apply(context);
        }
    }

    /**
     * Create the operation filename depending on the generation mode
     *
     * @param operation operation
     * @return operation filename
     */
    private String resolveOperationDocument(PathOperation operation) {
        if (config.isSeparatedOperationsEnabled())
            return new File(config.getSeparatedOperationsFolder(), this.markupDocBuilder.addFileExtension(normalizeName(operation.getId()))).getPath();
        else
            return this.markupDocBuilder.addFileExtension(config.getPathsDocument());
    }

    /**
     * Builds a path operation depending on generation mode.
     *
     * @param operation operation
     */
    private void buildOperation(PathOperation operation) {
        if (config.isSeparatedOperationsEnabled()) {
            MarkupDocBuilder pathDocBuilder = copyMarkupDocBuilder();
            buildOperation(operation, pathDocBuilder);
            java.nio.file.Path operationFile = outputPath.resolve(resolveOperationDocument(operation));
            pathDocBuilder.writeToFileWithoutExtension(operationFile, StandardCharsets.UTF_8);
            if (logger.isInfoEnabled()) {
                logger.info("Separate operation file produced : '{}'", operationFile);
            }

            buildOperationRef(operation, this.markupDocBuilder);

        } else {
            buildOperation(operation, this.markupDocBuilder);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Operation processed : '{}' (normalized id = '{}')", operation, normalizeName(operation.getId()));
        }
    }

    /**
     * Builds a path operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildOperation(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (operation != null) {
            applyPathsDocumentExtension(new Context(Position.OPERATION_BEFORE, docBuilder, operation));
            buildOperationTitle(operation, docBuilder);
            applyPathsDocumentExtension(new Context(Position.OPERATION_BEGIN, docBuilder, operation));
            buildDeprecatedSection(operation, docBuilder);
            buildDescriptionSection(operation, docBuilder);
            inlineDefinitions(buildParametersSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), docBuilder);
            inlineDefinitions(buildBodyParameterSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), docBuilder);
            inlineDefinitions(buildResponsesSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), docBuilder);
            buildConsumesSection(operation, docBuilder);
            buildProducesSection(operation, docBuilder);
            buildTagsSection(operation, docBuilder);
            buildSecuritySchemeSection(operation, docBuilder);
            buildExamplesSection(operation, docBuilder);
            applyPathsDocumentExtension(new Context(Position.OPERATION_END, docBuilder, operation));
            applyPathsDocumentExtension(new Context(Position.OPERATION_AFTER, docBuilder, operation));
        }
    }

    /**
     * Builds a cross-reference to a separated operation file
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildOperationRef(PathOperation operation, MarkupDocBuilder docBuilder) {
        String document;
        if (!config.isInterDocumentCrossReferencesEnabled() || outputPath == null)
            document = null;
        else if (config.isSeparatedOperationsEnabled())
            document = defaultString(config.getInterDocumentCrossReferencesPrefix()) + resolveOperationDocument(operation);
        else
            document = defaultString(config.getInterDocumentCrossReferencesPrefix()) + resolveOperationDocument(operation);

        buildOperationTitle(copyMarkupDocBuilder().crossReference(document, operation.getId(), operation.getTitle()).toString(), "ref-" + operation.getId(), docBuilder);
    }

    /**
     * Builds a warning if method is deprecated.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildDeprecatedSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        Boolean deprecated = operation.getOperation().isDeprecated();
        if (BooleanUtils.isTrue(deprecated)) {
            docBuilder.block(DEPRECATED_OPERATION, MarkupBlockStyle.EXAMPLE, null, MarkupAdmonition.CAUTION);
        }
    }

    /**
     * Adds the operation title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildOperationTitle(PathOperation operation, MarkupDocBuilder docBuilder) {
        buildOperationTitle(operation.getTitle(), operation.getId(), docBuilder);
        if (operation.getTitle().equals(operation.getOperation().getSummary())) {
            docBuilder.block(operation.getMethod() + " " + operation.getPath(), MarkupBlockStyle.LITERAL);
        }
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title      the operation title
     * @param anchor     optional anchor (null => auto-generate from title)
     * @param docBuilder the MarkupDocBuilder to use
     */
    private void buildOperationTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            docBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }

    /**
     * Adds a operation section title to the document.
     *
     * @param title      the section title
     * @param docBuilder the MarkupDocBuilder to use
     */
    private void buildSectionTitle(String title, MarkupDocBuilder docBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleLevel3(title);
        } else {
            docBuilder.sectionTitleLevel4(title);
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
     * Adds a example title to the document.
     *
     * @param title      the section title
     * @param docBuilder the MarkupDocBuilder to use
     */
    private void buildExampleTitle(String title, MarkupDocBuilder docBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleLevel4(title);
        } else {
            docBuilder.sectionTitleLevel5(title);
        }
    }

    /**
     * Adds a operation description to the document.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildDescriptionSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        MarkupDocBuilder descriptionBuilder = copyMarkupDocBuilder();
        applyPathsDocumentExtension(new Context(Position.OPERATION_DESCRIPTION_BEGIN, descriptionBuilder, operation));
        buildDescriptionParagraph(operation.getOperation().getDescription(), descriptionBuilder);
        applyPathsDocumentExtension(new Context(Position.OPERATION_DESCRIPTION_END, descriptionBuilder, operation));
        String descriptionContent = descriptionBuilder.toString();

        applyPathsDocumentExtension(new Context(Position.OPERATION_DESCRIPTION_BEFORE, docBuilder, operation));
        if (isNotBlank(descriptionContent)) {
            buildSectionTitle(DESCRIPTION, docBuilder);
            docBuilder.text(descriptionContent);
        }
        applyPathsDocumentExtension(new Context(Position.OPERATION_DESCRIPTION_AFTER, docBuilder, operation));
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
     * Builds the parameters section
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private List<ObjectType> buildParametersSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<Parameter> parameters = operation.getOperation().getParameters();
        if (config.getParameterOrdering() != null)
            Collections.sort(parameters, config.getParameterOrdering());
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        // Filter parameters to display in parameters section
        List<Parameter> filteredParameters = parameters.stream()
                .filter(this::filterParameter).collect(Collectors.toList());

        MarkupDocBuilder parametersBuilder = copyMarkupDocBuilder();
        applyPathsDocumentExtension(new Context(Position.OPERATION_DESCRIPTION_BEGIN, parametersBuilder, operation));
        if (CollectionUtils.isNotEmpty(filteredParameters)) {
            StringColumn.Builder typeColumnBuilder = StringColumn.builder(StringColumnId.of(TYPE_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2");
            StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(NAME_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "3");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(DESCRIPTION_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "9")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder schemaColumnBuilder = StringColumn.builder(StringColumnId.of(SCHEMA_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "4")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder defaultColumnBuilder = StringColumn.builder(StringColumnId.of(DEFAULT_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");

            for (Parameter parameter : filteredParameters) {
                Type type = ParameterUtils.getType(parameter, globalContext.getSwagger().getDefinitions(), definitionDocumentResolver);
                type = createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), inlineDefinitions);

                typeColumnBuilder.add(boldText(WordUtils.capitalize(parameter.getIn())));
                nameColumnBuilder.add(getParameterNameColumnContent(parameter));
                descriptionColumnBuilder.add(swaggerMarkupDescription(parameter.getDescription()));
                schemaColumnBuilder.add(type.displaySchema(markupDocBuilder));
                defaultColumnBuilder.add(ParameterUtils.getDefaultValue(parameter).map(value -> literalText(Json.pretty(value))).orElse(""));
            }

            parametersBuilder = new TableComponent(new MarkupComponent.Context(config, parametersBuilder, extensionRegistry),
                    typeColumnBuilder.build(),
                    nameColumnBuilder.build(),
                    descriptionColumnBuilder.build(),
                    schemaColumnBuilder.build(),
                    defaultColumnBuilder.build()).render();
        }
        applyPathsDocumentExtension(new Context(Position.OPERATION_DESCRIPTION_END, parametersBuilder, operation));
        String parametersContent = parametersBuilder.toString();

        applyPathsDocumentExtension(new Context(Position.OPERATION_PARAMETERS_BEFORE, docBuilder, operation));
        if (isNotBlank(parametersContent)) {
            buildSectionTitle(PARAMETERS, docBuilder);
            docBuilder.text(parametersContent);
        }
        applyPathsDocumentExtension(new Context(Position.OPERATION_PARAMETERS_AFTER, docBuilder, operation));

        return inlineDefinitions;
    }


    /**
     * @param parameter the Swagger parameter
     * @return the name column column content of the parameter
     */
    private String getParameterNameColumnContent(Parameter parameter){
        MarkupDocBuilder parameterNameContent = copyMarkupDocBuilder();

        parameterNameContent.boldTextLine(parameter.getName(), true);
        if (parameter.getRequired())
            parameterNameContent.italicText(FLAGS_REQUIRED.toLowerCase());
        else
            parameterNameContent.italicText(FLAGS_OPTIONAL.toLowerCase());
        return parameterNameContent.toString();
    }

    /**
     * Builds the body parameter section
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     * @return a list of inlined types.
     */
    private List<ObjectType> buildBodyParameterSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        if (config.isFlatBodyEnabled()) {
            List<Parameter> parameters = operation.getOperation().getParameters();
            if (CollectionUtils.isNotEmpty(parameters)) {
                for (Parameter parameter : parameters) {
                    if (StringUtils.equals(parameter.getIn(), "body")) {
                        Type type = ParameterUtils.getType(parameter, globalContext.getSwagger().getDefinitions(), definitionDocumentResolver);

                        if (!(type instanceof ObjectType)) {
                            type = createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), inlineDefinitions);
                        }

                        buildSectionTitle(BODY_PARAMETER, docBuilder);
                        if (isNotBlank(parameter.getDescription())) {
                            buildDescriptionParagraph(parameter.getDescription(), docBuilder);
                        }

                        MarkupDocBuilder typeInfos = copyMarkupDocBuilder();
                        typeInfos.italicText(NAME_COLUMN).textLine(COLON + parameter.getName());
                        typeInfos.italicText(FLAGS_COLUMN).textLine(COLON + (BooleanUtils.isTrue(parameter.getRequired()) ? FLAGS_REQUIRED.toLowerCase() : FLAGS_OPTIONAL.toLowerCase()));

                        if (!(type instanceof ObjectType)) {
                            typeInfos.italicText(TYPE_COLUMN).textLine(COLON + type.displaySchema(docBuilder));
                        }

                        docBuilder.paragraph(typeInfos.toString(), true);

                        if (type instanceof ObjectType) {
                            inlineDefinitions.addAll(buildPropertiesTable(((ObjectType) type).getProperties(), operation.getId(), definitionDocumentResolver, docBuilder));
                        }
                    }
                }
            }
        }

        return inlineDefinitions;
    }

    private void buildConsumesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> consumes = operation.getOperation().getConsumes();
        if (CollectionUtils.isNotEmpty(consumes)) {
            new ConsumesComponent(componentContext, consumes, getSectionTitleLevel()).render();
        }

    }

    private void buildProducesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> produces = operation.getOperation().getProduces();
        if (CollectionUtils.isNotEmpty(produces)) {
            new ProducesComponent(componentContext, produces, getSectionTitleLevel()).render();
        }
    }

    private void buildTagsSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            List<String> tags = operation.getOperation().getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                buildSectionTitle(TAGS, docBuilder);
                if (config.getTagOrdering() != null) {
                    Collections.sort(tags, config.getTagOrdering());
                }
                docBuilder.unorderedList(tags);
            }
        }
    }

    /**
     * Builds the example section of a Swagger Operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildExamplesSection(PathOperation operation, MarkupDocBuilder docBuilder) {

        Map<String, Object> generatedRequestExampleMap = ExamplesUtil.generateRequestExampleMap(config.isGeneratedExamplesEnabled(), operation, globalContext.getSwagger().getDefinitions(), definitionDocumentResolver, markupDocBuilder);
        Map<String, Object> generatedResponseExampleMap = ExamplesUtil.generateResponseExampleMap(config.isGeneratedExamplesEnabled(), operation, globalContext.getSwagger().getDefinitions(), definitionDocumentResolver, markupDocBuilder);

        exampleMap(generatedRequestExampleMap, EXAMPLE_REQUEST, REQUEST, docBuilder);
        exampleMap(generatedResponseExampleMap, EXAMPLE_RESPONSE, RESPONSE, docBuilder);
    }

    private void exampleMap(Map<String, Object> exampleMap, String operationSectionTitle, String sectionTitle, MarkupDocBuilder docBuilder) {
        if (exampleMap.size() > 0) {
            buildSectionTitle(operationSectionTitle, docBuilder);
            for (Map.Entry<String, Object> entry : exampleMap.entrySet()) {
                buildExampleTitle(sectionTitle + " " + entry.getKey(), docBuilder);
                docBuilder.listingBlock(Json.pretty(entry.getValue()), "json");
            }
        }
    }

    /**
     * Builds the security section of a Swagger Operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the MarkupDocBuilder document builder
     */
    private void buildSecuritySchemeSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.isPathSecuritySectionEnabled()) {
            List<Map<String, List<String>>> securitySchemes = operation.getOperation().getSecurity();

            MarkupDocBuilder securityBuilder = copyMarkupDocBuilder();
            applyPathsDocumentExtension(new Context(Position.OPERATION_SECURITY_BEGIN, securityBuilder, operation));
            if (CollectionUtils.isNotEmpty(securitySchemes)) {

                Map<String, SecuritySchemeDefinition> securityDefinitions = globalContext.getSwagger().getSecurityDefinitions();

                StringColumn.Builder typeColumnBuilder = StringColumn.builder(StringColumnId.of(TYPE_COLUMN))
                        .putMetaData(TableComponent.WIDTH_RATIO, "3");
                StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(NAME_COLUMN))
                        .putMetaData(TableComponent.WIDTH_RATIO, "4");
                StringColumn.Builder scopeColumnBuilder = StringColumn.builder(StringColumnId.of(SCOPES_COLUMN))
                        .putMetaData(TableComponent.WIDTH_RATIO, "13")
                        .putMetaData(TableComponent.HEADER_COLUMN, "true");


                for (Map<String, List<String>> securityScheme : securitySchemes) {
                    for (Map.Entry<String, List<String>> securityEntry : securityScheme.entrySet()) {
                        String securityKey = securityEntry.getKey();
                        String type = UNKNOWN;
                        if (securityDefinitions != null && securityDefinitions.containsKey(securityKey)) {
                            type = securityDefinitions.get(securityKey).getType();
                        }

                        typeColumnBuilder.add(boldText(type));
                        nameColumnBuilder.add(boldText(copyMarkupDocBuilder().crossReference(securityDocumentResolver(), securityKey, securityKey).toString()));
                        scopeColumnBuilder.add(Joiner.on(",").join(securityEntry.getValue()));
                    }
                }

                securityBuilder = new TableComponent(new MarkupComponent.Context(config, securityBuilder, extensionRegistry),
                        typeColumnBuilder.build(),
                        nameColumnBuilder.build(),
                        scopeColumnBuilder.build()).render();
            }
            applyPathsDocumentExtension(new Context(Position.OPERATION_SECURITY_END, securityBuilder, operation));
            String securityContent = securityBuilder.toString();

            applyPathsDocumentExtension(new Context(Position.OPERATION_SECURITY_BEFORE, docBuilder, operation));
            if (isNotBlank(securityContent)) {
                buildSectionTitle(SECURITY, docBuilder);
                docBuilder.text(securityContent);
            }
            applyPathsDocumentExtension(new Context(Position.OPERATION_SECURITY_AFTER, docBuilder, operation));
        }
    }

    /**
     * Resolve Security document for use in cross-references.
     *
     * @return document or null if cross-reference is not inter-document
     */
    private String securityDocumentResolver() {
        if (!config.isInterDocumentCrossReferencesEnabled() || outputPath == null)
            return null;
        else
            return defaultString(config.getInterDocumentCrossReferencesPrefix()) + markupDocBuilder.addFileExtension(config.getSecurityDocument());
    }

    private List<ObjectType> buildResponsesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        Map<String, Response> responses = operation.getOperation().getResponses();
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        MarkupDocBuilder responsesBuilder = copyMarkupDocBuilder();
        applyPathsDocumentExtension(new Context(Position.OPERATION_RESPONSES_BEGIN, responsesBuilder, operation));
        if (MapUtils.isNotEmpty(responses)) {
            StringColumn.Builder httpCodeColumnBuilder = StringColumn.builder(StringColumnId.of(HTTP_CODE_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(DESCRIPTION_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "14")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder schemaColumnBuilder = StringColumn.builder(StringColumnId.of(SCHEMA_COLUMN))
                    .putMetaData(TableComponent.WIDTH_RATIO, "4")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");

            Map<String, Response> sortedResponses = toSortedMap(responses, config.getResponseOrdering());
            sortedResponses.forEach((String responseName, Response response) -> {
                String schemaContent = NO_CONTENT;
                if (response.getSchema() != null) {
                    Property property = response.getSchema();
                    Type type = new PropertyWrapper(property).getType(new DefinitionDocumentResolverFromOperation());

                    type = createInlineType(type, RESPONSE + " " + responseName, operation.getId() + " " + RESPONSE + " " + responseName, inlineDefinitions);

                    schemaContent = type.displaySchema(markupDocBuilder);
                }

                MarkupDocBuilder descriptionBuilder = copyMarkupDocBuilder();

                descriptionBuilder.text(swaggerMarkupDescription(response.getDescription()));

                Map<String, Property> headers = response.getHeaders();
                if (MapUtils.isNotEmpty(headers)) {
                    descriptionBuilder.newLine(true).boldText(HEADERS_COLUMN).text(COLON);
                    for (Map.Entry<String, Property> header : headers.entrySet()) {
                        descriptionBuilder.newLine(true);
                        Property headerProperty = header.getValue();
                        PropertyWrapper headerPropertyWrapper = new PropertyWrapper(headerProperty);
                        Type propertyType = headerPropertyWrapper.getType(null);
                        String headerDescription = swaggerMarkupDescription(headerProperty.getDescription());
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
                                descriptionBuilder.text(" ").boldText(DEFAULT_COLUMN).text(COLON).literalText(Json.pretty(optionalDefaultValue.get()));
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
        applyPathsDocumentExtension(new Context(Position.OPERATION_RESPONSES_END, responsesBuilder, operation));
        String responsesContent = responsesBuilder.toString();

        applyPathsDocumentExtension(new Context(Position.OPERATION_RESPONSES_BEFORE, docBuilder, operation));
        if (isNotBlank(responsesContent)) {
            buildSectionTitle(RESPONSES, docBuilder);
            docBuilder.text(responsesContent);
        }
        applyPathsDocumentExtension(new Context(Position.OPERATION_RESPONSES_AFTER, docBuilder, operation));

        return inlineDefinitions;
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

                List<ObjectType> localDefinitions = buildPropertiesTable(definition.getProperties(), uniquePrefix, new DefinitionDocumentResolverFromOperation(), docBuilder);
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), localDefinition.getUniqueName(), docBuilder);
            }
        }

    }

    /**
     * Overrides definition document resolver functor for inter-document cross-references from operations files.
     * This implementation adapt the relative paths to definitions files
     */
    class DefinitionDocumentResolverFromOperation extends DefinitionDocumentResolverDefault {

        public DefinitionDocumentResolverFromOperation() {
        }

        public String apply(String definitionName) {
            String defaultResolver = super.apply(definitionName);

            if (defaultResolver != null && config.isSeparatedOperationsEnabled())
                return defaultString(config.getInterDocumentCrossReferencesPrefix()) + new File("..", defaultResolver).getPath();
            else
                return defaultResolver;
        }
    }
}
