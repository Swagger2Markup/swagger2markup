/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup.builder.document;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilders;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.markup.builder.MarkupTableColumn;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.PathOperation;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.OperationsContentExtension;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.github.robwin.swagger2markup.utils.ParameterUtils;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import io.github.robwin.swagger2markup.utils.TagUtils;
import io.swagger.models.*;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

import static io.github.robwin.swagger2markup.utils.TagUtils.convertTagsListToMap;
import static io.github.robwin.swagger2markup.utils.TagUtils.getTagDescription;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupDocument {

    private final String RESPONSE;
    private final String PATHS;
    private final String RESOURCES;
    private final String PARAMETERS;
    private final String BODY_PARAMETER;
    private final String RESPONSES;
    private final String EXAMPLE_CURL;
    private final String EXAMPLE_REQUEST;
    private final String EXAMPLE_RESPONSE;

    private final String SECURITY;
    private final String TYPE_COLUMN;
    private final String HTTP_CODE_COLUMN;

    private static final String PATHS_ANCHOR = "paths";
    private static final String REQUEST_EXAMPLE_FILE_NAME = "http-request";
    private static final String RESPONSE_EXAMPLE_FILE_NAME = "http-response";
    private static final String CURL_EXAMPLE_FILE_NAME = "curl-request";
    private static final String DESCRIPTION_FILE_NAME = "description";


    public PathsDocument(Swagger2MarkupConverter.Context globalContext, java.nio.file.Path outputPath) {
        super(globalContext, outputPath);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels", config.getOutputLanguage().toLocale());
        RESPONSE = labels.getString("response");
        PATHS = labels.getString("paths");
        RESOURCES = labels.getString("resources");
        PARAMETERS = labels.getString("parameters");
        BODY_PARAMETER = labels.getString("body_parameter");
        RESPONSES = labels.getString("responses");
        EXAMPLE_CURL = labels.getString("example_curl");
        EXAMPLE_REQUEST = labels.getString("example_request");
        EXAMPLE_RESPONSE = labels.getString("example_response");
        SECURITY = labels.getString("security");
        TYPE_COLUMN = labels.getString("type_column");
        HTTP_CODE_COLUMN = labels.getString("http_code_column");

        if (config.isExamples()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Include examples is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Include examples is disabled.");
            }
        }
        if (config.isOperationDescriptions()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Include hand-written operation descriptions is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Include hand-written operation descriptions is disabled.");
            }
        }

        if (config.isSeparatedOperations()) {
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
     * Builds the paths markup document.
     *
     * @return the the paths markup document
     */
    @Override
    public MarkupDocument build() {
        operations();
        return this;
    }

    private void addPathsTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, PATHS_ANCHOR);
    }

    /**
     * Builds all operations of the Swagger model. Either grouped as-is or by tags.
     */
    private void operations() {
        Set<PathOperation> allOperations = new LinkedHashSet<>();
        Map<String, Path> paths = globalContext.swagger.getPaths();

        if (paths != null) {
            for (Map.Entry<String, Path> path : paths.entrySet()) {
                Map<HttpMethod, Operation> operations = path.getValue().getOperationMap();

                if (operations != null) {
                    for (Map.Entry<HttpMethod, Operation> operation : operations.entrySet()) {
                        allOperations.add(new PathOperation(operation.getKey(), path.getKey(), operation.getValue()));
                    }
                }
            }
        }

        if (allOperations.size() > 0) {

            applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.DOC_BEFORE, this.markupDocBuilder, null));
            if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
                addPathsTitle(PATHS);
                applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.DOC_BEGIN, this.markupDocBuilder, null));
            } else {
                addPathsTitle(RESOURCES);
                applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.DOC_BEGIN, this.markupDocBuilder, null));
            }

            if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
                if (config.getOperationOrdering() != null) {
                    Set<PathOperation> sortedOperations = new TreeSet<>(config.getOperationOrdering());
                    sortedOperations.addAll(allOperations);
                    allOperations = sortedOperations;
                }

                for (PathOperation operation : allOperations) {
                    processOperation(operation);
                }
            } else {
                Multimap<String, PathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(allOperations, config.getTagOrdering(), config.getOperationOrdering());

                Map<String, Tag> tagsMap = convertTagsListToMap(globalContext.swagger.getTags());
                for (String tagName : operationsGroupedByTag.keySet()) {
                    this.markupDocBuilder.sectionTitleLevel2(WordUtils.capitalize(tagName));

                    Optional<String> tagDescription = getTagDescription(tagsMap, tagName);
                    if (tagDescription.isPresent()) {
                        this.markupDocBuilder.paragraph(tagDescription.get());
                    }

                    for (PathOperation operation : operationsGroupedByTag.get(tagName)) {
                        processOperation(operation);
                    }
                }
            }

            applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.DOC_END, this.markupDocBuilder, null));
            applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.DOC_AFTER, this.markupDocBuilder, null));
        }

    }

    /**
     * Apply extension context to all OperationsContentExtension
     *
     * @param context context
     */
    private void applyOperationExtension(OperationsContentExtension.Context context) {
        for (OperationsContentExtension extension : globalContext.extensionRegistry.getExtensions(OperationsContentExtension.class)) {
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
        if (config.isSeparatedOperations())
            return new File(config.getSeparatedOperationsFolder(), this.markupDocBuilder.addFileExtension(normalizeName(operation.getId()))).getPath();
        else
            return this.markupDocBuilder.addFileExtension(config.getPathsDocument());
    }

    /**
     * Generate operations depending on the generation mode.
     *
     * @param operation operation
     */
    private void processOperation(PathOperation operation) {
        if (config.isSeparatedOperations()) {
            MarkupDocBuilder pathDocBuilder = this.markupDocBuilder.copy();
            operation(operation, pathDocBuilder);
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

            operationRef(operation, this.markupDocBuilder);

        } else {
            operation(operation, this.markupDocBuilder);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Operation processed: {}", operation);
        }
    }

    /**
     * Returns the operation name depending on available informations.
     * The summary is used to name the operation, or else the operation summary is used.
     *
     * @param operation operation
     * @return operation name
     */
    private String operationName(PathOperation operation) {
        return operation.getTitle();
    }

    /**
     * Builds an operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operation(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (operation != null) {
            applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.OP_BEGIN, docBuilder, operation));
            operationTitle(operation, docBuilder);
            descriptionSection(operation, docBuilder);
            inlineDefinitions(parametersSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), config.getInlineSchemaDepthLevel(), docBuilder);
            inlineDefinitions(bodyParameterSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), config.getInlineSchemaDepthLevel(), docBuilder);
            inlineDefinitions(responsesSection(operation, docBuilder), operation.getPath() + " " + operation.getMethod(), config.getInlineSchemaDepthLevel(), docBuilder);
            consumesSection(operation, docBuilder);
            producesSection(operation, docBuilder);
            tagsSection(operation, docBuilder);
            securitySchemeSection(operation, docBuilder);
            examplesSection(operation, docBuilder);
            applyOperationExtension(new OperationsContentExtension.Context(OperationsContentExtension.Position.OP_END, docBuilder, operation));
        }
    }

    /**
     * Builds a cross-reference to a separated operation file
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operationRef(PathOperation operation, MarkupDocBuilder docBuilder) {
        String document = resolveOperationDocument(operation);
        String operationName = operationName(operation);

        addOperationTitle(docBuilder.copy().crossReference(document, operationName, operationName).toString(), "ref-" + operationName, docBuilder);
    }

    /**
     * Adds the operation title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operationTitle(PathOperation operation, MarkupDocBuilder docBuilder) {
        String operationName = operationName(operation);

        addOperationTitle(operationName, null, docBuilder);
        if (operationName.equals(operation.getOperation().getSummary())) {
            docBuilder.listing(operation.getMethod() + " " + operation.getPath());
        }
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title      the operation title
     * @param anchor     optional anchor (null => auto-generate from title)
     * @param docBuilder the docbuilder do use for output
     */
    private void addOperationTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            docBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }

    /**
     * Adds a operation section title to the document.
     *
     * @param title      the operation title
     * @param docBuilder the docbuilder do use for output
     */
    private void addOperationSectionTitle(String title, MarkupDocBuilder docBuilder) {
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleLevel3(title);
        } else {
            docBuilder.sectionTitleLevel4(title);
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
    private void descriptionSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.isOperationDescriptions()) {
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
            addOperationSectionTitle(DESCRIPTION, docBuilder);
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
        return (!config.isFlatBody() || !StringUtils.equals(parameter.getIn(), "body"));
    }

    private List<ObjectType> parametersSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<Parameter> parameters = operation.getOperation().getParameters();
        if (config.getParameterOrdering() != null)
            Collections.sort(parameters, config.getParameterOrdering());
        List<ObjectType> localDefinitions = new ArrayList<>();

        boolean displayParameters = false;
        if (CollectionUtils.isNotEmpty(parameters))
            for (Parameter p : parameters)
                if (filterParameter(p)) {
                    displayParameters = true;
                    break;
                }

        if (displayParameters) {
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(TYPE_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(NAME_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(DESCRIPTION_COLUMN, 6).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^6"),
                    new MarkupTableColumn(REQUIRED_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                    new MarkupTableColumn(SCHEMA_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                    new MarkupTableColumn(DEFAULT_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"));
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
            addOperationSectionTitle(PARAMETERS, docBuilder);
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
    private List<ObjectType> bodyParameterSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<ObjectType> localDefinitions = new ArrayList<>();

        if (config.isFlatBody()) {
            List<Parameter> parameters = operation.getOperation().getParameters();
            if (CollectionUtils.isNotEmpty(parameters)) {
                for (Parameter parameter : parameters) {
                    if (StringUtils.equals(parameter.getIn(), "body")) {
                        Type type = ParameterUtils.getType(parameter, new DefinitionDocumentResolverFromOperation());

                        addOperationSectionTitle(BODY_PARAMETER, docBuilder);
                        if (isNotBlank(parameter.getDescription())) {
                            docBuilder.paragraph(parameter.getDescription());
                        }

                        MarkupDocBuilder typeInfos = MarkupDocBuilders.documentBuilder(config.getMarkupLanguage());
                        typeInfos.italicText(REQUIRED_COLUMN).textLine(": " + parameter.getRequired());
                        typeInfos.italicText(NAME_COLUMN).textLine(": " + parameter.getName());
                        if (!(type instanceof ObjectType)) {
                            typeInfos.italicText(TYPE_COLUMN).textLine(": " + type.displaySchema(docBuilder));

                            docBuilder.paragraph(typeInfos.toString());
                        } else {
                            docBuilder.paragraph(typeInfos.toString());

                            localDefinitions.addAll(typeProperties((ObjectType) type, operation.getId(), config.getInlineSchemaDepthLevel(), new PropertyDescriptor(type), new DefinitionDocumentResolverFromOperation(), docBuilder));
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
        if (config.isOperationDescriptions()) {
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

    private void consumesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> consumes = operation.getOperation().getConsumes();
        if (CollectionUtils.isNotEmpty(consumes)) {
            addOperationSectionTitle(CONSUMES, docBuilder);
            docBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> produces = operation.getOperation().getProduces();
        if (CollectionUtils.isNotEmpty(produces)) {
            addOperationSectionTitle(PRODUCES, docBuilder);
            docBuilder.unorderedList(produces);
        }
    }

    private void tagsSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.getOperationsGroupedBy() == GroupBy.AS_IS) {
            List<String> tags = operation.getOperation().getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                addOperationSectionTitle(TAGS, docBuilder);
                Set<String> sortedTags;
                if (config.getTagOrdering() == null)
                    sortedTags = new LinkedHashSet<>();
                else
                    sortedTags = new TreeSet<>(config.getTagOrdering());
                sortedTags.addAll(tags);
                docBuilder.unorderedList(new ArrayList<>(sortedTags));
            }
        }
    }

    /**
     * Builds the example section of a Swagger Operation. Tries to load the examples from
     * curl-request.adoc, http-request.adoc and http-response.adoc or
     * curl-request.md, http-request.md and http-response.md.
     * Operation folder search order :
     * - normalizeOperationFileName(operation.operationId)
     * - then, normalizeOperationFileName(operation.method + " " + operation.path)
     * - then, normalizeOperationFileName(operation.summary)
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void examplesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (config.isExamples()) {
            Optional<String> curlExample = example(normalizeName(operation.getId()), CURL_EXAMPLE_FILE_NAME);
            if (!curlExample.isPresent())
                curlExample = example(normalizeName(operation.getTitle()), CURL_EXAMPLE_FILE_NAME);

            if (curlExample.isPresent()) {
                addOperationSectionTitle(EXAMPLE_CURL, docBuilder);
                docBuilder.paragraph(curlExample.get());
            }

            Optional<String> requestExample = example(normalizeName(operation.getId()), REQUEST_EXAMPLE_FILE_NAME);
            if (!requestExample.isPresent())
                requestExample = example(normalizeName(operation.getTitle()), REQUEST_EXAMPLE_FILE_NAME);

            if (requestExample.isPresent()) {
                addOperationSectionTitle(EXAMPLE_REQUEST, docBuilder);
                docBuilder.paragraph(requestExample.get());
            }

            Optional<String> responseExample = example(normalizeName(operation.getId()), RESPONSE_EXAMPLE_FILE_NAME);
            if (!responseExample.isPresent())
                responseExample = example(normalizeName(operation.getTitle()), RESPONSE_EXAMPLE_FILE_NAME);

            if (responseExample.isPresent()) {
                addOperationSectionTitle(EXAMPLE_RESPONSE, docBuilder);
                docBuilder.paragraph(responseExample.get());
            }
        }
    }

    /**
     * Reads an example
     *
     * @param exampleFolder   the name of the folder where the example file resides
     * @param exampleFileName the name of the example file
     * @return the content of the file
     */
    private Optional<String> example(String exampleFolder, String exampleFileName) {
        for (String fileNameExtension : config.getMarkupLanguage().getFileNameExtensions()) {
            URI contentUri = config.getExamplesUri().resolve(exampleFolder).resolve(exampleFileName + fileNameExtension);

            try (Reader reader = io.github.robwin.swagger2markup.utils.IOUtils.uriReader(contentUri)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Example content processed {}", contentUri);
                }

                return Optional.of(IOUtils.toString(reader).trim());
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to read example content {} > {}", contentUri, e.getMessage());
                }
            }
        }

        return Optional.absent();
    }

    /**
     * Builds the security section of a Swagger Operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the MarkupDocBuilder document builder
     */
    private void securitySchemeSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<Map<String, List<String>>> securitySchemes = operation.getOperation().getSecurity();
        if (CollectionUtils.isNotEmpty(securitySchemes)) {
            addOperationSectionTitle(SECURITY, docBuilder);
            Map<String, SecuritySchemeDefinition> securityDefinitions = globalContext.swagger.getSecurityDefinitions();
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(TYPE_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                    new MarkupTableColumn(NAME_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(SCOPES_COLUMN, 6).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^6"));
            for (Map<String, List<String>> securityScheme : securitySchemes) {
                for (Map.Entry<String, List<String>> securityEntry : securityScheme.entrySet()) {
                    String securityKey = securityEntry.getKey();
                    String type = "UNKNOWN";
                    if (securityDefinitions != null && securityDefinitions.containsKey(securityKey)) {
                        type = securityDefinitions.get(securityKey).getType();
                    }
                    List<String> content = Arrays.asList(type, docBuilder.copy().crossReference(securityKey, securityKey).toString(),
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

            try (Reader reader = io.github.robwin.swagger2markup.utils.IOUtils.uriReader(contentUri)) {
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

    private List<ObjectType> responsesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        Map<String, Response> responses = operation.getOperation().getResponses();
        List<ObjectType> localDefinitions = new ArrayList<>();

        if (MapUtils.isNotEmpty(responses)) {
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(HTTP_CODE_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                    new MarkupTableColumn(DESCRIPTION_COLUMN, 6).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^6"),
                    new MarkupTableColumn(SCHEMA_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"));
            Set<String> responseNames;
            if (config.getResponseOrdering() == null)
                responseNames = new LinkedHashSet<>();
            else
                responseNames = new TreeSet<>(config.getResponseOrdering());
            responseNames.addAll(responses.keySet());

            for (String responseName : responseNames) {
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
            }
            addOperationSectionTitle(RESPONSES, docBuilder);
            docBuilder.tableWithColumnSpecs(cols, cells);
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

                List<ObjectType> localDefinitions = typeProperties(definition, uniquePrefix, depth, new PropertyDescriptor(definition), new DefinitionDocumentResolverFromOperation(), docBuilder);
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

            if (defaultResolver != null && config.isSeparatedOperations())
                return defaultString(config.getInterDocumentCrossReferencesPrefix()) + new File("..", defaultResolver).getPath();
            else
                return defaultResolver;
        }
    }
}
