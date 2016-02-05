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
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static io.github.robwin.swagger2markup.utils.TagUtils.*;
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
    private final String RESPONSES;
    private final String EXAMPLE_CURL;
    private final String EXAMPLE_REQUEST;
    private final String EXAMPLE_RESPONSE;
    private final String TYPE_COLUMN;
    private final String HTTP_CODE_COLUMN;
    private static final String REQUEST_EXAMPLE_FILE_NAME = "http-request";
    private static final String RESPONSE_EXAMPLE_FILE_NAME = "http-response";
    private static final String CURL_EXAMPLE_FILE_NAME = "curl-request";
    private static final String DESCRIPTION_FOLDER_NAME = "paths";
    private static final String DESCRIPTION_FILE_NAME = "description";
    private static final String SEPARATED_PATHS_FOLDER_NAME = "paths";
    private final String PARAMETER;
    private static final Pattern FILENAME_FORBIDDEN_PATTERN = Pattern.compile("[^0-9A-Za-z-_]+");

    private boolean examplesEnabled;
    private String examplesFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;
    private final GroupBy pathsGroupedBy;
    private final int inlineSchemaDepthLevel;
    private final Comparator<String> tagComparator;
    private final Comparator<String> pathComparator;
    private final Comparator<HttpMethod> pathMethodComparator;
    private boolean separatedPathsEnabled;
    private String outputDirectory;

    public PathsDocument(Swagger2MarkupConfig swagger2MarkupConfig, String outputDirectory){
        super(swagger2MarkupConfig);

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
        TYPE_COLUMN = labels.getString("type_column");
        HTTP_CODE_COLUMN = labels.getString("http_code_column");
        PARAMETER = labels.getString("parameter");

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

        this.separatedPathsEnabled = swagger2MarkupConfig.isSeparatedPaths();
        if(this.separatedPathsEnabled){
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated path files is enabled.");
            }
            Validate.notEmpty(outputDirectory, "Output directory is required for separated path files!");
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated path files is disabled.");
            }
        }
        this.outputDirectory = outputDirectory;
        tagComparator = swagger2MarkupConfig.getTagComparator();
        pathComparator = swagger2MarkupConfig.getPathComparator();
        pathMethodComparator = swagger2MarkupConfig.getPathMethodComparator();
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
            if(pathsGroupedBy == GroupBy.AS_IS || pathsGroupedBy == GroupBy.SORTED){
                this.markupDocBuilder.sectionTitleLevel1(PATHS);

                Set<Pair<String, Path>> sortedPaths;
                if (pathsGroupedBy == GroupBy.SORTED) {
                    sortedPaths = new TreeSet<>(new PathUtils.PathPairComparator(this.pathComparator));
                } else {
                    sortedPaths = new LinkedHashSet<>();
                }
                for (Map.Entry<String, Path> e : paths.entrySet()) {
                    sortedPaths.add(Pair.of(e.getKey(), e.getValue()));
                }

                for (Pair<String, Path> pathEntry : sortedPaths) {
                    createPathSections(pathEntry.getKey(), pathEntry.getValue());
                }
            } else {
                this.markupDocBuilder.sectionTitleLevel1(RESOURCES);
                Multimap<String, Pair<String, Path>> pathsGroupedByTag = groupPathsByTag(paths, tagComparator, pathComparator);
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

        Map<HttpMethod, Operation> operationsMap = new TreeMap<>(pathMethodComparator);
        operationsMap.putAll(path.getOperationMap());

        for(Map.Entry<HttpMethod, Operation> operationEntry : operationsMap.entrySet()){
            String methodAndPath = operationEntry.getKey() + " " + pathUrl;
            processPath(methodAndPath, operationEntry.getValue());
        }
    }

     private void processPath(String methodAndPath, Operation operation) {

         path(methodAndPath, operation, this.markupDocBuilder);

        if (separatedPathsEnabled) {
            MarkupDocBuilder pathDocBuilder = MarkupDocBuilders.documentBuilder(markupLanguage);
            path(methodAndPath, operation, pathDocBuilder);
            String pathFileName = operation.getOperationId();
            if (pathFileName == null)
                pathFileName = methodAndPath;
            pathFileName = FILENAME_FORBIDDEN_PATTERN.matcher(pathFileName).replaceAll("_").toLowerCase();
            try {
                pathDocBuilder.writeToFile(Paths.get(outputDirectory, SEPARATED_PATHS_FOLDER_NAME).toString(), pathFileName, StandardCharsets.UTF_8);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Failed to write path file: %s", pathFileName), e);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Separate path file produced: {}", pathFileName);
            }
        }
    }

    /**
     * Builds an operation.
     *
     * @param methodAndPath the Method of the operation and the URL of the path
     * @param operation the Swagger Operation
     */
    private void path(String methodAndPath, Operation operation, MarkupDocBuilder docBuilder) {
        if(operation != null){
            pathTitle(methodAndPath, operation, docBuilder);
            descriptionSection(operation, docBuilder);
            inlineDefinitions(parametersSection(operation, docBuilder), inlineSchemaDepthLevel, docBuilder);
            inlineDefinitions(responsesSection(operation, docBuilder), inlineSchemaDepthLevel, docBuilder);
            consumesSection(operation, docBuilder);
            producesSection(operation, docBuilder);
            tagsSection(operation, docBuilder);
            examplesSection(operation, docBuilder);
        }
    }

    /**
     * Adds the path title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the path.
     *
     * @param methodAndPath the Method of the operation and the URL of the path
     * @param operation the Swagger Operation
     */
    private void pathTitle(String methodAndPath, Operation operation, MarkupDocBuilder docBuilder) {
        String summary = operation.getSummary();
        String title;
        if(isNotBlank(summary)) {
            title = summary;
            addPathTitle(title, docBuilder);
            docBuilder.listing(methodAndPath);
        }else{
            addPathTitle(methodAndPath, docBuilder);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Path processed: {}", methodAndPath);
        }
    }

    /**
     * Adds a path title to the document.
     *
     * @param title the path title
     */
    private void addPathTitle(String title, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS || pathsGroupedBy == GroupBy.SORTED){
            docBuilder.sectionTitleLevel2(title);
        }else{
            docBuilder.sectionTitleLevel3(title);
        }
    }

    /**
     * Adds a path section title to the document.
     *
     * @param title the path title
     */
    private void addPathSectionTitle(String title, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS || pathsGroupedBy == GroupBy.SORTED){
            docBuilder.sectionTitleLevel3(title);
        }else{
            docBuilder.sectionTitleLevel4(title);
        }
    }

    /**
     * Adds a path description to the document.
     *
     * @param operation the Swagger Operation
     */
    private void descriptionSection(Operation operation, MarkupDocBuilder docBuilder) {
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            if(isNotBlank(summary)) {
                String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                Optional<String> description = handWrittenPathDescription(operationFolder, DESCRIPTION_FILE_NAME);
                if(description.isPresent()){
                    pathDescription(description.get(), docBuilder);
                }else{
                    if (logger.isInfoEnabled()) {
                        logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                    }
                    pathDescription(operation.getDescription(), docBuilder);
                }
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read, because summary of operation is empty. Trying to use description from Swagger source.");
                }
                pathDescription(operation.getDescription(), docBuilder);
            }
        }else {
            pathDescription(operation.getDescription(), docBuilder);
        }
    }

    private void pathDescription(String description, MarkupDocBuilder docBuilder) {
        if (isNotBlank(description)) {
            addPathSectionTitle(DESCRIPTION, docBuilder);
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
                Type type = ParameterUtils.getType(parameter);
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
                        parameterDescription(operation, parameter, docBuilder),
                        Boolean.toString(parameter.getRequired()),
                        type.displaySchema(markupDocBuilder),
                        ParameterUtils.getDefaultValue(parameter));
               cells.add(content);
            }
            addPathSectionTitle(PARAMETERS, docBuilder);
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
    private String parameterDescription(Operation operation, Parameter parameter, MarkupDocBuilder docBuilder){
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
            String parameterName = parameter.getName();
            if(isNotBlank(operationFolder) && isNotBlank(parameterName)) {
                Optional<String> description = handWrittenPathDescription(operationFolder + "/" + parameterName, DESCRIPTION_FILE_NAME);
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
            addPathSectionTitle(CONSUMES, docBuilder);
            docBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(Operation operation, MarkupDocBuilder docBuilder) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            addPathSectionTitle(PRODUCES, docBuilder);
            docBuilder.unorderedList(produces);
        }
    }

    private void tagsSection(Operation operation, MarkupDocBuilder docBuilder) {
        if(pathsGroupedBy == GroupBy.AS_IS || pathsGroupedBy == GroupBy.SORTED) {
            List<String> tags = operation.getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                addPathSectionTitle(TAGS, docBuilder);
                Set<String> orderedTags = new TreeSet<>(this.tagComparator);
                orderedTags.addAll(tags);
                this.markupDocBuilder.unorderedList(new ArrayList<>(orderedTags));
            }
        }
    }

    /**
     * Builds the example section of a Swagger Operation. Tries to load the examples from
     * curl-request.adoc, http-request.adoc and http-response.adoc or
     * curl-request.md, http-request.md and http-response.md.
     *
     * @param operation the Swagger Operation
     */
    private void examplesSection(Operation operation, MarkupDocBuilder docBuilder) {
        if(examplesEnabled){
            String summary = operation.getSummary();
            if(isNotBlank(summary)) {
                String exampleFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                Optional<String> curlExample = example(exampleFolder, CURL_EXAMPLE_FILE_NAME);
                if(curlExample.isPresent()){
                    addPathSectionTitle(EXAMPLE_CURL, docBuilder);
                    docBuilder.paragraph(curlExample.get());
                }

                Optional<String> requestExample = example(exampleFolder, REQUEST_EXAMPLE_FILE_NAME);
                if(requestExample.isPresent()){
                    addPathSectionTitle(EXAMPLE_REQUEST, docBuilder);
                    docBuilder.paragraph(requestExample.get());
                }
                Optional<String> responseExample = example(exampleFolder, RESPONSE_EXAMPLE_FILE_NAME);
                if(responseExample.isPresent()){
                    addPathSectionTitle(EXAMPLE_RESPONSE, docBuilder);
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
     * Reads a hand-written description
     *
     * @param descriptionFolder the name of the folder where the description file resides
     * @param descriptionFileName the name of the description file
     * @return the content of the file
     */
    private Optional<String> handWrittenPathDescription(String descriptionFolder, String descriptionFileName){
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
                    Type type = PropertyUtils.getType(property);
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
            addPathSectionTitle(RESPONSES, docBuilder);
            docBuilder.tableWithColumnSpecs(cols, cells);
        }
        return localDefinitions;
    }

    /**
     * Inline definitions should never been referenced in TOC, so they are just text.
     */
    private void addInlineDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.anchor(anchor, null);
        docBuilder.newLine();
        docBuilder.boldTextLine(title);
    }

    private void inlineDefinitions(List<Type> definitions, int depth, MarkupDocBuilder docBuilder) {
        if(CollectionUtils.isNotEmpty(definitions)){
            for (Type definition: definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), docBuilder);
                List<Type> localDefinitions = typeProperties(definition, depth, new PropertyDescriptor(definition), this.markupDocBuilder);
                for (Type localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), depth - 1, docBuilder);
            }
        }

    }
}
