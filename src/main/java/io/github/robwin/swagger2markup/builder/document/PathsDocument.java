package io.github.robwin.swagger2markup.builder.document;

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.parameters.Parameter;
import io.github.robwin.swagger2markup.builder.markup.MarkupLanguage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupDocument {

    private static final String VERSION = "Version: ";
    private static final String DESCRIPTION_COLUMN = "Description";
    private static final String DESCRIPTION = DESCRIPTION_COLUMN;
    private static final String PARAMETERS = "Parameters";
    private static final String PRODUCES = "Produces";
    private static final String CONSUMES = "Consumes";
    private static final String RESPONSES = "Responses";
    private static final String EXAMPLE_REQUEST = "Example request";
    private static final String EXAMPLE_RESPONSE = "Example response";
    private static final String NAME_COLUMN = "Name";
    private static final String LOCATED_IN_COLUMN = "Located in";
    private static final String CODE_COLUMN = "Code";
    public static final String REQUEST_EXAMPLE_FILE_NAME = "request";
    public static final String RESPONSE_EXAMPLE_FILE_NAME = "response";

    private boolean examplesEnabled;
    private String examplesFolderPath;

    public PathsDocument(Swagger swagger, MarkupLanguage markupLanguage, String examplesFolderPath){
        super(swagger, markupLanguage);
        if(StringUtils.isNotBlank(examplesFolderPath)){
            this.examplesEnabled = true;
            this.examplesFolderPath = examplesFolderPath;
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
    }

    @Override
    public MarkupDocument build() throws IOException {
        documentHeader(swagger.getInfo());
        paths(swagger.getPaths());
        return this;
    }

    /**
     * Builds the document header
     *
     * @param info the Swagger Info
     */
    private void documentHeader(Info info) {
        this.markupDocBuilder
                .documentTitle(info.getTitle())
                .textLine(info.getDescription())
                .textLine(VERSION + info.getVersion())
                .newLine();
    }

    /**
     * Builds all paths of the Swagger file
     *
     * @param paths a Map of Swagger Paths
     */
    private void paths(Map<String, Path> paths) throws IOException {
        if(MapUtils.isNotEmpty(paths)) {
            //this.documentBuilder.sectionTitleLevel1(FEATURES);
            for (Map.Entry<String, Path> entry : paths.entrySet()) {
                Path path = entry.getValue();
                path("GET", entry.getKey(), path.getGet());
                path("PUT", entry.getKey(), path.getPut());
                path("DELETE", entry.getKey(), path.getDelete());
                path("POST", entry.getKey(), path.getPost());
                path("PATCH", entry.getKey(), path.getPatch());
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
            examplesSection(operation);
        }
    }


    private void pathTitle(String httpMethod, String resourcePath, Operation operation) {
        String summary = operation.getSummary();
        String title;
        if(StringUtils.isNotBlank(summary)) {
            title = summary;
            this.markupDocBuilder.sectionTitleLevel1(title);
            this.markupDocBuilder.listing(httpMethod + " " + resourcePath);
        }else{
            title = httpMethod + " " + resourcePath;
            this.markupDocBuilder.sectionTitleLevel1(title);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Path processed: {}", title);
        }
    }

    private void descriptionSection(Operation operation) {
        String description = operation.getDescription();
        if(StringUtils.isNotBlank(description)){
            this.markupDocBuilder.sectionTitleLevel2(DESCRIPTION);
            this.markupDocBuilder.paragraph(description);
        }
    }

    private void parametersSection(Operation operation) {
        List<Parameter> parameters = operation.getParameters();
        if(CollectionUtils.isNotEmpty(parameters)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add(NAME_COLUMN + DELIMITER + LOCATED_IN_COLUMN + DELIMITER + DESCRIPTION_COLUMN + DELIMITER + REQUIRED_COLUMN);
            for(Parameter parameter : parameters){
                csvContent.add(parameter.getName() + DELIMITER + parameter.getIn() + DELIMITER + parameter.getDescription() + DELIMITER + parameter.getRequired());
            }
            this.markupDocBuilder.sectionTitleLevel2(PARAMETERS);
            this.markupDocBuilder.tableWithHeaderRow(csvContent);
        }
    }

    private void consumesSection(Operation operation) {
        List<String> consumes = operation.getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            this.markupDocBuilder.sectionTitleLevel2(CONSUMES);
            this.markupDocBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(Operation operation) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            this.markupDocBuilder.sectionTitleLevel2(PRODUCES);
            this.markupDocBuilder.unorderedList(produces);
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
                example(EXAMPLE_REQUEST, exampleFolder, REQUEST_EXAMPLE_FILE_NAME);
                example(EXAMPLE_RESPONSE, exampleFolder, RESPONSE_EXAMPLE_FILE_NAME);
            }
        }
    }

    /**
     * Builds a concrete example
     *
     * @param title the title of the example
     * @param exampleFolder the name of the folder where the example file resides
     * @param exampleFileName the name of the example file
     * @throws IOException
     */
    private void example(String title, String exampleFolder, String exampleFileName) throws IOException {
        for (String fileNameExtension : markupLanguage.getFileNameExtensions()) {
            java.nio.file.Path path = Paths.get(examplesFolderPath, exampleFolder, exampleFileName + fileNameExtension);
            if (Files.isReadable(path)) {
                this.markupDocBuilder.sectionTitleLevel2(title);
                this.markupDocBuilder.paragraph(FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim());
                if (logger.isInfoEnabled()) {
                    logger.info("Example file processed: {}", path);
                }
                break;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Example file is not readable: {}", path);
                }
            }
        }
    }

    private void responsesSection(Operation operation) {
        Map<String, Response> responses = operation.getResponses();
        if(MapUtils.isNotEmpty(responses)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add(CODE_COLUMN + DELIMITER + DESCRIPTION_COLUMN);
            for(Map.Entry<String, Response> entry : responses.entrySet()){
                Response response = entry.getValue();
                csvContent.add(entry.getKey() + DELIMITER + response.getDescription());
            }
            this.markupDocBuilder.sectionTitleLevel2(RESPONSES);
            this.markupDocBuilder.tableWithHeaderRow(csvContent);
        }
    }

}
