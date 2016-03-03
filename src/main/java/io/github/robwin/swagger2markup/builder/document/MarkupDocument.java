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
package io.github.robwin.swagger2markup.builder.document;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilders;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.markup.builder.MarkupTableColumn;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.type.DefinitionDocumentResolver;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.github.robwin.swagger2markup.utils.IOUtils;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import io.swagger.models.properties.Property;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * @author Robert Winkler
 */
public abstract class MarkupDocument {

    protected final String DEFAULT_COLUMN;
    protected final String EXAMPLE_COLUMN;
    protected final String REQUIRED_COLUMN;
    protected final String SCHEMA_COLUMN;
    protected final String NAME_COLUMN;
    protected final String DESCRIPTION_COLUMN;
    protected final String SCOPES_COLUMN;
    protected final String DESCRIPTION;
    protected final String PRODUCES;
    protected final String CONSUMES;
    protected final String TAGS;
    protected final String NO_CONTENT;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected Swagger2MarkupConverter.Context globalContext;
    protected Swagger2MarkupConfig config;
    protected MarkupDocBuilder markupDocBuilder;
    protected Path outputPath;

    MarkupDocument(Swagger2MarkupConverter.Context globalContext, Path outputPath) {
        this.globalContext = globalContext;
        this.config = globalContext.config;
        this.outputPath = outputPath;

        this.markupDocBuilder = MarkupDocBuilders.documentBuilder(config.getMarkupLanguage()).withAnchorPrefix(config.getAnchorPrefix());

        ResourceBundle labels = ResourceBundle.getBundle("io/github/robwin/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
        DEFAULT_COLUMN = labels.getString("default_column");
        EXAMPLE_COLUMN = labels.getString("example_column");
        REQUIRED_COLUMN = labels.getString("required_column");
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
     * Returns a string representation of the document.
     */
    public String toString() {
        return markupDocBuilder.toString();
    }

    /**
     * Writes the content of the builder to a file and clears the builder.
     *
     * @param file    the generated file
     * @param charset the the charset to use for encoding
     * @throws IOException if the file cannot be written
     */
    public void writeToFile(Path file, Charset charset) throws IOException {
        markupDocBuilder.writeToFile(file, charset);
    }

    /**
     * Build a generic property table for any ObjectType
     *
     * @param type                       to display
     * @param uniquePrefix               unique prefix to prepend to inline object names to enforce unicity
     * @param depth                      current inline schema object depth
     * @param propertyDescriptor         property descriptor to apply to properties
     * @param definitionDocumentResolver definition document resolver to apply to property type cross-reference
     * @param docBuilder                 the docbuilder do use for output
     * @return a list of inline schemas referenced by some properties, for later display
     */
    protected List<ObjectType> typeProperties(ObjectType type, String uniquePrefix, int depth, PropertyDescriptor propertyDescriptor, DefinitionDocumentResolver definitionDocumentResolver, MarkupDocBuilder docBuilder) {
        List<ObjectType> localDefinitions = new ArrayList<>();
        List<List<String>> cells = new ArrayList<>();
        List<MarkupTableColumn> cols = Arrays.asList(
                new MarkupTableColumn(NAME_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1h"),
                new MarkupTableColumn(DESCRIPTION_COLUMN, 6).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^6"),
                new MarkupTableColumn(REQUIRED_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                new MarkupTableColumn(SCHEMA_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                new MarkupTableColumn(DEFAULT_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"),
                new MarkupTableColumn(EXAMPLE_COLUMN, 1).withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^1"));
        if (MapUtils.isNotEmpty(type.getProperties())) {
            Set<String> propertyNames;
            if (config.getPropertyOrdering() == null)
                propertyNames = new LinkedHashSet<>();
            else
                propertyNames = new TreeSet<>(config.getPropertyOrdering());
            propertyNames.addAll(type.getProperties().keySet());

            for (String propertyName : propertyNames) {
                Property property = type.getProperties().get(propertyName);
                Type propertyType = PropertyUtils.getType(property, definitionDocumentResolver);
                if (depth > 0 && propertyType instanceof ObjectType) {
                    if (MapUtils.isNotEmpty(((ObjectType) propertyType).getProperties())) {
                        propertyType.setName(propertyName);
                        propertyType.setUniqueName(uniquePrefix + " " + propertyName);
                        localDefinitions.add((ObjectType) propertyType);

                        propertyType = new RefType(propertyType);
                    }
                }

                List<String> content = Arrays.asList(
                        propertyName,
                        propertyDescriptor.getDescription(property, propertyName),
                        Boolean.toString(property.getRequired()),
                        propertyType.displaySchema(docBuilder),
                        PropertyUtils.getDefaultValue(property),
                        PropertyUtils.getExample(globalContext.config.isGeneratedExamplesEnabled(), property, markupDocBuilder)
                );
                cells.add(content);
            }
            docBuilder.tableWithColumnSpecs(cols, cells);
        } else {
            docBuilder.textLine(NO_CONTENT);
        }

        return localDefinitions;
    }

    /**
     * A functor to return descriptions for a given property
     */
    class PropertyDescriptor {
        protected Type type;

        public PropertyDescriptor(Type type) {
            this.type = type;
        }

        public String getDescription(Property property, String propertyName) {
            return defaultString(property.getDescription());
        }
    }

    /**
     * Default {@code DefinitionDocumentResolver} functor
     */
    class DefinitionDocumentResolverDefault implements DefinitionDocumentResolver {

        public DefinitionDocumentResolverDefault() {
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
