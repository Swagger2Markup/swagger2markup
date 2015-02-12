package io.swagger2asciidoc;

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
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
public class Swagger2AsciiDocConverter {
    private static final Logger logger = LoggerFactory.getLogger(Swagger2AsciiDocConverter.class);
    private static final String VERSION = "Version: ";
    private static final String SUMMARY = "Summary";
    private static final String DESCRIPTION = "Description";
    private static final String PARAMETERS = "Parameters";
    private static final String PRODUCES = "Produces";
    private static final String CONSUMES = "Consumes";
    private static final String RESPONSES = "Responses";
    private static final String DEFINITIONS = "Definitions";
    private static final List<String> IGNORED_DEFINITIONS = Arrays.asList("Void");
    private final AsciiDocBuilder asciiDocBuilder;
    private final Swagger swagger;
    private final String asciiDocFileLocation;

    private Swagger2AsciiDocConverter(String swaggerFileLocation, String asciiDocFileLocation){
        swagger =  new SwaggerParser().read(swaggerFileLocation);
        this.asciiDocFileLocation = asciiDocFileLocation;
        asciiDocBuilder = new AsciiDocBuilder();
    }

    public static Swagger2AsciiDocConverter newInstance(String swaggerFileLocation, String asciiDocFileLocation){
        return new Swagger2AsciiDocConverter(swaggerFileLocation, asciiDocFileLocation);
    }

    public void convertSwagger2AsciiDoc(){
        documentHeader(swagger.getInfo());
        paths(swagger.getPaths());
        definitions(swagger.getDefinitions());
        writeAsciiDocFile(asciiDocFileLocation);
    }

    private void writeAsciiDocFile(String asciiDocFileLocation) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(asciiDocFileLocation),
                StandardCharsets.UTF_8)){
            writer.write(asciiDocBuilder.toString());
        } catch (IOException e) {
            logger.warn("Failed to convert Swagger file to AsciiDoc", e);
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
        asciiDocBuilder
                .documentTitle(info.getTitle())
                .textLine(info.getDescription())
                .textLine(VERSION + info.getVersion())
                .lineBreak();

    }

    private void path(String httpMethod, String resourcePath, Operation operation) {
        if(operation != null){
            pathTitleAndDescription(httpMethod, resourcePath, operation);
            parameters(operation);
            responses(operation);
            consumes(operation);
            produces(operation);
        }
    }

    private void pathTitleAndDescription(String httpMethod, String resourcePath, Operation operation) {
        String summary = operation.getSummary();
        if(StringUtils.isNotBlank(summary)) {
            asciiDocBuilder.sectionTitleLevel1(operation.getSummary());
            description(operation);
            asciiDocBuilder.listing(httpMethod + " " + resourcePath);
        }else{
            asciiDocBuilder.sectionTitleLevel1(httpMethod + " " + resourcePath);
            description(operation);
        }
    }

    private void description(Operation operation) {
        String description = operation.getDescription();
        if(StringUtils.isNotBlank(description)){
            asciiDocBuilder.sectionTitleLevel2(DESCRIPTION);
            asciiDocBuilder.paragraph(description);
        }
    }

    private void consumes(Operation operation) {
        List<String> consumes = operation.getConsumes();
        if(CollectionUtils.isNotEmpty(consumes)){
            asciiDocBuilder.sectionTitleLevel2(CONSUMES);
            asciiDocBuilder.unorderedList(consumes);
        }

    }

    private void produces(Operation operation) {
        List<String> produces = operation.getProduces();
        if(CollectionUtils.isNotEmpty(produces)){
            asciiDocBuilder.sectionTitleLevel2(PRODUCES);
            asciiDocBuilder.unorderedList(produces);
        }
    }

    private void parameters(Operation operation) {
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
            asciiDocBuilder.sectionTitleLevel2(PARAMETERS);
            asciiDocBuilder.tableWithHeaderRow(csvContent);
        }
    }

    private void responses(Operation operation) {
        Map<String, Response> responses = operation.getResponses();
        if(MapUtils.isNotEmpty(responses)){
            List<String> csvContent = new ArrayList<>();
            csvContent.add("Code,Description,Schema");
            for(Map.Entry<String, Response> entry : responses.entrySet()){
                Response response = entry.getValue();
                StringBuilder rowBuilder = new StringBuilder();
                rowBuilder.append(entry.getKey()).append(",").
                        append(response.getDescription()).append(",").
                        append(response.getSchema());
                csvContent.add(rowBuilder.toString());
            }
            asciiDocBuilder.sectionTitleLevel2(RESPONSES);
            asciiDocBuilder.tableWithHeaderRow(csvContent);
        }
    }

    private void definitions(Map<String, Model> definitions) {
        if(MapUtils.isNotEmpty(definitions)){
            asciiDocBuilder.sectionTitleLevel1(DEFINITIONS);
            for(Map.Entry<String, Model> definitionsEntry : definitions.entrySet()){
                String definitionName = definitionsEntry.getKey();
                if(!IGNORED_DEFINITIONS.contains(definitionName)) {
                    asciiDocBuilder.sectionTitleLevel2(definitionName);
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
                    asciiDocBuilder.tableWithHeaderRow(csvContent);
                }
            }
        }
    }

}
