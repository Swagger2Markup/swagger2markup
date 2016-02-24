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
import io.github.robwin.markup.builder.MarkupTableColumn;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.PathOperation;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.github.robwin.swagger2markup.utils.ExamplesUtil;
import io.github.robwin.swagger2markup.utils.ParameterUtils;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import io.github.robwin.swagger2markup.utils.TagUtils;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static io.github.robwin.swagger2markup.utils.TagUtils.convertTagsListToMap;
import static io.github.robwin.swagger2markup.utils.TagUtils.getTagDescription;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupDocument {

    private final String RESPONSE;
    private final String REQUEST;
    private final String PATHS;
    private final String RESOURCES;
    private final String PARAMETERS;
    private final String RESPONSES;
    private final String EXAMPLE_CURL;
    private final String EXAMPLE_REQUEST;
    private final String EXAMPLE_RESPONSE;

    private final String SECURITY;
    private final String TYPE_COLUMN;
    private final String HTTP_CODE_COLUMN;
    private static final String REQUEST_EXAMPLE_FILE_NAME = "http-request";
    private static final String RESPONSE_EXAMPLE_FILE_NAME = "http-response";
    private static final String CURL_EXAMPLE_FILE_NAME = "curl-request";
    private static final String DESCRIPTION_FOLDER_NAME = "paths";
    private static final String DESCRIPTION_FILE_NAME = "description";
    private final String PARAMETER;
    private static final Pattern FILENAME_FORBIDDEN_PATTERN = Pattern.compile("[^0-9A-Za-z-_]+");

    private boolean examplesAvailable;
    private String examplesFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;
    private final GroupBy pathsGroupedBy;
    private final int inlineSchemaDepthLevel;
    private final Comparator<String> tagOrdering;
    private final Comparator<PathOperation> operationOrdering;
    private boolean separatedOperationsEnabled;
    private String separatedOperationsFolder;
    private String pathsDocument;


    public PathsDocument(Swagger2MarkupConfig swagger2MarkupConfig, String outputDirectory){
        super(swagger2MarkupConfig, outputDirectory);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        RESPONSE = labels.getString("response");
        REQUEST = labels.getString("request");
        PATHS = labels.getString("paths");
        RESOURCES = labels.getString("resources");
        PARAMETERS = labels.getString("parameters");
        RESPONSES = labels.getString("responses");
        EXAMPLE_CURL = labels.getString("example_curl");
        EXAMPLE_REQUEST = labels.getString("example_request");
        EXAMPLE_RESPONSE = labels.getString("example_response");
        SECURITY = labels.getString("security");
        TYPE_COLUMN = labels.getString("type_column");
        HTTP_CODE_COLUMN = labels.getString("http_code_column");
        PARAMETER = labels.getString("parameter");

        this.pathsDocument = swagger2MarkupConfig.getPathsDocument();
        this.inlineSchemaDepthLevel = swagger2MarkupConfig.getInlineSchemaDepthLevel();
        this.pathsGroupedBy = swagger2MarkupConfig.getPathsGroupedBy();
        if(isNotBlank(swagger2MarkupConfig.getExamplesFolderPath())){
            this.examplesAvailable = true;
            this.examplesFolderPath = swagger2MarkupConfig.getExamplesFolderPath();
        }
        if(isNotBlank(swagger2MarkupConfig.getDescriptionsFolderPath())){
            this.handWrittenDescriptionsEnabled = true;
            this.descriptionsFolderPath = swagger2MarkupConfig.getDescriptionsFolderPath() + "/" + DESCRIPTION_FOLDER_NAME;
        }

        if(examplesAvailable){
            if (logger.isDebugEnabled()) {
                logger.debug("Include examples is enabled.");
            }
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("Include examples is disabled.");
            }
        }
        if(handWrittenDescriptionsEnabled){
            if (logger.isDebugEnabled()) {
                logger.debug("Include hand-written descriptions is enabled.");
            }
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("Include hand-written descriptions is disabled.");
            }
        }

        this.separatedOperationsEnabled = swagger2MarkupConfig.isSeparatedOperations();
        this.separatedOperationsFolder = swagger2MarkupConfig.getSeparatedOperationsFolder();
        if(this.separatedOperationsEnabled){
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is enabled.");
            }
            Validate.notEmpty(outputDirectory, "Output directory is required for separated operation files!");
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is disabled.");
            }
        }
        tagOrdering = swagger2MarkupConfig.getTagOrdering();
        operationOrdering = swagger2MarkupConfig.getOperationOrdering();
    }

    /**
     * Builds the paths markup document.
     *
     * @return the the paths markup document
     */
    @Override
    public MarkupDocument build(){
        operations();
        return this;
    }

    /**
     * Builds all operations of the Swagger model. Either grouped as-is or by tags.
     */
    private void operations(){
        Set<PathOperation> allOperations = new LinkedHashSet<>();
        Map<String, Path> paths = swagger.getPaths();

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

            if (pathsGroupedBy == GroupBy.AS_IS) {
                this.markupDocBuilder.sectionTitleLevel1(PATHS);

                if (this.operationOrdering != null) {
                    Set<PathOperation> sortedOperations = new TreeSet<>(this.operationOrdering);
                    sortedOperations.addAll(allOperations);
                    allOperations = sortedOperations;
                }

                for (PathOperation operation : allOperations) {
                    processOperation(operation);
                }


            } else {
                this.markupDocBuilder.sectionTitleLevel1(RESOURCES);

                Multimap<String, PathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(allOperations, tagOrdering, operationOrdering);

                Map<String, Tag> tagsMap = convertTagsListToMap(swagger.getTags());
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
        }

    }

    /**
     * Create a normalized filename for a separated operation file
     * @param operation operation
     * @return a normalized filename for the separated operation file
     */
    private String normalizeOperationFileName(String operation) {
        return FILENAME_FORBIDDEN_PATTERN.matcher(operation).replaceAll("_").toLowerCase();
    }

    /**
     * Create the operation filename depending on the generation mode
     * @param operation operation
     * @return operation filename
     */
    private String resolveOperationDocument(PathOperation operation) {
        if (this.separatedOperationsEnabled)
            return new File(this.separatedOperationsFolder, this.markupDocBuilder.addfileExtension(normalizeOperationFileName(operation.getId()))).getPath();
        else
            return this.markupDocBuilder.addfileExtension(this.pathsDocument);
    }

    /**
     * Generate operations depending on the generation mode.
     * @param operation operation
     */
    private void processOperation(PathOperation operation) {
        if (separatedOperationsEnabled) {
            MarkupDocBuilder pathDocBuilder = MarkupDocBuilders.documentBuilder(markupLanguage);
            operation(operation, pathDocBuilder);
            File operationFile = new File(outputDirectory, resolveOperationDocument(operation));

            try {
                String operationDirectory = FilenameUtils.getFullPath(operationFile.getPath());
                String operationFileName = FilenameUtils.getName(operationFile.getPath());

                pathDocBuilder.writeToFileWithoutExtension(operationDirectory, operationFileName, StandardCharsets.UTF_8);
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
     * @param operation operation
     * @return operation name
     */
    private String operationName(PathOperation operation) {
      return operation.getTitle();
    }

    /**
     * Builds an operation.
     *
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operation(PathOperation operation, MarkupDocBuilder docBuilder) {
        if(operation != null){
            operationTitle(operation, docBuilder);
            descriptionSection(operation, docBuilder);
            inlineDefinitions(parametersSection(operation, docBuilder), inlineSchemaDepthLevel, docBuilder);
            inlineDefinitions(responsesSection(operation, docBuilder), inlineSchemaDepthLevel, docBuilder);
            consumesSection(operation, docBuilder);
            producesSection(operation, docBuilder);
            tagsSection(operation, docBuilder);
            securitySchemeSection(operation, docBuilder);
            examplesSection(operation, docBuilder);
        }
    }

    /**
     * Builds a cross-reference to a separated operation file
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operationRef(PathOperation operation, MarkupDocBuilder docBuilder) {
        String document = resolveOperationDocument(operation);
        String operationName = operationName(operation);

        addOperationTitle(docBuilder.crossReferenceAsString(document, operationName, operationName), docBuilder);
    }

    /**
     * Adds the operation title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the operation.
     *
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operationTitle(PathOperation operation, MarkupDocBuilder docBuilder) {
        String operationName = operationName(operation);

        addOperationTitle(operationName, docBuilder);
        if(operationName.equals(operation.getOperation().getSummary())) {
            docBuilder.listing(operation.getMethod() + " " + operation.getPath());
        }
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title the operation title
     * @param docBuilder the docbuilder do use for output
     */
    private void addOperationTitle(String title, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS){
            docBuilder.sectionTitleLevel2(title);
        }else{
            docBuilder.sectionTitleLevel3(title);
        }
    }

    /**
     * Adds a operation section title to the document.
     *
     * @param title the operation title
     * @param docBuilder the docbuilder do use for output
     */
    private void addOperationSectionTitle(String title, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS){
            docBuilder.sectionTitleLevel3(title);
        }else{
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
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void descriptionSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if(handWrittenDescriptionsEnabled){
            Optional<String> description = handWrittenOperationDescription(normalizeOperationFileName(operation.getId()), DESCRIPTION_FILE_NAME);
            if (!description.isPresent())
                description = handWrittenOperationDescription(normalizeOperationFileName(operation.getTitle()), DESCRIPTION_FILE_NAME);
            if (description.isPresent()) {
                operationDescription(description.get(), docBuilder);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                }
                operationDescription(operation.getOperation().getDescription(), docBuilder);
            }
        }else {
            operationDescription(operation.getOperation().getDescription(), docBuilder);
        }
    }

    private void operationDescription(String description, MarkupDocBuilder docBuilder) {
        if (isNotBlank(description)) {
            addOperationSectionTitle(DESCRIPTION, docBuilder);
            docBuilder.paragraph(description);
        }
    }

    private List<Type> parametersSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<Parameter> parameters = operation.getOperation().getParameters();
        List<Type> localDefinitions = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(parameters)){
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(TYPE_COLUMN, 1),
                    new MarkupTableColumn(NAME_COLUMN, 1),
                    new MarkupTableColumn(DESCRIPTION_COLUMN, 6),
                    new MarkupTableColumn(REQUIRED_COLUMN, 1),
                    new MarkupTableColumn(SCHEMA_COLUMN, 1),
                    new MarkupTableColumn(DEFAULT_COLUMN, 1));
            for(Parameter parameter : parameters){
                Type type = ParameterUtils.getType(parameter, new DefinitionDocumentResolverFromOperation());
                if (inlineSchemaDepthLevel > 0 && type instanceof ObjectType) {
                    if (MapUtils.isNotEmpty(((ObjectType) type).getProperties())) {
                        String localTypeName = parameter.getName();

                        type.setName(localTypeName);
                        type.setUniqueName(uniqueTypeName(localTypeName));
                        localDefinitions.add(type);
                        type = new RefType(type);
                    }
                }
                String parameterType = WordUtils.capitalize(parameter.getIn() + PARAMETER);
                // Table content row
                List<String> content = Arrays.asList(
                        parameterType,
                        parameter.getName(),
                        parameterDescription(operation, parameter),
                        Boolean.toString(parameter.getRequired()),
                        type.displaySchema(markupDocBuilder),
                        ParameterUtils.getDefaultValue(parameter));
               cells.add(content);
            }
            addOperationSectionTitle(PARAMETERS, docBuilder);
            docBuilder.tableWithColumnSpecs(cols, cells);
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
    private String parameterDescription(final PathOperation operation, Parameter parameter){
        if (handWrittenDescriptionsEnabled) {
            final String parameterName = parameter.getName();
            if (isNotBlank(parameterName)) {
                Optional<String> description = handWrittenOperationDescription(new File(normalizeOperationFileName(operation.getId()), parameterName).getPath(), DESCRIPTION_FILE_NAME);
                if (!description.isPresent())
                    description = handWrittenOperationDescription(new File(normalizeOperationFileName(operation.getTitle()), parameterName).getPath(), DESCRIPTION_FILE_NAME);
                if (description.isPresent()) {
                    return description.get();
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Hand-written description file cannot be read. Trying to use description from Swagger source.");
                    }
                    return defaultString(parameter.getDescription());
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Hand-written description file cannot be read, because name of parameter is empty. Trying to use description from Swagger source.");
                }
                return defaultString(parameter.getDescription());
            }
        } else {
            return defaultString(parameter.getDescription());
        }
    }

    private void consumesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> consumes = operation.getOperation().getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            addOperationSectionTitle(CONSUMES, docBuilder);
            docBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<String> produces = operation.getOperation().getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            addOperationSectionTitle(PRODUCES, docBuilder);
            docBuilder.unorderedList(produces);
        }
    }

    private void tagsSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS) {
            List<String> tags = operation.getOperation().getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                addOperationSectionTitle(TAGS, docBuilder);
                Set<String> sortedTags;
                if (tagOrdering == null)
                    sortedTags = new LinkedHashSet<>();
                else
                    sortedTags = new TreeSet<>(this.tagOrdering);
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
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void examplesSection(PathOperation operation, MarkupDocBuilder docBuilder) {

        Optional<String> curlExample = Optional.absent();
        Optional<String> requestExample = Optional.absent();
        Optional<String> responseExample = Optional.absent();
        Optional<Map<String, Object>> generatedRequestExampleMap;
        Optional<Map<String, Object>> generatedResponseExampleMap;

        if(examplesAvailable){
            curlExample = exampleFromFile(normalizeOperationFileName(operation.getId()), CURL_EXAMPLE_FILE_NAME);
            if (!curlExample.isPresent())
                curlExample = exampleFromFile(normalizeOperationFileName(operation.getTitle()), CURL_EXAMPLE_FILE_NAME);
            requestExample = exampleFromFile(normalizeOperationFileName(operation.getId()), REQUEST_EXAMPLE_FILE_NAME);
            if (!requestExample.isPresent())
                requestExample = exampleFromFile(normalizeOperationFileName(operation.getTitle()), REQUEST_EXAMPLE_FILE_NAME);
            responseExample = exampleFromFile(normalizeOperationFileName(operation.getId()), RESPONSE_EXAMPLE_FILE_NAME);
            if (!responseExample.isPresent())
                responseExample = exampleFromFile(normalizeOperationFileName(operation.getTitle()), RESPONSE_EXAMPLE_FILE_NAME);
        }

        generatedRequestExampleMap = ExamplesUtil.generateRequestExampleMap(operation, swagger.getDefinitions(), markupDocBuilder);
        generatedResponseExampleMap = ExamplesUtil.generateResponseExampleMap(operation.getOperation(), swagger.getDefinitions(), markupDocBuilder);

        if(curlExample.isPresent()){
            addOperationSectionTitle(EXAMPLE_CURL, docBuilder);
            docBuilder.paragraph(curlExample.get());
        }

        if (requestExample.isPresent() || generatedRequestExampleMap.isPresent()) {
            addOperationSectionTitle(EXAMPLE_REQUEST, docBuilder);
            if(requestExample.isPresent()){
                docBuilder.paragraph(requestExample.get());
            }
            if (generatedRequestExampleMap.isPresent() && generatedRequestExampleMap.get().size() > 0) {
                for (Map.Entry<String, Object> request : generatedRequestExampleMap.get().entrySet()) {
                    docBuilder.sectionTitleLevel4(REQUEST + ' ' + request.getKey() + ":");
                    //Workaround to support text formatting in listing block:
                    docBuilder.textLine("[subs=\"quotes\"]");
                    docBuilder.listing(Json.pretty(request.getValue()));
                }
            }
        }

        if (responseExample.isPresent() || generatedResponseExampleMap.isPresent()) {
            addOperationSectionTitle(EXAMPLE_RESPONSE, docBuilder);
            if (responseExample.isPresent()){
                docBuilder.paragraph(responseExample.get());
            }
            if (generatedResponseExampleMap.isPresent() && generatedResponseExampleMap.get().size() > 0) {
                for (Map.Entry<String, Object> response : generatedResponseExampleMap.get().entrySet()) {
                    docBuilder.sectionTitleLevel4(RESPONSE + ' ' + response.getKey() + ':');
                    docBuilder.listing(Json.pretty(response.getValue()));
                }
            }
        }
    }

    /**
     * Reads an example
     *
     * @param exampleFolder the name of the folder where the example file resides
     * @param exampleFileName the name of the example file
     * @return the content of the file
     */
    private Optional<String> exampleFromFile(String exampleFolder, String exampleFileName) {
        for (String fileNameExtension : markupLanguage.getFileNameExtensions()) {
            java.nio.file.Path path = Paths.get(examplesFolderPath, exampleFolder, exampleFileName + fileNameExtension);
            if (Files.isReadable(path)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Example file processed: {}", path);
                }
                try {
                    return Optional.of(FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim());
                } catch (IOException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(String.format("Failed to read example file: %s", path),  e);
                    }
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Example file is not readable: {}", path);
                }
            }
        }
        if (logger.isWarnEnabled()) {
            logger.warn("No example file found with correct file name extension in folder: {}", Paths.get(examplesFolderPath, exampleFolder));
        }
        return Optional.absent();
    }

    /**
     * Builds the security section of a Swagger Operation.
     *
     * @param operation the Swagger Operation
     * @param docBuilder the MarkupDocBuilder document builder
     */
    private void securitySchemeSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        List<Map<String, List<String>>> securitySchemes = operation.getOperation().getSecurity();
        if (CollectionUtils.isNotEmpty(securitySchemes)) {
            addOperationSectionTitle(SECURITY, docBuilder);
            Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(TYPE_COLUMN, 1),
                    new MarkupTableColumn(NAME_COLUMN, 1),
                    new MarkupTableColumn(SCOPES_COLUMN, 6));
            for (Map<String, List<String>> securityScheme : securitySchemes) {
                for (Map.Entry<String, List<String>> securityEntry : securityScheme.entrySet()) {
                    String securityKey = securityEntry.getKey();
                    String type = "UNKNOWN";
                    if (securityDefinitions != null && securityDefinitions.containsKey(securityKey)) {
                        type = securityDefinitions.get(securityKey).getType();
                    }
                    List<String> content = Arrays.asList(type, docBuilder.crossReferenceAsString(null,
                            securityKey, securityKey),
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
     * @param descriptionFolder the name of the folder where the description file resides
     * @param descriptionFileName the name of the description file
     * @return the content of the file
     */
    private Optional<String> handWrittenOperationDescription(String descriptionFolder, String descriptionFileName){
        for (String fileNameExtension : markupLanguage.getFileNameExtensions()) {
            java.nio.file.Path path = Paths.get(descriptionsFolderPath, descriptionFolder, descriptionFileName + fileNameExtension);
            if (Files.isReadable(path)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Description file processed: {}", path);
                }
                try {
                    return Optional.of(FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim());
                } catch (IOException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(String.format("Failed to read description file: %s", path),  e);
                    }
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Description file is not readable: {}", path);
                }
            }
        }
        if (logger.isWarnEnabled()) {
            logger.warn("No description file found with correct file name extension in folder: {}", Paths.get(descriptionsFolderPath, descriptionFolder));
        }
        return Optional.absent();
    }

    private List<Type> responsesSection(PathOperation operation, MarkupDocBuilder docBuilder) {
        Map<String, Response> responses = operation.getOperation().getResponses();
        List<Type> localDefinitions = new ArrayList<>();
        if(MapUtils.isNotEmpty(responses)){
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(HTTP_CODE_COLUMN, 1),
                    new MarkupTableColumn(DESCRIPTION_COLUMN, 6),
                    new MarkupTableColumn(SCHEMA_COLUMN, 1));
            for(Map.Entry<String, Response> entry : responses.entrySet()){
                Response response = entry.getValue();
                if(response.getSchema() != null){
                    Property property = response.getSchema();
                    Type type = PropertyUtils.getType(property, new DefinitionDocumentResolverFromOperation());
                    if (this.inlineSchemaDepthLevel > 0 && type instanceof ObjectType) {
                        if (MapUtils.isNotEmpty(((ObjectType) type).getProperties())) {
                            String localTypeName = RESPONSE + " " + entry.getKey();

                            type.setName(localTypeName);
                            type.setUniqueName(uniqueTypeName(localTypeName));
                            localDefinitions.add(type);
                            type = new RefType(type);
                        }
                    }
                    cells.add(Arrays.asList(entry.getKey(), response.getDescription(), type.displaySchema(markupDocBuilder)));
                }else{
                    cells.add(Arrays.asList(entry.getKey(), response.getDescription(), NO_CONTENT));
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
     * @param title inline schema title
     * @param anchor inline schema anchor
     * @param docBuilder the docbuilder do use for output
     */
    private void addInlineDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.anchor(anchor);
        docBuilder.newLine();
        docBuilder.boldTextLine(title);
    }

    /**
     * Builds inline schema definitions
     * @param definitions all inline definitions to display
     * @param depth current inline schema depth
     * @param docBuilder the docbuilder do use for output
     */
    private void inlineDefinitions(List<Type> definitions, int depth, MarkupDocBuilder docBuilder) {
        if(CollectionUtils.isNotEmpty(definitions)){
            for (Type definition: definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), docBuilder);

                List<Type> localDefinitions = typeProperties(definition, depth, new PropertyDescriptor(definition), new DefinitionDocumentResolverFromOperation(), docBuilder);
                for (Type localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), depth - 1, docBuilder);
            }
        }

    }

    /**
     * Overrides definition document resolver functor for inter-document cross-references from operations files.
     * This implementation adapt the relative paths to definitions files
     */
    class DefinitionDocumentResolverFromOperation extends DefinitionDocumentResolverDefault {

        public DefinitionDocumentResolverFromOperation() {}

        public String apply(String definitionName) {
            String defaultResolver = super.apply(definitionName);

            if (defaultResolver != null && separatedOperationsEnabled)
                return interDocumentCrossReferencesPrefix + new File("..", defaultResolver).getPath();
            else
                return defaultResolver;
        }
    }
}
