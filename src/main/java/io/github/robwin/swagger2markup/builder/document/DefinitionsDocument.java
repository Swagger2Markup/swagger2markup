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

import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

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
public class DefinitionsDocument extends MarkupDocument {

    private static final String DEFINITIONS = "Definitions";
    private static final List<String> IGNORED_DEFINITIONS = Arrays.asList("Void");
    private static final String JSON_SCHEMA = "JSON Schema";
    private static final String XML_SCHEMA = "XML Schema";
    public static final String JSON_SCHEMA_EXTENSION = ".json";
    public static final String XML_SCHEMA_EXTENSION = ".xsd";
    public static final String JSON = "json";
    public static final String XML = "xml";
    private static final String DESCRIPTION_FILE_NAME = "description";
    private boolean schemasEnabled;
    private String schemasFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;

    public DefinitionsDocument(Swagger swagger, MarkupLanguage markupLanguage, String schemasFolderPath, String descriptionsFolderPath){
        super(swagger, markupLanguage);
        if(StringUtils.isNotBlank(schemasFolderPath)){
            this.schemasEnabled = true;
            this.schemasFolderPath = schemasFolderPath;
        }
        if(StringUtils.isNotBlank(descriptionsFolderPath)){
            this.handWrittenDescriptionsEnabled = true;
            this.descriptionsFolderPath = descriptionsFolderPath + "/" + DEFINITIONS.toLowerCase();
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
            this.markupDocBuilder.sectionTitleLevel1(DEFINITIONS);
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
    private void definition(String definitionName, Model model) throws IOException {
        this.markupDocBuilder.sectionTitleLevel2(definitionName);
        descriptionSection(definitionName, model);
        propertiesSection(definitionName, model);

    }

    private void propertiesSection(String definitionName, Model model) throws IOException {
        Map<String, Property> properties = model.getProperties();
        List<String> headerAndContent = new ArrayList<>();
        List<String> header = Arrays.asList(NAME_COLUMN, DESCRIPTION_COLUMN, REQUIRED_COLUMN, SCHEMA_COLUMN, DEFAULT_COLUMN);
        headerAndContent.add(StringUtils.join(header, DELIMITER));
        if(MapUtils.isNotEmpty(properties)){
            for (Map.Entry<String, Property> propertyEntry : properties.entrySet()) {
                Property property = propertyEntry.getValue();
                String propertyName = propertyEntry.getKey();
                List<String> content = Arrays.asList(
                        propertyName,
                        propertyDescription(definitionName, propertyName, property),
                        Boolean.toString(property.getRequired()),
                        PropertyUtils.getType(property, markupLanguage),
                        PropertyUtils.getDefaultValue(property));
                headerAndContent.add(StringUtils.join(content, DELIMITER));
            }
            this.markupDocBuilder.tableWithHeaderRow(headerAndContent);
        }
    }

    private void descriptionSection(String definitionName, Model model) throws IOException {
        if(handWrittenDescriptionsEnabled){
            String description = handWrittenPathDescription(definitionName.toLowerCase(), DESCRIPTION_FILE_NAME);
            if(StringUtils.isNotBlank(description)){
                this.markupDocBuilder.paragraph(description);
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                }
                modelDescription(model);
            }
        }
        else{
            modelDescription(model);
        }
    }

    private void modelDescription(Model model) {
        String description = model.getDescription();
        if (StringUtils.isNotBlank(description)) {
            this.markupDocBuilder.paragraph(description);
        }
    }

    private String propertyDescription(String definitionName, String propertyName, Property property) throws IOException {
        String description;
        if(handWrittenDescriptionsEnabled){
            description = handWrittenPathDescription(definitionName.toLowerCase() + "/" + propertyName.toLowerCase(), DESCRIPTION_FILE_NAME);
            if(StringUtils.isBlank(description)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description file cannot be read. Trying to use description from Swagger source.");
                }
                description = StringUtils.defaultString(property.getDescription());
            }
        }
        else{
            description = StringUtils.defaultString(property.getDescription());
        }
        return description;
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
            this.markupDocBuilder.sectionTitleLevel3(title);
            this.markupDocBuilder.source(FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim(), language);
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
