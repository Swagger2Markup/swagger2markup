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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import io.github.robwin.markup.builder.*;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.RefType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.ExamplesUtil;
import io.github.swagger2markup.internal.utils.ParameterUtils;
import io.github.swagger2markup.internal.utils.PropertyUtils;
import io.github.swagger2markup.internal.utils.TagUtils;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.*;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.github.swagger2markup.internal.utils.ListUtils.toSet;
import static io.github.swagger2markup.internal.utils.MapUtils.toKeySet;
import static io.github.swagger2markup.internal.utils.TagUtils.convertTagsListToMap;
import static io.github.swagger2markup.internal.utils.TagUtils.getTagDescription;
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
    private static final String DESCRIPTION_FILE_NAME = "description";


    public PathsDocumentBuilder(Swagger2MarkupConverter.Context globalContext, java.nio.file.Path outputPath) {
        super(globalContext, outputPath);

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
                logger.debug("Include examples is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Include examples is disabled.");
            }
        }
        if (config.isOperationDescriptionsEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Include hand-written operation descriptions is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Include hand-written operation descriptions is disabled.");
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
        }
        return new MarkupDocument(markupDocBuilder);
    }

    private void buildsPathsSection(Map<String, Path> paths) {
        Set<PathOperation> pathOperations = toPathOperationsSet(paths);
        if (CollectionUtils.isNotEmpty(pathOperations)) {
            if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
                for (PathOperation operation : pathOperations) {
                    buildOperation(operation);
                }
            } else {
                Multimap<String, PathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(pathOperations, config.getTagOrdering(), config.getOperationOrdering());
                Map<String, Tag> tagsMap = convertTagsListToMap(globalContext.getSwagger().getTags());
                for (String tagName : operationsGroupedByTag.keySet()) {
                    this.markupDocBuilder.sectionTitleLevel2(WordUtils.capitalize(tagName));

                    Optional<String> tagDescription = getTagDescription(tagsMap, tagName);
                    if (tagDescription.isPresent()) {
                        this.markupDocBuilder.paragraph(tagDescription.get());
                    }

                    for (PathOperation operation : operationsGroupedByTag.get(tagName)) {
                        buildOperation(operation);
                    }
                }
            }
        }
    }

    /**
     * Builds the path title depending on the operationsGroupedBy configuration setting.
     */
    private void buildPathsTitle() {
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            buildPathsTitle(PATHS);
        } else {
            buildPathsTitle(RESOURCES);
        }
    }

    /**
     * Converts the Swagger paths into a list PathOperations.
     *
     * @param paths the Swagger paths
     * @return the path operations
     */
    private Set<PathOperation> toPathOperationsSet(Map<String, Path> paths) {
        Set<PathOperation> pathOperations;
        if (config.getOperationOrdering() != null) {
            pathOperations = new TreeSet<>(config.getOperationOrdering());
        }else{
            pathOperations = new LinkedHashSet<>();
        }
        for (Map.Entry<String, Path> path : paths.entrySet()) {
            Map<HttpMethod, Operation> operations = path.getValue().getOperationMap(); // TODO AS_IS does not work because of https://github.com/swagger-api/swagger-core/issues/1696
            if (MapUtils.isNotEmpty(operations)) {
                for (Map.Entry<HttpMethod, Operation> operation : operations.entrySet()) {
                    pathOperations.add(new PathOperation(operation.getKey(), path.getKey(), operation.getValue()));
                }
            }
        }
        return pathOperations;
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
        for (PathsDocumentExtension extension : globalContext.getExtensionRegistry().getPathsDocumentExtensions()) {
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
            MarkupDocBuilder pathDocBuilder = this.markupDocBuilder.copy(false);
            buildOperation(operation, pathDocBuilder);
            java.nio.file.Path operationFile = outputPath.resolve(resolveOperationDocument(operation));

            try {
                pathDocBuilder.writeToFileWithoutExtension(operationFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Failed to write operation file: %s", operationFile), e);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Separate operation file produced: {}", operationFile);
            }

            buildOperationRef(operation, this.markupDocBuilder);

        } else {
            buildOperation(operation, this.markupDocBuilder);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Operation processed: {}", operation);
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
            applyPathsDocumentExtension(new Context(Position.OPERATION_BEGIN, docBuilder, operation));
            buildDeprecatedSection(operation, docBuilder);
            buildOperationTitle(operation, docBuilder);
            buildDescriptionSection(operation, docBuilder);
            inlineDefinitions(buildParametersSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), config.getInlineSchemaDepthLevel(), docBuilder);
            inlineDefinitions(buildBodyParameterSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), config.getInlineSchemaDepthLevel(), docBuilder);
            inlineDefinitions(buildResponsesSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), config.getInlineSchemaDepthLevel(), docBuilder);
            buildConsumesSection(operation, docBuilder);
            buildProducesSection(operation, docBuilder);
            buildTagsSection(operation, docBuilder);
            buildSecuritySchemeSection(operation, docBuilder);
            buildExamplesSection(operation, docBuilder);
            applyPathsDocumentExtension(new Context(Position.OPERATION_END, docBuilder, operation));
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
            document =  defaultString(config.getInterDocumentCrossReferencesPrefix()) + resolveOperationDocument(operation);
        else
            document = defaultString(config.getInterDocumentCrossReferencesPrefix()) + resolveOperationDocument(operation);

        buildOperationTitle(docBuilder.copy(false).crossReference(document, operation.getId(), operation.getTitle()).toString(), "ref-" + operation.getId(), docBuilder);
    }

    /**
     * Builds a warning if method is deprecated.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildDeprecatedSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        Boolean deprecated = operation.getOperation().isDeprecated();
        if (deprecated != null && deprecated) {
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
            docBuilder.listing(operation.getMethod() + " " + operation.getPath());
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
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
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
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleLevel3(title);
        } else {
            docBuilder.sectionTitleLevel4(title);
        }
    }

    /**
     * Adds a response section title to the document.
     *
     * @param title      the response title
     * @param docBuilder the MarkupDocBuilder to use
     */
    private void buildResponseTitle(String title, MarkupDocBuilder docBuilder) {
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleLevel4(title);
        } else {
            docBuilder.sectionTitleLevel5(title);
        }
    }

    /**
     * Adds a operation description to the document.
     * If hand-written descriptions exist, it tries to load the description from a file.
     * If the file cannot be read, the description of the operation is returned.
     * Operation folder search order :
     * - normalizeOperationFileName(operation.operationId)
     * - then, normalizeOperationFileName(operation.method + " " + operation.path)
     * - then, normalizeOperationFileName(operation.summary)
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildDescriptionSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.isOperationDescriptionsEnabled()) {
            Optional<String> description = handWrittenOperationDescription(normalizeName(operation.getId()), DESCRIPTION_FILE_NAME);
            if (!description.isPresent())
                description = handWrittenOperationDescription(normalizeName(operation.getTitle()), DESCRIPTION_FILE_NAME);

            if (description.isPresent()) {
                operationDescription(description.get(), docBuilder);
            } else {
                operationDescription(operation.getOperation().getDescription(), docBuilder);
            }
        } else {
            operationDescription(operation.getOperation().getDescription(), docBuilder);
        }
    }

    private void operationDescription(String description, MarkupDocBuilder docBuilder) {
        if (isNotBlank(description)) {
            buildSectionTitle(DESCRIPTION, docBuilder);
            docBuilder.paragraph(description);
        }
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

    private List<ObjectType> buildParametersSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<Parameter> parameters = operation.getOperation().getParameters();
        if (config.getParameterOrdering() != null)
            Collections.sort(parameters, config.getParameterOrdering());
        List<ObjectType> localDefinitions = new ArrayList<>();

        boolean displayParameters = false;
        if (CollectionUtils.isNotEmpty(parameters)) {
            for (Parameter p : parameters) {
                if (filterParameter(p)) {
                    displayParameters = true;
                    break;
                }
            }
        }

        if (displayParameters) {
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(TYPE_COLUMN).withWidthRatio(1).withHeaderColumn(true).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(NAME_COLUMN).withWidthRatio(1).withHeaderColumn(true).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(DESCRIPTION_COLUMN).withWidthRatio(6).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^6"),
                    new MarkupTableColumn(REQUIRED_COLUMN).withWidthRatio(1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                    new MarkupTableColumn(SCHEMA_COLUMN).withWidthRatio(1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                    new MarkupTableColumn(DEFAULT_COLUMN).withWidthRatio(1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"));
            for (Parameter parameter : parameters) {
                if (filterParameter(parameter)) {
                    Type type = ParameterUtils.getType(parameter, new DefinitionDocumentResolverFromOperation());

                    if (config.getInlineSchemaDepthLevel() > 0 && type instanceof ObjectType) {
                        if (MapUtils.isNotEmpty(((ObjectType) type).getProperties())) {
                            String localTypeName = parameter.getName();

                            type.setName(localTypeName);
                            type.setUniqueName(operation.getId() + " " + localTypeName);
                            localDefinitions.add((ObjectType) type);
                            type = new RefType(type);
                        }
                    }
                    String parameterType = WordUtils.capitalize(parameter.getIn());

                    List<String> content = Arrays.asList(
                            parameterType,
                            parameter.getName(),
                            parameterDescription(operation, parameter),
                            Boolean.toString(parameter.getRequired()),
                            type.displaySchema(markupDocBuilder),
                            ParameterUtils.getDefaultValue(parameter));
                    cells.add(content);
                }
            }
            buildSectionTitle(PARAMETERS, docBuilder);
            docBuilder.tableWithColumnSpecs(cols, cells);
        }

        return localDefinitions;
    }

    /**
     * Builds the body parameter section, if {@code Swagger2MarkupConfig.isIsolatedBody()} is true
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     * @return a list of inlined types.
     */
    private List<ObjectType> buildBodyParameterSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<ObjectType> localDefinitions = new ArrayList<>();

        if (config.isFlatBodyEnabled()) {
            List<Parameter> parameters = operation.getOperation().getParameters();
            if (CollectionUtils.isNotEmpty(parameters)) {
                for (Parameter parameter : parameters) {
                    if (StringUtils.equals(parameter.getIn(), "body")) {
                        Type type = ParameterUtils.getType(parameter, new DefinitionDocumentResolverFromOperation());

                        buildSectionTitle(BODY_PARAMETER, docBuilder);
                        if (isNotBlank(parameter.getDescription())) {
                            docBuilder.paragraph(parameter.getDescription());
                        }

                        MarkupDocBuilder typeInfos = MarkupDocBuilders.documentBuilder(config.getMarkupLanguage(), config.getLineSeparator());
                        typeInfos.italicText(REQUIRED_COLUMN).textLine(": " + parameter.getRequired());
                        typeInfos.italicText(NAME_COLUMN).textLine(": " + parameter.getName());
                        if (!(type instanceof ObjectType)) {
                            typeInfos.italicText(TYPE_COLUMN).textLine(": " + type.displaySchema(docBuilder));

                            docBuilder.paragraph(typeInfos.toString());
                        } else {
                            docBuilder.paragraph(typeInfos.toString());

                            localDefinitions.addAll(buildPropertiesTable((ObjectType) type, operation.getId(), config.getInlineSchemaDepthLevel(), new PropertyDescriptor(type), new DefinitionDocumentResolverFromOperation(), docBuilder));
                        }
                    }
                }
            }
        }

        return localDefinitions;
    }

    /**
     * Retrieves the description of a parameter, or otherwise an empty String.
     * If hand-written descriptions exist, it tries to load the description from a file.
     * If the file cannot be read, the description of the parameter is returned.
     * Operation folder search order :
     * - normalizeOperationFileName(operation.operationId)
     * - then, normalizeOperationFileName(operation.method + " " + operation.path)
     * - then, normalizeOperationFileName(operation.summary)
     *
     * @param operation the Swagger Operation
     * @param parameter the Swagger Parameter
     * @return the description of a parameter.
     */
    private String parameterDescription(final PathOperation operation, Parameter parameter) {
        if (config.isOperationDescriptionsEnabled()) {
            final String parameterName = parameter.getName();
            if (isNotBlank(parameterName)) {
                Optional<String> description = handWrittenOperationDescription(new File(normalizeName(operation.getId()), parameterName).getPath(), DESCRIPTION_FILE_NAME);
                if (!description.isPresent())
                    description = handWrittenOperationDescription(new File(normalizeName(operation.getTitle()), parameterName).getPath(), DESCRIPTION_FILE_NAME);

                if (description.isPresent()) {
                    return description.get();
                } else {
                    return defaultString(parameter.getDescription());
                }
            } else {
                return defaultString(parameter.getDescription());
            }
        } else {
            return defaultString(parameter.getDescription());
        }
    }

    private void buildConsumesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> consumes = operation.getOperation().getConsumes();
        if (CollectionUtils.isNotEmpty(consumes)) {
            buildSectionTitle(CONSUMES, docBuilder);
            docBuilder.unorderedList(consumes);
        }

    }

    private void buildProducesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> produces = operation.getOperation().getProduces();
        if (CollectionUtils.isNotEmpty(produces)) {
            buildSectionTitle(PRODUCES, docBuilder);
            docBuilder.unorderedList(produces);
        }
    }

    private void buildTagsSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            List<String> tags = operation.getOperation().getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                buildSectionTitle(TAGS, docBuilder);
                Set<String> tagsSet = toSet(tags, config.getTagOrdering());
                docBuilder.unorderedList(new ArrayList<>(tagsSet));
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

        Map<String, Object> generatedRequestExampleMap = ExamplesUtil.generateRequestExampleMap(config.isGeneratedExamplesEnabled(), operation, globalContext.getSwagger().getDefinitions(), markupDocBuilder);
        Map<String, Object> generatedResponseExampleMap = ExamplesUtil.generateResponseExampleMap(config.isGeneratedExamplesEnabled(), operation.getOperation(), globalContext.getSwagger().getDefinitions(), markupDocBuilder);

        exampleMap(generatedRequestExampleMap, EXAMPLE_REQUEST, REQUEST, docBuilder);
        exampleMap(generatedResponseExampleMap, EXAMPLE_RESPONSE, RESPONSE, docBuilder);
    }

    private void exampleMap(Map<String, Object> exampleMap, String operationSectionTitle, String sectionTile, MarkupDocBuilder docBuilder) {
        if (exampleMap.size() > 0) {
            buildSectionTitle(operationSectionTitle, docBuilder);
            for (Map.Entry<String, Object> entry : exampleMap.entrySet()) {
                buildSectionTitle(sectionTile + " " + entry.getKey(), docBuilder);
                docBuilder.listing(Json.pretty(entry.getValue()));
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
        List<Map<String, List<String>>> securitySchemes = operation.getOperation().getSecurity();
        if (CollectionUtils.isNotEmpty(securitySchemes)) {
            buildSectionTitle(SECURITY, docBuilder);
            Map<String, SecuritySchemeDefinition> securityDefinitions = globalContext.getSwagger().getSecurityDefinitions();
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(TYPE_COLUMN).withWidthRatio(1).withHeaderColumn(true).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(NAME_COLUMN).withWidthRatio(1).withHeaderColumn(true).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(SCOPES_COLUMN).withWidthRatio(6).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^6"));
            for (Map<String, List<String>> securityScheme : securitySchemes) {
                for (Map.Entry<String, List<String>> securityEntry : securityScheme.entrySet()) {
                    String securityKey = securityEntry.getKey();
                    String type = UNKNOWN;
                    if (securityDefinitions != null && securityDefinitions.containsKey(securityKey)) {
                        type = securityDefinitions.get(securityKey).getType();
                    }
                    List<String> content = Arrays.asList(type, docBuilder.copy(false).crossReference(securityKey, securityKey).toString(),
                            Joiner.on(",").join(securityEntry.getValue()));
                    cells.add(content);
                }
            }
            docBuilder.tableWithColumnSpecs(cols, cells);
        }
    }

    /**
     * Reads a hand-written description
     *
     * @param descriptionFolder   the name of the folder where the description file resides
     * @param descriptionFileName the name of the description file
     * @return the content of the file
     */
    private Optional<String> handWrittenOperationDescription(String descriptionFolder, String descriptionFileName) {
        for (String fileNameExtension : config.getMarkupLanguage().getFileNameExtensions()) {
            URI contentUri = config.getOperationDescriptionsUri().resolve(descriptionFolder).resolve(descriptionFileName + fileNameExtension);

            try (Reader reader = io.github.swagger2markup.utils.IOUtils.uriReader(contentUri)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Operation description content processed {}", contentUri);
                }

                return Optional.of(IOUtils.toString(reader).trim());
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to read Operation description content {} > {}", contentUri, e.getMessage());
                }
            }
        }
        return Optional.absent();
    }

    private List<ObjectType> buildResponsesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        Map<String, Response> responses = operation.getOperation().getResponses();
        List<ObjectType> localDefinitions = new ArrayList<>();

        if (MapUtils.isNotEmpty(responses)) {
            buildSectionTitle(RESPONSES, docBuilder);

            List<MarkupTableColumn> responseCols = Arrays.asList(
                    new MarkupTableColumn(HTTP_CODE_COLUMN).withWidthRatio(1).withHeaderColumn(true).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(DESCRIPTION_COLUMN).withWidthRatio(3).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^3"),
                    new MarkupTableColumn(SCHEMA_COLUMN).withWidthRatio(1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"));

            List<MarkupTableColumn> responseHeaderCols = Arrays.asList(
                    new MarkupTableColumn(NAME_COLUMN).withWidthRatio(1).withHeaderColumn(true).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(DESCRIPTION_COLUMN).withWidthRatio(3).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^3"),
                    new MarkupTableColumn(SCHEMA_COLUMN).withWidthRatio(1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                    new MarkupTableColumn(DEFAULT_COLUMN).withWidthRatio(1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"));

            Set<String> responseNames = toKeySet(responses, config.getResponseOrdering());
            for (String responseName : responseNames) {
                List<List<String>> cells = new ArrayList<>();
                List<List<String>> responseHeaderCells = new ArrayList<>();
                Response response = responses.get(responseName);
                if (response.getSchema() != null) {
                    Property property = response.getSchema();
                    Type type = PropertyUtils.getType(property, new DefinitionDocumentResolverFromOperation());
                    if (config.getInlineSchemaDepthLevel() > 0 && type instanceof ObjectType) {
                        if (MapUtils.isNotEmpty(((ObjectType) type).getProperties())) {
                            String localTypeName = RESPONSE + " " + responseName;

                            type.setName(localTypeName);
                            type.setUniqueName(operation.getId() + " " + localTypeName);
                            localDefinitions.add((ObjectType) type);
                            type = new RefType(type);
                        }
                    }
                    cells.add(Arrays.asList(responseName, response.getDescription(), type.displaySchema(markupDocBuilder)));
                } else {
                    cells.add(Arrays.asList(responseName, response.getDescription(), NO_CONTENT));
                }

                buildResponseTitle(HTTP_CODE_COLUMN + " " + responseName, docBuilder);
                docBuilder.tableWithColumnSpecs(responseCols, cells);

                Map<String, Property> headers = response.getHeaders();
                if(MapUtils.isNotEmpty(headers)) {
                    docBuilder.boldTextLine(HEADERS_COLUMN);
                    for(Map.Entry<String, Property> header : headers.entrySet()){
                        Property property = header.getValue();
                        Type propertyType = PropertyUtils.getType(property, null);
                        responseHeaderCells.add(Arrays.asList(header.getKey(),
                                property.getDescription(),
                                propertyType.displaySchema(markupDocBuilder),
                                PropertyUtils.getDefaultValue(property)));
                    }
                    docBuilder.tableWithColumnSpecs(responseHeaderCols, responseHeaderCells);
                }
            }
        }
        return localDefinitions;
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
     * @param depth        current inline schema depth
     * @param docBuilder   the docbuilder do use for output
     */
    private void inlineDefinitions(List<ObjectType> definitions, String uniquePrefix, int depth, MarkupDocBuilder docBuilder) {
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (ObjectType definition : definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), docBuilder);

                List<ObjectType> localDefinitions = buildPropertiesTable(definition, uniquePrefix, depth, new PropertyDescriptor(definition), new DefinitionDocumentResolverFromOperation(), docBuilder);
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), uniquePrefix, depth - 1, docBuilder);
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
