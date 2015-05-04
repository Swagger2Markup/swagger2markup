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

import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Response;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.properties.Property;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.utils.ParameterUtils;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupDocument {

    private static final String PATHS = "Paths";
    private static final String PARAMETERS = "Parameters";
    private static final String RESPONSES = "Responses";
    private static final String EXAMPLE_REQUEST = "Example request";
    private static final String EXAMPLE_RESPONSE = "Example response";
    private static final String TYPE_COLUMN = "Type";
    private static final String HTTP_CODE_COLUMN = "HTTP Code";
    private static final String REQUEST_EXAMPLE_FILE_NAME = "http-request";
    private static final String RESPONSE_EXAMPLE_FILE_NAME = "http-response";
    private static final String DESCRIPTION_FILE_NAME = "description";
    private static final String PARAMETER = "Parameter";

    private boolean examplesEnabled;
    private String examplesFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;

    public PathsDocument(Swagger swagger, MarkupLanguage markupLanguage, String examplesFolderPath, String descriptionsFolderPath){
        super(swagger, markupLanguage);
        if(StringUtils.isNotBlank(examplesFolderPath)){
            this.examplesEnabled = true;
            this.examplesFolderPath = examplesFolderPath;
        }
        if(StringUtils.isNotBlank(descriptionsFolderPath)){
            this.handWrittenDescriptionsEnabled = true;
            this.descriptionsFolderPath = descriptionsFolderPath + "/" + PATHS.toLowerCase();
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

    @Override
    public MarkupDocument build() throws IOException {
        paths();
        return this;
    }

    /**
     * Builds all paths of the Swagger model
     */
    private void paths() throws IOException {
        Map<String, Path> paths = swagger.getPaths();
        if(MapUtils.isNotEmpty(paths)) {
            this.markupDocBuilder.sectionTitleLevel1(PATHS);
            for (Map.Entry<String, Path> entry : paths.entrySet()) {
                Path path = entry.getValue();
                if(path != null) {
                    path("GET", entry.getKey(), path.getGet());
                    path("PUT", entry.getKey(), path.getPut());
                    path("DELETE", entry.getKey(), path.getDelete());
                    path("POST", entry.getKey(), path.getPost());
                    path("PATCH", entry.getKey(), path.getPatch());
                }
            }
        }
    }

    /**
     * Builds a path
     *
     * @param httpMethod the HTTP method of the path
     * @param resourcePath the URL of the path
     * @param operation the Swagger Operation
     */
    private void path(String httpMethod, String resourcePath, Operation operation) throws IOException {
        if(operation != null){
            pathTitle(httpMethod, resourcePath, operation);
            descriptionSection(operation);
            parametersSection(operation);
            responsesSection(operation);
            consumesSection(operation);
            producesSection(operation);
            tagsSection(operation);
            examplesSection(operation);
        }
    }

    private void pathTitle(String httpMethod, String resourcePath, Operation operation) {
        String summary = operation.getSummary();
        String title;
        if(StringUtils.isNotBlank(summary)) {
            title = summary;
            this.markupDocBuilder.sectionTitleLevel2(title);
            this.markupDocBuilder.listing(httpMethod + " " + resourcePath);
        }else{
            title = httpMethod + " " + resourcePath;
            this.markupDocBuilder.sectionTitleLevel2(title);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Path processed: {}", title);
        }
    }

    private void descriptionSection(Operation operation) throws IOException {
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            if(StringUtils.isNotBlank(summary)) {
                String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                String description = handWrittenPathDescription(operationFolder, DESCRIPTION_FILE_NAME);
                if(StringUtils.isNotBlank(description)){
                    this.markupDocBuilder.sectionTitleLevel3(DESCRIPTION);
                    this.markupDocBuilder.paragraph(description);
                }else{
                    if (logger.isInfoEnabled()) {
                        logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                    }
                    pathDescription(operation);
                }
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read, because summary of operation is empty. Trying to use description from Swagger source.");
                }
                pathDescription(operation);
            }
        }else {
            pathDescription(operation);
        }
    }

    private void pathDescription(Operation operation) {
        String description = operation.getDescription();
        if (StringUtils.isNotBlank(description)) {
            this.markupDocBuilder.sectionTitleLevel3(DESCRIPTION);
            this.markupDocBuilder.paragraph(description);
        }
    }

    private void parametersSection(Operation operation) throws IOException {
        List<Parameter> parameters = operation.getParameters();
        if(CollectionUtils.isNotEmpty(parameters)){
            List<String> headerAndContent = new ArrayList<>();
            // Table header row
            List<String> header = Arrays.asList(TYPE_COLUMN, NAME_COLUMN, DESCRIPTION_COLUMN, REQUIRED_COLUMN, SCHEMA_COLUMN);
            headerAndContent.add(StringUtils.join(header, DELIMITER));
            for(Parameter parameter : parameters){
                String type = ParameterUtils.getType(parameter, markupLanguage);
                String parameterType = WordUtils.capitalize(parameter.getIn() + PARAMETER);
                // Table content row
                List<String> content = Arrays.asList(parameterType, parameter.getName(),  parameterDescription(operation, parameter), Boolean.toString(parameter.getRequired()), type);
                headerAndContent.add(StringUtils.join(content, DELIMITER));
            }
            this.markupDocBuilder.sectionTitleLevel3(PARAMETERS);
            this.markupDocBuilder.tableWithHeaderRow(headerAndContent);
        }
    }

    private String parameterDescription(Operation operation, Parameter parameter) throws IOException {
        String description;
        if(handWrittenDescriptionsEnabled){
            String summary = operation.getSummary();
            String operationFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
            String parameterName = parameter.getName();
            if(StringUtils.isNotBlank(operationFolder) && StringUtils.isNotBlank(parameterName)) {
                description = handWrittenPathDescription(operationFolder + "/" + parameterName, DESCRIPTION_FILE_NAME);
                if(StringUtils.isBlank(description)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Hand-written description file cannot be read. Trying to use description from Swagger source.");
                    }
                    description = StringUtils.defaultString(parameter.getDescription());
                }
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description file cannot be read, because summary of operation or name of parameter is empty. Trying to use description from Swagger source.");
                }
                description = StringUtils.defaultString(parameter.getDescription());
            }
        }else {
            description = StringUtils.defaultString(parameter.getDescription());
        }
        return description;
    }

    private void consumesSection(Operation operation) {
        List<String> consumes = operation.getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            this.markupDocBuilder.sectionTitleLevel3(CONSUMES);
            this.markupDocBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(Operation operation) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            this.markupDocBuilder.sectionTitleLevel3(PRODUCES);
            this.markupDocBuilder.unorderedList(produces);
        }
    }

    private void tagsSection(Operation operation) {
        List<String> tags = operation.getTags();
        if(CollectionUtils.isNotEmpty(tags)){
            this.markupDocBuilder.sectionTitleLevel3(TAGS);
            this.markupDocBuilder.unorderedList(tags);
        }
    }

    /**
     * Builds the example section of a Swagger Operation
     *
     * @param operation the Swagger Operation
     * @throws IOException if the example file is not readable
     */
    private void examplesSection(Operation operation) throws IOException {
        if(examplesEnabled){
            String summary = operation.getSummary();
            if(StringUtils.isNotBlank(summary)) {
                String exampleFolder = summary.replace(".", "").replace(" ", "_").toLowerCase();
                String requestExample = example(exampleFolder, REQUEST_EXAMPLE_FILE_NAME);
                if(StringUtils.isNotBlank(requestExample)){
                    this.markupDocBuilder.sectionTitleLevel3(EXAMPLE_REQUEST);
                    this.markupDocBuilder.paragraph(requestExample);
                }
                String responseExample = example(exampleFolder, RESPONSE_EXAMPLE_FILE_NAME);
                if(StringUtils.isNotBlank(responseExample)){
                    this.markupDocBuilder.sectionTitleLevel3(EXAMPLE_RESPONSE);
                    this.markupDocBuilder.paragraph(responseExample);
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
     * @throws IOException
     */
    private String example(String exampleFolder, String exampleFileName) throws IOException {
        for (String fileNameExtension : markupLanguage.getFileNameExtensions()) {
            java.nio.file.Path path = Paths.get(examplesFolderPath, exampleFolder, exampleFileName + fileNameExtension);
            if (Files.isReadable(path)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Example file processed: {}", path);
                }
                return FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Example file is not readable: {}", path);
                }
            }
        }
        if (logger.isWarnEnabled()) {
            logger.info("No example file found with correct file name extension in folder: {}", Paths.get(examplesFolderPath, exampleFolder));
        }
        return null;
    }

    /**
     * Reads a hand-written description
     *
     * @param descriptionFolder the name of the folder where the description file resides
     * @param descriptionFileName the name of the description file
     * @return the content of the file
     * @throws IOException
     */
    private String handWrittenPathDescription(String descriptionFolder, String descriptionFileName) throws IOException {
        for (String fileNameExtension : markupLanguage.getFileNameExtensions()) {
            java.nio.file.Path path = Paths.get(descriptionsFolderPath, descriptionFolder, descriptionFileName + fileNameExtension);
            if (Files.isReadable(path)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Description file processed: {}", path);
                }
                return FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Description file is not readable: {}", path);
                }
            }
        }
        if (logger.isWarnEnabled()) {
            logger.info("No description file found with correct file name extension in folder: {}", Paths.get(descriptionsFolderPath, descriptionFolder));
        }
        return null;
    }

    private void responsesSection(Operation operation) {
        Map<String, Response> responses = operation.getResponses();
        if(MapUtils.isNotEmpty(responses)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add(HTTP_CODE_COLUMN + DELIMITER + DESCRIPTION_COLUMN + DELIMITER + SCHEMA_COLUMN);
            for(Map.Entry<String, Response> entry : responses.entrySet()){
                Response response = entry.getValue();
                if(response.getSchema() != null){
                    Property property = response.getSchema();
                    String type = PropertyUtils.getType(property, markupLanguage);
                    csvContent.add(entry.getKey() + DELIMITER + response.getDescription() + DELIMITER +  type);
                }else{
                    csvContent.add(entry.getKey() + DELIMITER + response.getDescription() + DELIMITER +  "No Content");
                }
            }
            this.markupDocBuilder.sectionTitleLevel3(RESPONSES);
            this.markupDocBuilder.tableWithHeaderRow(csvContent);
        }
    }

}
