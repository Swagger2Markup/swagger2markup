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
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.github.robwin.swagger2markup.utils.ParameterUtils;
import io.github.robwin.swagger2markup.utils.PathUtils;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import io.swagger.models.*;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static io.github.robwin.swagger2markup.utils.TagUtils.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupDocument {

    private final String RESPONSE;
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

    private boolean examplesEnabled;
    private String examplesFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;
    private final GroupBy pathsGroupedBy;
    private final int inlineSchemaDepthLevel;
    private final Comparator<String> tagOrdering;
    private final Comparator<String> pathOrdering;
    private final Comparator<HttpMethod> pathMethodOrdering;
    private boolean separatedOperationsEnabled;
    private String separatedOperationsFolder;
    private String pathsDocument;


    public PathsDocument(Swagger2MarkupConfig swagger2MarkupConfig, String outputDirectory){
        super(swagger2MarkupConfig, outputDirectory);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        RESPONSE = labels.getString("response");
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
            this.examplesEnabled = true;
            this.examplesFolderPath = swagger2MarkupConfig.getExamplesFolderPath();
        }
        if(isNotBlank(swagger2MarkupConfig.getDescriptionsFolderPath())){
            this.handWrittenDescriptionsEnabled = true;
            this.descriptionsFolderPath = swagger2MarkupConfig.getDescriptionsFolderPath() + "/" + DESCRIPTION_FOLDER_NAME;
        }

        if(examplesEnabled){
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
        pathOrdering = swagger2MarkupConfig.getPathOrdering();
        pathMethodOrdering = swagger2MarkupConfig.getPathMethodOrdering();
    }

    /**
     * Builds the paths markup document.
     *
     * @return the the paths markup document
     */
    @Override
    public MarkupDocument build(){
        paths();
        return this;
    }

    /**
     * Builds all paths of the Swagger model. Either grouped as-is or by tags.
     */
    private void paths(){
        Map<String, Path> paths = swagger.getPaths();
        if(MapUtils.isNotEmpty(paths)) {
            if(pathsGroupedBy == GroupBy.AS_IS){
                this.markupDocBuilder.sectionTitleLevel1(PATHS);

                Set<Pair<String, Path>> sortedPaths;
                if (this.pathOrdering == null)
                    sortedPaths = new LinkedHashSet<>();
                else
                    sortedPaths = new TreeSet<>(new PathUtils.PathPairComparator(this.pathOrdering));
                for (Map.Entry<String, Path> e : paths.entrySet()) {
                    sortedPaths.add(Pair.of(e.getKey(), e.getValue()));
                }

                for (Pair<String, Path> pathEntry : sortedPaths) {
                    createPathSections(pathEntry.getKey(), pathEntry.getValue());
                }
            } else {
                this.markupDocBuilder.sectionTitleLevel1(RESOURCES);
                Multimap<String, Pair<String, Path>> pathsGroupedByTag = groupPathsByTag(paths, tagOrdering, pathOrdering);
                Map<String, Tag> tagsMap = convertTagsListToMap(swagger.getTags());
                for(String tagName : pathsGroupedByTag.keySet()){
                    this.markupDocBuilder.sectionTitleLevel2(WordUtils.capitalize(tagName));
                    Optional<String> tagDescription = getTagDescription(tagsMap, tagName);
                    if(tagDescription.isPresent()) {
                        this.markupDocBuilder.paragraph(tagDescription.get());
                    }
                    Collection<Pair<String, Path>> pathsOfTag = pathsGroupedByTag.get(tagName);
                    for(Pair<String, Path> pathPair : pathsOfTag){
                        Path path = pathPair.getValue();
                        if(path != null) {
                            createPathSections(pathPair.getKey(), path);
                        }
                    }
                }
            }
        }
    }

    private void createPathSections(String pathUrl, Path path){

        Map<HttpMethod, Operation> operationsMap;
        if (pathMethodOrdering == null)
            operationsMap = new LinkedHashMap<>();
        else
            operationsMap = new TreeMap<>(pathMethodOrdering);
        operationsMap.putAll(path.getOperationMap());

        for(Map.Entry<HttpMethod, Operation> operationEntry : operationsMap.entrySet()){
            String methodAndPath = operationEntry.getKey() + " " + pathUrl;
            processOperation(methodAndPath, operationEntry.getValue());
        }
    }

    /**
     * Create a normalized filename for a separated operation file
     * @param methodAndPath method and path of the operation
     * @param operation operation
     * @return a normalized filename for the separated operation file
     */
    private String normalizeOperationFileName(String methodAndPath, Operation operation) {
        String operationFileName = operation.getOperationId();

        if (operationFileName == null)
            operationFileName = methodAndPath;
        operationFileName = FILENAME_FORBIDDEN_PATTERN.matcher(operationFileName).replaceAll("_").toLowerCase();

        return operationFileName;
    }

    /**
     * Create the operation filename depending on the generation mode
     * @param methodAndPath method and path of the operation
     * @param operation operation
     * @return operation filename
     */
    private String resolveOperationDocument(String methodAndPath, Operation operation) {
        if (this.separatedOperationsEnabled)
            return new File(this.separatedOperationsFolder, this.markupDocBuilder.addfileExtension(normalizeOperationFileName(methodAndPath, operation))).getPath();
        else
            return this.markupDocBuilder.addfileExtension(this.pathsDocument);
    }

    /**
     * Generate operations depending on the generation mode.
     * @param methodAndPath method and path of the operation
     * @param operation operation
     */
    private void processOperation(String methodAndPath, Operation operation) {
        if (separatedOperationsEnabled) {
            MarkupDocBuilder pathDocBuilder = MarkupDocBuilders.documentBuilder(markupLanguage);
            operation(methodAndPath, operation, pathDocBuilder);
            File operationFile = new File(outputDirectory, resolveOperationDocument(methodAndPath, operation));

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

            operationRef(methodAndPath, operation, this.markupDocBuilder);

        } else {
            operation(methodAndPath, operation, this.markupDocBuilder);
        }
    }


    /**
     * Returns the operation name depending on available informations.
     * The summary is used to name the operation, or else the operation summary is used.
     * @param methodAndPath method and path of the operation
     * @param operation operation
     * @return operation name
     */
    private String operationName(String methodAndPath, Operation operation) {
        String operationName = operation.getSummary();
        if(isBlank(operationName)) {
            operationName = methodAndPath;
        }
        return operationName;
    }

    /**
     * Builds an operation.
     *
     * @param methodAndPath the Method of the operation and the URL of the operation
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operation(String methodAndPath, Operation operation, MarkupDocBuilder docBuilder) {
        if(operation != null){
            operationTitle(methodAndPath, operation, docBuilder);
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
     * @param methodAndPath the Method of the operation and the URL of the operation
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operationRef(String methodAndPath, Operation operation, MarkupDocBuilder docBuilder) {
        String document = resolveOperationDocument(methodAndPath, operation);
        String operationName = operationName(methodAndPath, operation);

        addOperationTitle(docBuilder.crossReferenceAsString(document, operationName, operationName), docBuilder);
    }

    /**
     * Adds the operation title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the operation.
     *
     * @param methodAndPath the Method of the operation and the URL of the operation
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void operationTitle(String methodAndPath, Operation operation, MarkupDocBuilder docBuilder) {
        String operationName = operationName(methodAndPath, operation);

        addOperationTitle(operationName, docBuilder);
        if(operationName.equals(operation.getSummary())) {
            docBuilder.listing(methodAndPath);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Path processed: {}", methodAndPath);
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
     *
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void descriptionSection(Operation operation, MarkupDocBuilder docBuilder) {
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            if(isNotBlank(summary)) {
                String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                Optional<String> description = handWrittenOperationDescription(operationFolder, DESCRIPTION_FILE_NAME);
                if(description.isPresent()){
                    operationDescription(description.get(), docBuilder);
                }else{
                    if (logger.isInfoEnabled()) {
                        logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                    }
                    operationDescription(operation.getDescription(), docBuilder);
                }
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read, because summary of operation is empty. Trying to use description from Swagger source.");
                }
                operationDescription(operation.getDescription(), docBuilder);
            }
        }else {
            operationDescription(operation.getDescription(), docBuilder);
        }
    }

    private void operationDescription(String description, MarkupDocBuilder docBuilder) {
        if (isNotBlank(description)) {
            addOperationSectionTitle(DESCRIPTION, docBuilder);
            docBuilder.paragraph(description);
        }
    }

    private List<Type> parametersSection(Operation operation, MarkupDocBuilder docBuilder) {
        List<Parameter> parameters = operation.getParameters();
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
     * If hand-written descriptions are enabled, it tries to load the description from a file.
     * If the file cannot be read, the description the parameter is returned.
     *
     * @param operation the Swagger Operation
     * @param parameter the Swagger Parameter
     * @return the description of a parameter.
     */
    private String parameterDescription(Operation operation, Parameter parameter){
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
            String parameterName = parameter.getName();
            if(isNotBlank(operationFolder) && isNotBlank(parameterName)) {
                Optional<String> description = handWrittenOperationDescription(operationFolder + "/" + parameterName, DESCRIPTION_FILE_NAME);
                if(description.isPresent()){
                    return description.get();
                }
                else{
                    if (logger.isWarnEnabled()) {
                        logger.warn("Hand-written description file cannot be read. Trying to use description from Swagger source.");
                    }
                    return defaultString(parameter.getDescription());
                }
            }else{
                if (logger.isWarnEnabled()) {
                    logger.warn("Hand-written description file cannot be read, because summary of operation or name of parameter is empty. Trying to use description from Swagger source.");
                }
                return defaultString(parameter.getDescription());
            }
        }else {
            return defaultString(parameter.getDescription());
        }
    }

    private void consumesSection(Operation operation, MarkupDocBuilder docBuilder) {
        List<String> consumes = operation.getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            addOperationSectionTitle(CONSUMES, docBuilder);
            docBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(Operation operation, MarkupDocBuilder docBuilder) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            addOperationSectionTitle(PRODUCES, docBuilder);
            docBuilder.unorderedList(produces);
        }
    }

    private void tagsSection(Operation operation, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS) {
            List<String> tags = operation.getTags();
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
     *
     * @param operation the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void examplesSection(Operation operation, MarkupDocBuilder docBuilder) {
        if(examplesEnabled){
            String summary = operation.getSummary();
            if(isNotBlank(summary)) {
                String exampleFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                Optional<String> curlExample = example(exampleFolder, CURL_EXAMPLE_FILE_NAME);
                if(curlExample.isPresent()){
                    addOperationSectionTitle(EXAMPLE_CURL, docBuilder);
                    docBuilder.paragraph(curlExample.get());
                }

                Optional<String> requestExample = example(exampleFolder, REQUEST_EXAMPLE_FILE_NAME);
                if(requestExample.isPresent()){
                    addOperationSectionTitle(EXAMPLE_REQUEST, docBuilder);
                    docBuilder.paragraph(requestExample.get());
                }
                Optional<String> responseExample = example(exampleFolder, RESPONSE_EXAMPLE_FILE_NAME);
                if(responseExample.isPresent()){
                    addOperationSectionTitle(EXAMPLE_RESPONSE, docBuilder);
                    docBuilder.paragraph(responseExample.get());
                }
            }else{
                if (logger.isWarnEnabled()) {
                    logger.warn("Example file cannot be read, because summary of operation is empty.");
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
    private Optional<String> example(String exampleFolder, String exampleFileName) {
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
    private void securitySchemeSection(Operation operation, MarkupDocBuilder docBuilder) {
        List<Map<String, List<String>>> securitySchemes = operation.getSecurity();
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

    private List<Type> responsesSection(Operation operation, MarkupDocBuilder docBuilder) {
        Map<String, Response> responses = operation.getResponses();
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
        docBuilder.anchor(anchor, null);
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
