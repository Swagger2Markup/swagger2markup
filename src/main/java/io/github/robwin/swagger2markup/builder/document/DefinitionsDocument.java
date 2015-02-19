package io.github.robwin.swagger2markup.builder.document;

import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.properties.Property;
import io.github.robwin.swagger2markup.builder.markup.MarkupLanguage;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Project:   swagger2markup
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
public class DefinitionsDocument extends MarkupDocument {

    private static final String DEFINITIONS = "Definitions";
    private static final List<String> IGNORED_DEFINITIONS = Arrays.asList("Void");
    private static final String JSON_SCHEMA = "JSON Schema";
    private static final String XML_SCHEMA = "XML Schema";
    private static final String NAME_COLUMN = "Name";
    private static final String TYPE_COLUMN = "Type";
    public static final String JSON_SCHEMA_EXTENSION = ".json";
    public static final String XML_SCHEMA_EXTENSION = ".xsd";
    public static final String JSON = "json";
    public static final String XML = "xml";
    private boolean schemasEnabled;
    private String schemasFolderPath;    

    public DefinitionsDocument(Swagger swagger, MarkupLanguage markupLanguage, String schemasFolderPath){
        super(swagger, markupLanguage);
        if(StringUtils.isNotBlank(schemasFolderPath)){
            this.schemasEnabled = true;
            this.schemasFolderPath = schemasFolderPath;
        }
        if(schemasEnabled){
            if (logger.isDebugEnabled()) {
                logger.debug("Include schemas is enabled.");
            }
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("Include schemas is disabled.");
            }
        }
    }

    @Override
    public MarkupDocument build() throws IOException {
        definitions(swagger.getDefinitions());
        return this;
    }

    /**
     * Builds the Swagger definitions.
     *
     * @param definitions the Swagger definitions
     */
    private void definitions(Map<String, Model> definitions) throws IOException {
        if(MapUtils.isNotEmpty(definitions)){
            this.documentBuilder.sectionTitleLevel1(DEFINITIONS);
            for(Map.Entry<String, Model> definitionsEntry : definitions.entrySet()){
                String definitionName = definitionsEntry.getKey();
                if(StringUtils.isNotBlank(definitionName)) {
                    if (checkThatDefinitionIsNotInIgnoreList(definitionName)) {
                        definition(definitionName, definitionsEntry.getValue());
                        definitionSchema(definitionName);
                        if (logger.isInfoEnabled()) {
                            logger.info("Definition processed: {}", definitionName);
                        }
                    }else{
                        if (logger.isDebugEnabled()) {
                            logger.debug("Definition was ignored: {}", definitionName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks that the definition is not in the list of ignored definitions.
     *
     * @param definitionName the name of the definition
     * @return true if the definition can be processed
     */
    private boolean checkThatDefinitionIsNotInIgnoreList(String definitionName) {
        return !IGNORED_DEFINITIONS.contains(definitionName);
    }

    /**
     * Builds a concrete definition
     *
     * @param definitionName the name of the definition
     * @param model the Swagger Model of the definition
     */
    private void definition(String definitionName, Model model) {
        this.documentBuilder.sectionTitleLevel2(definitionName);
        Map<String, Property> properties = model.getProperties();
        List<String> csvContent = new ArrayList<>();
        csvContent.add(NAME_COLUMN + DELIMITER + TYPE_COLUMN + DELIMITER + REQUIRED_COLUMN);
        for (Map.Entry<String, Property> propertyEntry : properties.entrySet()) {
            Property property = propertyEntry.getValue();
            csvContent.add(propertyEntry.getKey() + DELIMITER + property.getType() + DELIMITER + property.getRequired());
        }
        this.documentBuilder.tableWithHeaderRow(csvContent);
    }

    private void definitionSchema(String definitionName) throws IOException {
        if(schemasEnabled) {
            if (StringUtils.isNotBlank(definitionName)) {
                schema(JSON_SCHEMA, schemasFolderPath, definitionName + JSON_SCHEMA_EXTENSION, JSON);
                schema(XML_SCHEMA, schemasFolderPath, definitionName + XML_SCHEMA_EXTENSION, XML);
            }
        }
    }

    private void schema(String title, String schemasFolderPath, String schemaName, String language) throws IOException {
        java.nio.file.Path path = Paths.get(schemasFolderPath, schemaName);
        if (Files.isReadable(path)) {
            this.documentBuilder.sectionTitleLevel3(title);
            this.documentBuilder.source(FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim(), language);
            if (logger.isInfoEnabled()) {
                logger.info("Schema file processed: {}", path);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Schema file is not readable: {}", path);
            }
        }

    }
}
