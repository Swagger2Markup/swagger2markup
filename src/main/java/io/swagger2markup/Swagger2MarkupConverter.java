package io.swagger2markup;

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import io.swagger2markup.builder.DocumentBuilder;
import io.swagger2markup.builder.asciidoc.AsciiDocBuilder;
import io.swagger2markup.builder.markdown.MarkdownBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
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
public class Swagger2MarkupConverter {
    private static final Logger logger = LoggerFactory.getLogger(Swagger2MarkupConverter.class);
    private static final String VERSION = "Version: ";
    private static final String SUMMARY = "Summary";
    private static final String DESCRIPTION = "Description";
    private static final String PARAMETERS = "Parameters";
    private static final String PRODUCES = "Produces";
    private static final String CONSUMES = "Consumes";
    private static final String RESPONSES = "Responses";
    private static final String DEFINITIONS = "Definitions";
    private static final List<String> IGNORED_DEFINITIONS = Arrays.asList("Void");
    private final Swagger swagger;
    private DocumentBuilder documentBuilder;

    private Swagger2MarkupConverter(String swaggerFileLocation){
        swagger =  new SwaggerParser().read(swaggerFileLocation);
    }

    public static Swagger2MarkupConverter from(String swaggerFileLocation){
        return new Swagger2MarkupConverter(swaggerFileLocation);
    }

    public void toAsciiDoc(String fileLocation) throws IOException {
        documentBuilder = new AsciiDocBuilder();
        buildDocument();
        writeAsciiDocFile(fileLocation);
    }

    public void toMarkdown(String fileLocation) throws IOException {
        documentBuilder = new MarkdownBuilder();
        buildDocument();
        writeAsciiDocFile(fileLocation);
    }

    private void buildDocument(){
        documentHeader(swagger.getInfo());
        paths(swagger.getPaths());
        definitions(swagger.getDefinitions());
    }

    private void writeAsciiDocFile(String asciiDocFileLocation) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(asciiDocFileLocation),
                StandardCharsets.UTF_8)){
            writer.write(documentBuilder.toString());
        } catch (IOException e) {
            logger.warn("Failed to convert Swagger file to AsciiDoc", e);
            throw e;
        }
    }

    private void paths(Map<String, Path> paths) {
        for(Map.Entry<String, Path> entry : paths.entrySet()){
            Path path = entry.getValue();
            path("GET", entry.getKey(), path.getGet());
            path("PUT", entry.getKey(), path.getPut());
            path("DELETE", entry.getKey(), path.getDelete());
            path("POST", entry.getKey(), path.getPost());
            path("PATCH", entry.getKey(), path.getPatch());
        }
    }

    private void documentHeader(Info info) {
        documentBuilder
                .documentTitle(info.getTitle())
                .textLine(info.getDescription())
                .textLine(VERSION + info.getVersion())
                .newLine();
    }

    private void path(String httpMethod, String resourcePath, Operation operation) {
        if(operation != null){
            pathTitle(httpMethod, resourcePath, operation);
            descriptionSection(operation);
            parametersSection(operation);
            responsesSection(operation);
            consumesSection(operation);
            producesSection(operation);
        }
    }

    private void pathTitle(String httpMethod, String resourcePath, Operation operation) {
        String summary = operation.getSummary();
        if(StringUtils.isNotBlank(summary)) {
            documentBuilder.sectionTitleLevel1(operation.getSummary());
            documentBuilder.listing(httpMethod + " " + resourcePath);
        }else{
            documentBuilder.sectionTitleLevel1(httpMethod + " " + resourcePath);
        }
    }

    private void descriptionSection(Operation operation) {
        String description = operation.getDescription();
        if(StringUtils.isNotBlank(description)){
            documentBuilder.sectionTitleLevel2(DESCRIPTION);
            documentBuilder.paragraph(description);
        }
    }

    private void consumesSection(Operation operation) {
        List<String> consumes = operation.getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            documentBuilder.sectionTitleLevel2(CONSUMES);
            documentBuilder.unorderedList(consumes);
        }

    }

    private void producesSection(Operation operation) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            documentBuilder.sectionTitleLevel2(PRODUCES);
            documentBuilder.unorderedList(produces);
        }
    }

    private void parametersSection(Operation operation) {
        List<Parameter> parameters = operation.getParameters();
        if(CollectionUtils.isNotEmpty(parameters)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add("Name,Located in,Description,Required");
            for(Parameter parameter : parameters){
                StringBuilder rowBuilder = new StringBuilder();
                rowBuilder.append(parameter.getName()).append(",").
                           append(parameter.getIn()).append(",").
                        append(parameter.getDescription()).append(",").
                        append(parameter.getRequired());
                csvContent.add(rowBuilder.toString());
            }
            documentBuilder.sectionTitleLevel2(PARAMETERS);
            documentBuilder.tableWithHeaderRow(csvContent);
        }
    }

    private void responsesSection(Operation operation) {
        Map<String, Response> responses = operation.getResponses();
        if(MapUtils.isNotEmpty(responses)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add("Code,Description");
            for(Map.Entry<String, Response> entry : responses.entrySet()){
                Response response = entry.getValue();
                StringBuilder rowBuilder = new StringBuilder();
                rowBuilder.append(entry.getKey()).append(",").
                        append(response.getDescription());
                csvContent.add(rowBuilder.toString());
            }
            documentBuilder.sectionTitleLevel2(RESPONSES);
            documentBuilder.tableWithHeaderRow(csvContent);
        }
    }

    private void definitions(Map<String, Model> definitions) {
        if(MapUtils.isNotEmpty(definitions)){
            documentBuilder.sectionTitleLevel1(DEFINITIONS);
            for(Map.Entry<String, Model> definitionsEntry : definitions.entrySet()){
                String definitionName = definitionsEntry.getKey();
                if(!IGNORED_DEFINITIONS.contains(definitionName)) {
                    documentBuilder.sectionTitleLevel2(definitionName);
                    Model model = definitionsEntry.getValue();
                    Map<String, Property> properties = model.getProperties();
                    List<String> csvContent = new ArrayList<>();
                    csvContent.add("Name,Type,Required");
                    for (Map.Entry<String, Property> propertyEntry : properties.entrySet()) {
                        Property property = propertyEntry.getValue();
                        StringBuilder rowBuilder = new StringBuilder();
                        rowBuilder.append(propertyEntry.getKey()).append(",").
                                append(property.getType()).append(",").append(property.getRequired());
                        csvContent.add(rowBuilder.toString());
                    }
                    documentBuilder.tableWithHeaderRow(csvContent);
                }
            }
        }
    }

}
