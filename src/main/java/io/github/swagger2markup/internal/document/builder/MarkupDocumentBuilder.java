/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.swagger2markup.internal.document.builder;

import ch.netzwerg.paleo.StringColumn;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.internal.type.*;
import io.github.swagger2markup.internal.utils.PropertyUtils;
import io.github.swagger2markup.internal.utils.Table;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupDocBuilders;
import io.github.swagger2markup.utils.IOUtils;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.internal.utils.MapUtils.toKeySet;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
abstract class MarkupDocumentBuilder {

    static final String COLON = " : ";

    final String DEFAULT_COLUMN;
    
    private final String MAXLENGTH_COLUMN;
    private final String MINLENGTH_COLUMN;
    private final String LENGTH_COLUMN;
    
    private final String PATTERN_COLUMN;
    private final String MINVALUE_COLUMN;
    private final String MINVALUE_EXCLUSIVE_COLUMN;
    private final String MAXVALUE_COLUMN;
    private final String MAXVALUE_EXCLUSIVE_COLUMN;

    
    private final String EXAMPLE_COLUMN;
    final String SCHEMA_COLUMN;
    final String NAME_COLUMN;
    final String DESCRIPTION_COLUMN;
    final String SCOPES_COLUMN;
    final String DESCRIPTION;
    final String PRODUCES;
    final String CONSUMES;
    protected final String TAGS;
    final String NO_CONTENT;
    final String FLAGS_COLUMN;
    final String FLAGS_REQUIRED;
    final String FLAGS_OPTIONAL;
    private final String FLAGS_READ_ONLY;
    

    Logger logger = LoggerFactory.getLogger(getClass());

    protected Swagger2MarkupConverter.Context globalContext;
    Swagger2MarkupExtensionRegistry extensionRegistry;
    protected Swagger2MarkupConfig config;
    MarkupDocBuilder markupDocBuilder;
    Path outputPath;

    MarkupDocumentBuilder(Swagger2MarkupConverter.Context globalContext, Swagger2MarkupExtensionRegistry extensionRegistry, Path outputPath) {
        this.globalContext = globalContext;
        this.extensionRegistry = extensionRegistry;
        this.config = globalContext.getConfig();
        this.outputPath = outputPath;

        this.markupDocBuilder = MarkupDocBuilders.documentBuilder(config.getMarkupLanguage(), config.getLineSeparator()).withAnchorPrefix(config.getAnchorPrefix());

        ResourceBundle labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
        DEFAULT_COLUMN = labels.getString("default_column");
        
        MINLENGTH_COLUMN = labels.getString("minlength_column");
        MAXLENGTH_COLUMN = labels.getString("maxlength_column");
        LENGTH_COLUMN = labels.getString("length_column");
        
        PATTERN_COLUMN = labels.getString("pattern_column");
        MINVALUE_COLUMN = labels.getString("minvalue_column");
        MAXVALUE_COLUMN = labels.getString("maxvalue_column");
        MINVALUE_EXCLUSIVE_COLUMN = labels.getString("minvalue_exclusive_column");
        MAXVALUE_EXCLUSIVE_COLUMN = labels.getString("maxvalue_exclusive_column");

        EXAMPLE_COLUMN = labels.getString("example_column");
        FLAGS_COLUMN = labels.getString("flags.column");
        FLAGS_REQUIRED = labels.getString("flags.required");
        FLAGS_OPTIONAL = labels.getString("flags.optional");
        FLAGS_READ_ONLY = labels.getString("flags.read_only");
        SCHEMA_COLUMN = labels.getString("schema_column");
        NAME_COLUMN = labels.getString("name_column");
        DESCRIPTION_COLUMN = labels.getString("description_column");
        SCOPES_COLUMN = labels.getString("scopes_column");
        DESCRIPTION = DESCRIPTION_COLUMN;
        PRODUCES = labels.getString("produces");
        CONSUMES = labels.getString("consumes");
        TAGS = labels.getString("tags");
        NO_CONTENT = labels.getString("no_content");
    }

    /**
     * Builds the MarkupDocument.
     *
     * @return the built MarkupDocument
     * @throws IOException if the files to include are not readable
     */
    public abstract MarkupDocument build() throws IOException;

