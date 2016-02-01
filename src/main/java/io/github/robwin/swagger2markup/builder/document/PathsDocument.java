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
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.github.robwin.swagger2markup.utils.ParameterUtils;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static io.github.robwin.swagger2markup.utils.TagUtils.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupDocument {

    private final String PATHS;
    private final String RESOURCES;
    private final String PARAMETERS;
    private final String RESPONSES;
    private final String EXAMPLE_CURL;
    private final String EXAMPLE_REQUEST;
    private final String EXAMPLE_RESPONSE;
    private final String TYPE_COLUMN;
    private final String HTTP_CODE_COLUMN;
    private final String REQUEST_EXAMPLE_FILE_NAME;
    private final String RESPONSE_EXAMPLE_FILE_NAME;
    private final String CURL_EXAMPLE_FILE_NAME;
    private final String DESCRIPTION_FILE_NAME;
    private final String PARAMETER;
    private final String DEFINITIONS;


    private boolean examplesEnabled;
    private String examplesFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;
    private final GroupBy pathsGroupedBy;

    public PathsDocument(Swagger2MarkupConfig swagger2MarkupConfig){
        super(swagger2MarkupConfig);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        PATHS = labels.getString("paths");
        RESOURCES = labels.getString("resources");
        PARAMETERS = labels.getString("parameters");
        RESPONSES = labels.getString("responses");
        EXAMPLE_CURL = labels.getString("example_curl");
        EXAMPLE_REQUEST = labels.getString("example_request");
        EXAMPLE_RESPONSE = labels.getString("example_response");
        TYPE_COLUMN = labels.getString("type_column");
        HTTP_CODE_COLUMN = labels.getString("http_code_column");
        REQUEST_EXAMPLE_FILE_NAME = labels.getString("request_example_file_name");
        RESPONSE_EXAMPLE_FILE_NAME = labels.getString("response_example_file_name");
        CURL_EXAMPLE_FILE_NAME = labels.getString("curl_example_file_name");
        DESCRIPTION_FILE_NAME = labels.getString("description_file_name");
        PARAMETER = labels.getString("parameter");
        DEFINITIONS = labels.getString("definitions");

        this.pathsGroupedBy = swagger2MarkupConfig.getPathsGroupedBy();
        if(isNotBlank(swagger2MarkupConfig.getExamplesFolderPath())){
            this.examplesEnabled = true;
            this.examplesFolderPath = swagger2MarkupConfig.getExamplesFolderPath();
        }
        if(isNotBlank(swagger2MarkupConfig.getDescriptionsFolderPath())){
            this.handWrittenDescriptionsEnabled = true;
            this.descriptionsFolderPath = swagger2MarkupConfig.getDescriptionsFolderPath() + "/" + PATHS.toLowerCase();
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
            if(pathsGroupedBy.equals(GroupBy.AS_IS)){
                this.markupDocBuilder.sectionTitleLevel1(PATHS);
                for (Map.Entry<String, Path> pathEntry : paths.entrySet()) {
                    Path path = pathEntry.getValue();
                    if(path != null) {
                        createPathSections(pathEntry.getKey(), path);
                    }
                }
            }else{
                this.markupDocBuilder.sectionTitleLevel1(RESOURCES);
                Multimap<String, Pair<String, Path>> pathsGroupedByTag = groupPathsByTag(paths);
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
        for(Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()){
            String methodAndPath = operationEntry.getKey() + " " + pathUrl;
            path(methodAndPath, operationEntry.getValue());
        }
    }

    /**
     * Builds a path.
     *
     * @param methodAndPath the Method of the operation and the URL of the path
     * @param operation the Swagger Operation
     */
    private void path(String methodAndPath, Operation operation) {
        if(operation != null){
            List<Type> localDefinitions = new ArrayList<>();

            pathTitle(methodAndPath, operation);
            descriptionSection(operation);
            localDefinitions.addAll(parametersSection(operation));
            localDefinitions.addAll(responsesSection(operation));
            consumesSection(operation);
            producesSection(operation);
            tagsSection(operation);
            examplesSection(operation);
            localDefinitionsSection(localDefinitions);
        }
    }

    /**
     * Adds the path title to the document. If the operation has a summary, the title is the summary.
     * Otherwise the title is the method of the operation and the URL of the path.
     *
     * @param methodAndPath the Method of the operation and the URL of the path
     * @param operation the Swagger Operation
     */
    private void pathTitle(String methodAndPath, Operation operation) {
        String summary = operation.getSummary();
        String title;
        if(isNotBlank(summary)) {
            title = summary;
            addPathTitle(title);
            this.markupDocBuilder.listing(methodAndPath);
        }else{
            addPathTitle(methodAndPath);
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
    private void addPathTitle(String title) {
        if(pathsGroupedBy.equals(GroupBy.AS_IS)){
            this.markupDocBuilder.sectionTitleLevel2(title);
        }else{
            this.markupDocBuilder.sectionTitleLevel3(title);
        }
    }

    /**
     * Adds a path section title to the document.
     *
     * @param title the path title
     */
    private void addPathSectionTitle(String title) {
        if(pathsGroupedBy.equals(GroupBy.AS_IS)){
            this.markupDocBuilder.sectionTitleLevel3(title);
        }else{
            this.markupDocBuilder.sectionTitleLevel4(title);
        }
    }

    /**
     * Adds a path description to the document.
     *
     * @param operation the Swagger Operation
     */
    private void descriptionSection(Operation operation) {
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            if(isNotBlank(summary)) {
                String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                Optional<String> description = handWrittenPathDescription(operationFolder, DESCRIPTION_FILE_NAME);
                if(description.isPresent()){
                    pathDescription(description.get());
                }else{
                    if (logger.isInfoEnabled()) {
                        logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                    }
                    pathDescription(operation.getDescription());
                }
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read, because summary of operation is empty. Trying to use description from Swagger source.");
                }
                pathDescription(operation.getDescription());
            }
        }else {
            pathDescription(operation.getDescription());
        }
    }

    private void pathDescription(String description) {
        if (isNotBlank(description)) {
            addPathSectionTitle(DESCRIPTION);
            this.markupDocBuilder.paragraph(description);
        }
    }

    private List<Type> parametersSection(Operation operation) {
        List<Parameter> parameters = operation.getParameters();
        List<Type> localDefinitions = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(parameters)){
            List<String> headerAndContent = new ArrayList<>();
            // Table header row
            List<String> header = Arrays.asList(TYPE_COLUMN, NAME_COLUMN, DESCRIPTION_COLUMN, REQUIRED_COLUMN, SCHEMA_COLUMN, DEFAULT_COLUMN);
            headerAndContent.add(join(header, DELIMITER));
            for(Parameter parameter : parameters){
                Type type = ParameterUtils.getType(parameter);
                if (type instanceof ObjectType) {
                    String localTypeName = parameter.getName();
                    type.setName(localTypeName);
                    type.setUniqueName(uniqueTypeName(localTypeName));
                    localDefinitions.add(type);

                    type = new RefType(type);
                }
                String parameterType = WordUtils.capitalize(parameter.getIn() + PARAMETER);
                // Table content row
                List<String> content = Arrays.asList(
                        parameterType,
                        parameter.getName(),
                        parameterDescription(operation, parameter),
                        Boolean.toString(parameter.getRequired()),
                        typeSchema(type),
                        ParameterUtils.getDefaultValue(parameter));
                headerAndContent.add(join(content, DELIMITER));
            }
            addPathSectionTitle(PARAMETERS);
            this.markupDocBuilder.tableWithHeaderRow(headerAndContent);
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

    private void consumesSection(Operation operation) {
        List<String> consumes = operation.getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            addPathSectionTitle(CONSUMES);
            this.markupDocBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(Operation operation) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            addPathSectionTitle(PRODUCES);
            this.markupDocBuilder.unorderedList(produces);
        }
    }

    private void tagsSection(Operation operation) {
        if(pathsGroupedBy.equals(GroupBy.AS_IS)) {
            List<String> tags = operation.getTags();
            if (CollectionUtils.isNotEmpty(tags)) {
                addPathSectionTitle(TAGS);
                this.markupDocBuilder.unorderedList(tags);
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
    private void examplesSection(Operation operation) {
        if(examplesEnabled){
            String summary = operation.getSummary();
            if(isNotBlank(summary)) {
                String exampleFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                Optional<String> curlExample = example(exampleFolder, CURL_EXAMPLE_FILE_NAME);
                if(curlExample.isPresent()){
                    addPathSectionTitle(EXAMPLE_CURL);
                    this.markupDocBuilder.paragraph(curlExample.get());
                }

                Optional<String> requestExample = example(exampleFolder, REQUEST_EXAMPLE_FILE_NAME);
                if(requestExample.isPresent()){
                    addPathSectionTitle(EXAMPLE_REQUEST);
                    this.markupDocBuilder.paragraph(requestExample.get());
                }
                Optional<String> responseExample = example(exampleFolder, RESPONSE_EXAMPLE_FILE_NAME);
                if(responseExample.isPresent()){
                    addPathSectionTitle(EXAMPLE_RESPONSE);
                    this.markupDocBuilder.paragraph(responseExample.get());
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

    private List<Type> responsesSection(Operation operation) {
        Map<String, Response> responses = operation.getResponses();
        List<Type> localDefinitions = new ArrayList<>();
        if(MapUtils.isNotEmpty(responses)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add(HTTP_CODE_COLUMN + DELIMITER + DESCRIPTION_COLUMN + DELIMITER + SCHEMA_COLUMN);
            for(Map.Entry<String, Response> entry : responses.entrySet()){
                Response response = entry.getValue();
                if(response.getSchema() != null){
                    Property property = response.getSchema();
                    Type type = PropertyUtils.getType(property);
                    if (type instanceof ObjectType) {
                        String localTypeName = "Response " + entry.getKey();
                        type.setName(localTypeName);
                        type.setUniqueName(uniqueTypeName(localTypeName));
                        localDefinitions.add(type);

                        type = new RefType(type);
                    }
                    csvContent.add(entry.getKey() + DELIMITER + response.getDescription() + DELIMITER + typeSchema(type));
                }else{
                    csvContent.add(entry.getKey() + DELIMITER + response.getDescription() + DELIMITER +  "No Content");
                }
            }
            addPathSectionTitle(RESPONSES);
            this.markupDocBuilder.tableWithHeaderRow(csvContent);
        }
        return localDefinitions;
    }

    private void localDefinitionsSection(List<Type> definitions) {
        if(CollectionUtils.isNotEmpty(definitions)){
            addPathSectionTitle(DEFINITIONS);

            for (Type definition: definitions) {
                if(pathsGroupedBy.equals(GroupBy.AS_IS)){
                    sectionTitleLevel(4, definition.getName(), definition.getUniqueName(), this.markupDocBuilder);
                }else{
                    sectionTitleLevel(5, definition.getName(), definition.getUniqueName(), this.markupDocBuilder);
                }
                typeProperties(definition, new PropertyDescriptor(definition), this.markupDocBuilder);
            }
        }

    }
}
