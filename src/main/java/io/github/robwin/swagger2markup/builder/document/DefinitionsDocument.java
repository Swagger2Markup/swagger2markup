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

import com.google.common.collect.ImmutableMap;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.Type;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.Property;
import io.swagger.models.refs.RefFormat;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class DefinitionsDocument extends MarkupDocument {

    private static final String DEFINITIONS_ANCHOR = "definitions";
    private final String DEFINITIONS;
    private static final List<String> IGNORED_DEFINITIONS = Collections.singletonList("Void");
    private final String JSON_SCHEMA;
    private final String XML_SCHEMA;
    private static final String JSON_SCHEMA_EXTENSION = ".json";
    private static final String XML_SCHEMA_EXTENSION = ".xsd";
    private static final String JSON = "json";
    private static final String XML = "xml";
    private static final String DESCRIPTION_FOLDER_NAME = "definitions";
    private static final String DESCRIPTION_FILE_NAME = "description";
    private boolean schemasEnabled;
    private String schemasFolderPath;
    private boolean handWrittenDescriptionsEnabled;
    private String descriptionsFolderPath;
    private final int inlineSchemaDepthLevel;
    private final Comparator<String> definitionOrdering;

    public DefinitionsDocument(Swagger2MarkupConfig swagger2MarkupConfig, String outputDirectory){
        super(swagger2MarkupConfig, outputDirectory);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        DEFINITIONS = labels.getString("definitions");
        JSON_SCHEMA = labels.getString("json_schema");
        XML_SCHEMA = labels.getString("xml_schema");

        this.inlineSchemaDepthLevel = swagger2MarkupConfig.getInlineSchemaDepthLevel();
        if(isNotBlank(swagger2MarkupConfig.getSchemasFolderPath())){
            this.schemasEnabled = true;
            this.schemasFolderPath = swagger2MarkupConfig.getSchemasFolderPath();
        }
        if(isNotBlank(swagger2MarkupConfig.getDescriptionsFolderPath())){
            this.handWrittenDescriptionsEnabled = true;
            this.descriptionsFolderPath = swagger2MarkupConfig.getDescriptionsFolderPath() + "/" + DESCRIPTION_FOLDER_NAME;
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
        if(this.separatedDefinitionsEnabled){
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated definition files is enabled.");
            }
            Validate.notEmpty(outputDirectory, "Output directory is required for separated definition files!");
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated definition files is disabled.");
            }
        }
        this.definitionOrdering = swagger2MarkupConfig.getDefinitionOrdering();
    }

    @Override
    public MarkupDocument build(){
        definitions(swagger.getDefinitions());
        return this;
    }

    private void addDefinitionsTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, DEFINITIONS_ANCHOR);
    }

    /**
     * Builds the Swagger definitions.
     *
     * @param definitions the Swagger definitions
     */
    private void definitions(Map<String, Model> definitions){
        if(MapUtils.isNotEmpty(definitions)){
            addDefinitionsTitle(DEFINITIONS);
            Set<String> definitionNames;
            if (definitionOrdering == null)
              definitionNames = new LinkedHashSet<>();
            else
              definitionNames = new TreeSet<>(definitionOrdering);
            definitionNames.addAll(definitions.keySet());
            for(String definitionName : definitionNames){
                Model model = definitions.get(definitionName);
                if(isNotBlank(definitionName)) {
                    if (checkThatDefinitionIsNotInIgnoreList(definitionName)) {
                        processDefinition(definitions, definitionName, model);
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
     * Create the definition filename depending on the generation mode
     * @param definitionName definition name
     * @return definition filename
     */
    private String resolveDefinitionDocument(String definitionName) {
        if (separatedDefinitionsEnabled)
            return new File(separatedDefinitionsFolder, markupDocBuilder.addfileExtension(normalizeFileName(definitionName))).getPath();
        else
            return markupDocBuilder.addfileExtension(definitionsDocument);
    }

    /**
     * Generate definition files depending on the generation mode
     * @param definitions all available definitions to be able to verify references
     * @param definitionName definition name to process
     * @param model definition model to process
     */
    private void processDefinition(Map<String, Model> definitions, String definitionName, Model model) {

        if (separatedDefinitionsEnabled) {
            MarkupDocBuilder defDocBuilder = this.markupDocBuilder.copy();
            definition(definitions, definitionName, model, defDocBuilder);
            File definitionFile = new File(outputDirectory, resolveDefinitionDocument(definitionName));
            try {
                String definitionDirectory = FilenameUtils.getFullPath(definitionFile.getPath());
                String definitionFileName = FilenameUtils.getName(definitionFile.getPath());

                defDocBuilder.writeToFileWithoutExtension(definitionDirectory, definitionFileName, StandardCharsets.UTF_8);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Failed to write definition file: %s", definitionFile), e);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Separate definition file produced: {}", definitionFile);
            }

            definitionRef(definitionName, this.markupDocBuilder);

        } else {
            definition(definitions, definitionName, model, this.markupDocBuilder);
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
     * @param docBuilder the docbuilder do use for output
     */
    private void definition(Map<String, Model> definitions, String definitionName, Model model, MarkupDocBuilder docBuilder){
        addDefinitionTitle(definitionName, null, docBuilder);
        descriptionSection(definitionName, model, docBuilder);
        inlineDefinitions(propertiesSection(definitions, definitionName, model, docBuilder), definitionName, inlineSchemaDepthLevel, docBuilder);
        definitionSchema(definitionName, docBuilder);
    }

    /**
     * Builds a cross-reference to a separated definition file.
     * @param definitionName definition name to target
     * @param docBuilder the docbuilder do use for output
     */
    private void definitionRef(String definitionName, MarkupDocBuilder docBuilder){
        addDefinitionTitle(docBuilder.copy().crossReference(new DefinitionDocumentResolverDefault().apply(definitionName), definitionName, definitionName).toString(), "ref-" + definitionName, docBuilder);
    }

    /**
     * Builds definition title
     * @param title definition title
     * @param anchor optional anchor (null => auto-generate from title)
     * @param docBuilder the docbuilder do use for output
     */
    private void addDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.sectionTitleWithAnchorLevel2(title, anchor);
    }

    /**
     * Override Property description functor for definitions.
     * This implementation handles optional handwritten descriptions.
     */
    private class DefinitionPropertyDescriptor extends PropertyDescriptor {

        public DefinitionPropertyDescriptor(Type type) {
            super(type);
        }

        @Override
        public String getDescription(Property property, String propertyName) {
            String description;
            if(handWrittenDescriptionsEnabled){
                description = handWrittenPathDescription(type.getName().toLowerCase() + "/" + propertyName.toLowerCase(), DESCRIPTION_FILE_NAME);
                if(isBlank(description)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Hand-written description file cannot be read. Trying to use description from Swagger source.");
                    }
                    description = defaultString(property.getDescription());
                }
            }
            else{
                description = defaultString(property.getDescription());
            }
            return description;
        }
    }

    /**
     * Builds the properties of a definition and inline schemas.
     * @param definitions all available definitions
     * @param definitionName name of the definition to display
     * @param model model of the definition to display
     * @param docBuilder the docbuilder do use for output
     * @return a list of inlined types.
     */
    private List<ObjectType> propertiesSection(Map<String, Model> definitions, String definitionName, Model model, MarkupDocBuilder docBuilder){
        Map<String, Property> properties = getAllProperties(definitions, model);
        ObjectType type = new ObjectType(definitionName, properties);

        return typeProperties(type, definitionName, 1, new PropertyDescriptor(type), new DefinitionDocumentResolverFromDefinition(), docBuilder);
    }

    private Map<String, Property> getAllProperties(Map<String, Model> definitions, Model model) {
        if(model instanceof RefModel) {
            RefModel refModel = (RefModel)model;
            String ref;
            if(refModel.getRefFormat().equals(RefFormat.INTERNAL)){
                ref = refModel.getSimpleRef();
            }else{
                ref = model.getReference();
            }
            return definitions.containsKey(ref)
                    ? getAllProperties(definitions, definitions.get(ref))
                    : null;
        }
        if(model instanceof ComposedModel) {
            ComposedModel composedModel = (ComposedModel)model;
            Map<String, Property> allProperties = new HashMap<>();
            if(composedModel.getAllOf() != null) {
                for(Model innerModel : composedModel.getAllOf()) {
                    Map<String, Property> innerProperties = getAllProperties(definitions, innerModel);
                    if(innerProperties != null) {
                        allProperties.putAll(innerProperties);
                    }
                }
            }
            return ImmutableMap.copyOf(allProperties);
        }
        else {
            return model.getProperties();
        }
    }

    private void descriptionSection(String definitionName, Model model, MarkupDocBuilder docBuilder){
        if(handWrittenDescriptionsEnabled){
            String description = handWrittenPathDescription(definitionName.toLowerCase(), DESCRIPTION_FILE_NAME);
            if(isNotBlank(description)){
                docBuilder.paragraph(description);
            }else{
                if (logger.isInfoEnabled()) {
                    logger.info("Hand-written description cannot be read. Trying to use description from Swagger source.");
                }
                modelDescription(model, docBuilder);
            }
        }
        else{
            modelDescription(model, docBuilder);
        }
    }

    private void modelDescription(Model model, MarkupDocBuilder docBuilder) {
        String description = model.getDescription();
        if (isNotBlank(description)) {
            docBuilder.paragraph(description);
        }
    }

    /**
     * Reads a hand-written description
     *
     * @param descriptionFolder the name of the folder where the description file resides
     * @param descriptionFileName the name of the description file
     * @return the content of the file
     */
    private String handWrittenPathDescription(String descriptionFolder, String descriptionFileName){
        for (String fileNameExtension : markupLanguage.getFileNameExtensions()) {
            java.nio.file.Path path = Paths.get(descriptionsFolderPath, descriptionFolder, descriptionFileName + fileNameExtension);
            if (Files.isReadable(path)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Description file processed: {}", path);
                }
                try {
                    return FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim();
                } catch (IOException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(String.format("Failed to read description file: %s", path), e);
                    }
                }
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

    private void definitionSchema(String definitionName, MarkupDocBuilder docBuilder) {
        if(schemasEnabled) {
            if (isNotBlank(definitionName)) {
                schema(JSON_SCHEMA, schemasFolderPath, definitionName + JSON_SCHEMA_EXTENSION, JSON, docBuilder);
                schema(XML_SCHEMA, schemasFolderPath, definitionName + XML_SCHEMA_EXTENSION, XML, docBuilder);
            }
        }
    }

    private void schema(String title, String schemasFolderPath, String schemaName, String language, MarkupDocBuilder docBuilder) {
        java.nio.file.Path path = Paths.get(schemasFolderPath, schemaName);
        if (Files.isReadable(path)) {
            docBuilder.sectionTitleLevel3(title);
            try {
                docBuilder.source(FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8).trim(), language);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Failed to read schema file: %s", path), e);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Schema file processed: {}", path);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Schema file is not readable: {}", path);
            }
        }
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
     * @param uniquePrefix unique prefix to prepend to inline object names to enforce unicity
     * @param depth current inline schema depth
     * @param docBuilder the docbuilder do use for output
     */
    private void inlineDefinitions(List<ObjectType> definitions, String uniquePrefix, int depth, MarkupDocBuilder docBuilder) {
        if(CollectionUtils.isNotEmpty(definitions)){
            for (ObjectType definition: definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), docBuilder);
                List<ObjectType> localDefinitions = typeProperties(definition, uniquePrefix, depth, new DefinitionPropertyDescriptor(definition), new DefinitionDocumentResolverFromDefinition(), docBuilder);
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(Collections.singletonList(localDefinition), uniquePrefix, depth - 1, docBuilder);
            }
        }

    }

    /**
     * Overrides definition document resolver functor for inter-document cross-references from definitions files.
     * This implementation simplify the path between two definitions because all definitions are in the same path.
     */
    class DefinitionDocumentResolverFromDefinition extends DefinitionDocumentResolverDefault {

        public DefinitionDocumentResolverFromDefinition() {}

        public String apply(String definitionName) {
            String defaultResolver = super.apply(definitionName);

            if (defaultResolver != null && separatedDefinitionsEnabled)
                return interDocumentCrossReferencesPrefix + markupDocBuilder.addfileExtension(normalizeFileName(definitionName));
            else
                return defaultResolver;
        }
    }
}