    /**
     * Returns a RefType to a new inlined type named with {@code name} and {@code uniqueName}.<br>
     * The returned RefType point to the new inlined type which is added to the {@code inlineDefinitions} collection.<br>
     * The function is recursive and support collections (ArrayType and MapType).<br>
     * The function is transparent : {@code type} is returned as-is if type is not inlinable or if !config.isInlineSchemaEnabled().<br> 
     * 
     * @param type type to inline
     * @param name name of the created inline ObjectType
     * @param uniqueName unique name of the created inline ObjectType
     * @param inlineDefinitions a non null collection of inline ObjectType
     * @return the type referencing the newly created inline ObjectType. Can be a RefType, an ArrayType or a MapType
     */
    Type createInlineType(Type type, String name, String uniqueName, List<ObjectType> inlineDefinitions) {
        if (!config.isInlineSchemaEnabled())
            return type;
        
        if (type instanceof ObjectType) {
            return createInlineObjectType(type, name, uniqueName, inlineDefinitions);
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            arrayType.setOfType(createInlineType(arrayType.getOfType(), name, uniqueName, inlineDefinitions));

            return arrayType;
        } else if (type instanceof MapType) {
            MapType mapType = (MapType)type;
            if (mapType.getValueType() instanceof ObjectType)
                mapType.setValueType(createInlineType(mapType.getValueType(), name, uniqueName, inlineDefinitions));

            return mapType;
        } else {
            return type;
        }
    }

    private Type createInlineObjectType(Type type, String name, String uniqueName, List<ObjectType> inlineDefinitions) {
        if (type instanceof ObjectType) {
            ObjectType objectType = (ObjectType)type;
            if (MapUtils.isNotEmpty(objectType.getProperties())) {
                if (objectType.getName() == null) {
                    objectType.setName(name);
                    objectType.setUniqueName(uniqueName);
                }
                inlineDefinitions.add(objectType);
                return new RefType(objectType);
            } else
                return type;
        } else
            return type;
    }

    /**
     * Build a generic property table
     *
     * @param properties                 properties to display
     * @param uniquePrefix               unique prefix to prepend to inline object names to enforce unicity
     * @param definitionDocumentResolver definition document resolver to apply to property type cross-reference
     * @param docBuilder                 the docbuilder do use for output
     * @return a list of inline schemas referenced by some properties, for later display
     */
    List<ObjectType> buildPropertiesTable(Map<String, Property> properties, String uniquePrefix, DefinitionDocumentResolver definitionDocumentResolver, MarkupDocBuilder docBuilder) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();

        StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(NAME_COLUMN))
                .putMetaData(Table.WIDTH_RATIO, "3");
        StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(DESCRIPTION_COLUMN))
                .putMetaData(Table.WIDTH_RATIO, "11")
                .putMetaData(Table.HEADER_COLUMN, "true");
        StringColumn.Builder schemaColumnBuilder = StringColumn.builder(StringColumnId.of(SCHEMA_COLUMN))
                .putMetaData(Table.WIDTH_RATIO, "4")
                .putMetaData(Table.HEADER_COLUMN, "true");

        if (MapUtils.isNotEmpty(properties)) {
            Set<String> propertyNames = toKeySet(properties, config.getPropertyOrdering());
            for (String propertyName : propertyNames) {
                Property property = properties.get(propertyName);
                Type propertyType = PropertyUtils.getType(property, definitionDocumentResolver);

                propertyType = createInlineType(propertyType, propertyName, uniquePrefix + " " + propertyName, inlineDefinitions);
                
                Object example = PropertyUtils.getExample(config.isGeneratedExamplesEnabled(), property, markupDocBuilder);

                Object defaultValue = PropertyUtils.getDefaultValue(property);
                
                Integer maxlength = PropertyUtils.getMaxlength(property);
                Integer minlength = PropertyUtils.getMinlength(property);
                String pattern = PropertyUtils.getPattern(property);
                Number minValue = PropertyUtils.getMin(property);
                Boolean exclusiveMin = PropertyUtils.getExclusiveMin(property);
                Number maxValue = PropertyUtils.getMax(property);
                Boolean exclusiveMax = PropertyUtils.getExclusiveMax(property);

                MarkupDocBuilder propertyNameContent = copyMarkupDocBuilder();
                propertyNameContent.boldTextLine(propertyName, true);
                if (isTrue(property.getRequired()))
                    propertyNameContent.italicText(FLAGS_REQUIRED.toLowerCase());
                else
                    propertyNameContent.italicText(FLAGS_OPTIONAL.toLowerCase());
                if (isTrue(property.getReadOnly())) {
                    propertyNameContent.newLine(true);
                    propertyNameContent.italicText(FLAGS_READ_ONLY.toLowerCase());
                }
                
                MarkupDocBuilder descriptionContent = copyMarkupDocBuilder();
                String description = swaggerMarkupDescription(property.getDescription());
                if (isNotBlank(description))
                    descriptionContent.text(description);
                
                if(defaultValue != null){
                    if (isNotBlank(descriptionContent.toString()))
                        descriptionContent.newLine(true);
                    descriptionContent.boldText(DEFAULT_COLUMN).text(COLON).literalText(Json.pretty(defaultValue));
                }
                
                if (minlength != null && maxlength != null) {
                    // combination of minlength/maxlength
                	
                	if (isNotBlank(descriptionContent.toString()))
                        descriptionContent.newLine(true);
                	
                	String lengthRange = minlength + " - " + maxlength;
                	if (minlength.equals(maxlength)) {
                		lengthRange = minlength.toString();
                	}
                	
                    descriptionContent.boldText(LENGTH_COLUMN).text(COLON).literalText(lengthRange);
                    
                } else {
                	 if(minlength != null){
                     	if (isNotBlank(descriptionContent.toString()))
                             descriptionContent.newLine(true);
                         descriptionContent.boldText(MINLENGTH_COLUMN).text(COLON).literalText(minlength.toString());
                     }
                     
                     if(maxlength != null){
                     	if (isNotBlank(descriptionContent.toString()))
                             descriptionContent.newLine(true);
                         descriptionContent.boldText(MAXLENGTH_COLUMN).text(COLON).literalText(maxlength.toString());
                     }
                }
                
               
                
                if(pattern != null){
                	if (isNotBlank(descriptionContent.toString()))
                        descriptionContent.newLine(true);
                	
                    descriptionContent.boldText(PATTERN_COLUMN).text(COLON).literalText(Json.pretty(pattern));
                }

                if(minValue != null){
                	if (isNotBlank(descriptionContent.toString()))
                        descriptionContent.newLine(true);

                    String minValueColumn = isTrue(exclusiveMin) ? MINVALUE_EXCLUSIVE_COLUMN : MINVALUE_COLUMN;
                    descriptionContent.boldText(minValueColumn).text(COLON).literalText(minValue.toString());
                }
                
                if(maxValue != null){
                	if (isNotBlank(descriptionContent.toString()))
                        descriptionContent.newLine(true);

                    String maxValueColumn = isTrue(exclusiveMax) ? MAXVALUE_EXCLUSIVE_COLUMN : MAXVALUE_COLUMN;
                    descriptionContent.boldText(maxValueColumn).text(COLON).literalText(maxValue.toString());
                }
                
                if (example != null) {
                    if (isNotBlank(description) || defaultValue != null)
                        descriptionContent.newLine(true);
                    descriptionContent.boldText(EXAMPLE_COLUMN).text(COLON).literalText(Json.pretty(example));
                }

                nameColumnBuilder.add(propertyNameContent.toString());
                descriptionColumnBuilder.add(descriptionContent.toString());
                schemaColumnBuilder.add(propertyType.displaySchema(docBuilder));
            }

            Table table = Table.ofAll(
                    nameColumnBuilder.build(),
                    descriptionColumnBuilder.build(),
                    schemaColumnBuilder.build());
            docBuilder.tableWithColumnSpecs(table.getColumnSpecs(), table.getCells());
        } else {
            docBuilder.textLine(NO_CONTENT);
        }

        return inlineDefinitions;
    }

    MarkupDocBuilder copyMarkupDocBuilder() {
        return markupDocBuilder.copy(false);
    }

    String boldText(String text) {
        return copyMarkupDocBuilder().boldText(text).toString();
    }

    protected String italicText(String text) {
        return copyMarkupDocBuilder().italicText(text).toString();
    }

    String literalText(String text) {
        return copyMarkupDocBuilder().literalText(text).toString();
    }
    
    /**
     * Returns converted markup text from Swagger.
     *
     * @param markupText text to convert, or empty string
     * @return converted markup text, or an empty string if {@code markupText} == null
     */
    String swaggerMarkupDescription(String markupText) {
        if (markupText == null)
            return StringUtils.EMPTY;
        return copyMarkupDocBuilder().importMarkup(new StringReader(markupText), globalContext.getConfig().getSwaggerMarkupLanguage()).toString().trim();
    }

    void buildDescriptionParagraph(String description, MarkupDocBuilder docBuilder) {
        if (isNotBlank(description)) {
            docBuilder.paragraph(swaggerMarkupDescription(description));
        }
    }

    /**
     * Default {@code DefinitionDocumentResolver} functor
     */
    class DefinitionDocumentResolverDefault implements DefinitionDocumentResolver {

        DefinitionDocumentResolverDefault() {
        }

        public String apply(String definitionName) {
            if (!config.isInterDocumentCrossReferencesEnabled() || outputPath == null)
                return null;
            else if (config.isSeparatedDefinitionsEnabled())
                return defaultString(config.getInterDocumentCrossReferencesPrefix()) + new File(config.getSeparatedDefinitionsFolder(), markupDocBuilder.addFileExtension(IOUtils.normalizeName(definitionName))).getPath();
            else
                return defaultString(config.getInterDocumentCrossReferencesPrefix()) + markupDocBuilder.addFileExtension(config.getDefinitionsDocument());
        }
    }
}
