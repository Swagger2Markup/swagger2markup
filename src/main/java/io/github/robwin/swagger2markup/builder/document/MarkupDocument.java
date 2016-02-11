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

import com.google.common.base.Function;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilders;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.markup.builder.MarkupTableColumn;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.github.robwin.swagger2markup.utils.PropertyUtils;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * @author Robert Winkler
 */
public abstract class MarkupDocument {

    protected final String DEFAULT_COLUMN;
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
    protected Swagger swagger;
    protected MarkupLanguage markupLanguage;
    protected MarkupDocBuilder markupDocBuilder;
    protected boolean separatedDefinitionsEnabled;
    protected String separatedDefinitionsFolder;
    protected String definitionsDocument;
    protected String outputDirectory;


    protected static AtomicInteger typeIdCount = new AtomicInteger(0);

    MarkupDocument(Swagger2MarkupConfig swagger2MarkupConfig, String outputDirectory) {
        this.swagger = swagger2MarkupConfig.getSwagger();
        this.markupLanguage = swagger2MarkupConfig.getMarkupLanguage();
        this.markupDocBuilder = MarkupDocBuilders.documentBuilder(markupLanguage);
        this.separatedDefinitionsEnabled = swagger2MarkupConfig.isSeparatedDefinitions();
        this.separatedDefinitionsFolder = swagger2MarkupConfig.getSeparatedDefinitionsFolder();
        this.definitionsDocument = swagger2MarkupConfig.getDefinitionsDocument();
        this.outputDirectory = outputDirectory;

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        DEFAULT_COLUMN = labels.getString("default_column");
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

    protected String normalizeDefinitionFileName(String definitionName) {
        return definitionName.toLowerCase();
    }

    protected String resolveDefinitionDocument(String definitionName, String relativePath) {
        if (this.outputDirectory == null)
            return null;
        else if (this.separatedDefinitionsEnabled)
            return new File(new File(relativePath, this.separatedDefinitionsFolder), this.markupDocBuilder.addfileExtension(normalizeDefinitionFileName(definitionName))).getPath();
        else
            //return new File(relativePath, this.markupDocBuilder.addfileExtension(this.definitionsDocument)).getPath();
            return null;
    }

    protected String resolveDefinitionDocument(String definitionName) {
        return resolveDefinitionDocument(definitionName, null);
    }

    class DefinitionDocumentResolver implements Function<String, String> {
        private String relativePath;

        public DefinitionDocumentResolver(String relativePath) {
            this.relativePath = relativePath;
        }

        public DefinitionDocumentResolver() {}

        @Nullable
        @Override
        public String apply(@Nullable String definitionName) {
            return resolveDefinitionDocument(definitionName, relativePath);
        }
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
     * @param directory the directory where the generated file should be stored
     * @param fileName  the name of the file
     * @param charset   the the charset to use for encoding
     * @throws IOException if the file cannot be written
     */
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        markupDocBuilder.writeToFile(directory, fileName, charset);
    }

    public String uniqueTypeName(String name) {
        return name + "-" + typeIdCount.getAndIncrement();
    }

    public List<Type> typeProperties(Type type, int depth, PropertyDescriptor propertyDescriptor, String definitionsRelativePath, MarkupDocBuilder docBuilder) {
        List<Type> localDefinitions = new ArrayList<>();
        if (type instanceof ObjectType) {
            ObjectType objectType = (ObjectType) type;
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(
                    new MarkupTableColumn(NAME_COLUMN, 1),
                    new MarkupTableColumn(DESCRIPTION_COLUMN, 6),
                    new MarkupTableColumn(REQUIRED_COLUMN, 1),
                    new MarkupTableColumn(SCHEMA_COLUMN, 1),
                    new MarkupTableColumn(DEFAULT_COLUMN, 1));
            if (MapUtils.isNotEmpty(objectType.getProperties())) {
                for (Map.Entry<String, Property> propertyEntry : objectType.getProperties().entrySet()) {
                    Property property = propertyEntry.getValue();
                    String propertyName = propertyEntry.getKey();
                    Type propertyType = PropertyUtils.getType(property, new DefinitionDocumentResolver(definitionsRelativePath));
                    if (depth > 0 && propertyType instanceof ObjectType) {
                        if (MapUtils.isNotEmpty(((ObjectType) propertyType).getProperties())) {
                            propertyType.setName(propertyName);
                            propertyType.setUniqueName(uniqueTypeName(propertyName));
                            localDefinitions.add(propertyType);

                            propertyType = new RefType(propertyType);
                        }
                    }

                    List<String> content = Arrays.asList(
                            propertyName,
                            propertyDescriptor.getDescription(property, propertyName),
                            Boolean.toString(property.getRequired()),
                            propertyType.displaySchema(docBuilder),
                            PropertyUtils.getDefaultValue(property));
                    cells.add(content);
                }
                docBuilder.tableWithColumnSpecs(cols, cells);
            }
            else {
                docBuilder.textLine(NO_CONTENT);
            }
        }
        else {
            docBuilder.textLine(NO_CONTENT);
        }

        return localDefinitions;
    }

    public class PropertyDescriptor {
        protected Type type;

        public PropertyDescriptor(Type type) {
            this.type = type;
        }

        public String getDescription(Property property, String propertyName) {
            return defaultString(property.getDescription());
        }
    }

}
