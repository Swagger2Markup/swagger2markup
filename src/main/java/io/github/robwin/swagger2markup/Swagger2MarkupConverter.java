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
package io.github.robwin.swagger2markup;

import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.builder.document.DefinitionsDocument;
import io.github.robwin.swagger2markup.builder.document.OverviewDocument;
import io.github.robwin.swagger2markup.builder.document.PathsDocument;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.utils.Consumer;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverter {
    private static final Logger LOG = LoggerFactory.getLogger(Swagger2MarkupConverter.class);

    private final Swagger2MarkupConfig swagger2MarkupConfig;
    private static final String OVERVIEW_DOCUMENT = "overview";
    private static final String PATHS_DOCUMENT = "paths";
    private static final String DEFINITIONS_DOCUMENT = "definitions";

    /**
     * @param swagger2MarkupConfig the configuration
     */
    Swagger2MarkupConverter(Swagger2MarkupConfig swagger2MarkupConfig){
        this.swagger2MarkupConfig = swagger2MarkupConfig;
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder using a given Swagger source.
     *
     * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(String swaggerLocation){
        Validate.notEmpty(swaggerLocation, "swaggerLocation must not be empty!");
        return new Builder(swaggerLocation);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger model.
     *
     * @param swagger the Swagger source.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Swagger swagger){
        Validate.notNull(swagger, "Swagger must not be null!");
        return new Builder(swagger);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON String.
     *
     * @param swaggerAsString the Swagger YAML or JSON String.
     * @return a Swagger2MarkupConverter
     * @throws java.io.IOException if String can not be parsed
     */
    public static Builder fromString(String swaggerAsString) throws IOException {
        Validate.notEmpty(swaggerAsString, "Swagger String must not be null!");
        Swagger swagger = new SwaggerParser().parse(swaggerAsString);
        if(swagger == null)
            throw new IllegalArgumentException("Swagger String is in the wrong format");

        return new Builder(swagger);
    }

    /**
     * Builds the document with the given markup language and stores
     * the files in the given folder.
     *
     * @param targetFolderPath the target folder
     * @throws IOException if the files cannot be written
     */
    public void intoFolder(String targetFolderPath) throws IOException {
        Validate.notEmpty(targetFolderPath, "folderPath must not be null!");
        buildDocuments(targetFolderPath);
    }

    /**
     * Builds the document with the given markup language and returns it as a String
     *
     * @return a the document as a String
     * @throws java.io.IOException if files can not be read
     */
    public String asString() throws IOException{
        return buildDocuments();
    }

    /**
     * Builds all documents and writes them to a directory

     * @param directory the directory where the generated file should be stored
     * @throws IOException if a file cannot be written
     */
    private void buildDocuments(String directory) throws IOException {
        new OverviewDocument(swagger2MarkupConfig).build().writeToFile(directory, OVERVIEW_DOCUMENT, StandardCharsets.UTF_8);
        new PathsDocument(swagger2MarkupConfig, directory).build().writeToFile(directory, PATHS_DOCUMENT, StandardCharsets.UTF_8);
        new DefinitionsDocument(swagger2MarkupConfig, directory).build().writeToFile(directory, DEFINITIONS_DOCUMENT, StandardCharsets.UTF_8);
    }

    /**
     * Returns all documents as a String

     * @return a the document as a String
     */
    private String buildDocuments() {
        return new OverviewDocument(swagger2MarkupConfig).build().toString()
                .concat(new PathsDocument(swagger2MarkupConfig, null).build().toString()
                .concat(new DefinitionsDocument(swagger2MarkupConfig, null).build().toString()));
    }


    public static class Builder{
        private final Swagger swagger;
        private String examplesFolderPath;
        private String schemasFolderPath;
        private String descriptionsFolderPath;
        private boolean separatedDefinitions;
        private boolean separatedOperations;
        private GroupBy pathsGroupedBy = GroupBy.AS_IS;
        private OrderBy definitionsOrderedBy = OrderBy.NATURAL;
        private MarkupLanguage markupLanguage = MarkupLanguage.ASCIIDOC;
        private Language outputLanguage = Language.EN;
        private int inlineSchemaDepthLevel = 0;

        /**
         * Creates a Builder using a given Swagger source.
         *
         * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
         */
        Builder(String swaggerLocation){
            swagger = new SwaggerParser().read(swaggerLocation);
            if(swagger == null){
                throw new IllegalArgumentException("Failed to read the Swagger file. ");
            }
        }

        /**
         * Creates a Builder using a given Swagger model.
         *
         * @param swagger the Swagger source.
         */
        Builder(Swagger swagger){
            this.swagger = swagger;
        }

        public Swagger2MarkupConverter build(){
            return new Swagger2MarkupConverter(new Swagger2MarkupConfig(swagger, markupLanguage, examplesFolderPath,
                    schemasFolderPath, descriptionsFolderPath, separatedDefinitions, separatedOperations, pathsGroupedBy, definitionsOrderedBy,
                    outputLanguage, inlineSchemaDepthLevel));
        }

        /**
         * Specifies the markup language which should be used to generate the files
         *
         * @param markupLanguage the markup language which is used to generate the files
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withMarkupLanguage(MarkupLanguage markupLanguage){
            this.markupLanguage = markupLanguage;
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths and Definitions document
         *
         * @param descriptionsFolderPath the path to the folder where the description documents reside
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withDescriptions(String descriptionsFolderPath){
            this.descriptionsFolderPath = descriptionsFolderPath;
            return this;
        }

        /**
         * In addition to the definitions file, also create separate definition files for each model definition.
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withSeparatedDefinitions() {
            this.separatedDefinitions = true;
            return this;
        }

        /**
         * In addition to the paths file, also create separate path files for each path.
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withSeparatedOperations() {
            this.separatedOperations = true;
            return this;
        }

        /**
         * Include examples into the Paths document
         *
         * @param examplesFolderPath the path to the folder where the example documents reside
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withExamples(String examplesFolderPath){
            this.examplesFolderPath = examplesFolderPath;
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document
         *
         * @param schemasFolderPath the path to the folder where the schema documents reside
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withSchemas(String schemasFolderPath){
            this.schemasFolderPath = schemasFolderPath;
            return this;
        }

        /**
         * Customize the Swagger data in any useful way
         *
         * @param preProcessor function object to mutate the swagger object
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder preProcessSwagger(Consumer<Swagger> preProcessor) {
            preProcessor.accept(this.swagger);
            return this;
        }

        /**
         * Specifies if the paths should be grouped by tags or stay as-is.
         *
         * @param pathsGroupedBy the GroupBy enum
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withPathsGroupedBy(GroupBy pathsGroupedBy) {
            this.pathsGroupedBy = pathsGroupedBy;
            return this;
        }

        /**
         * Specifies if the definitions should be ordered by natural ordering or stay as-is.
         *
         * @param definitionsOrderedBy the OrderBy enum
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withDefinitionsOrderedBy(OrderBy definitionsOrderedBy) {
            this.definitionsOrderedBy = definitionsOrderedBy;
            return this;
        }

        /**
         * Specifies labels language of output files
         *
         * @param language the enum
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withOutputLanguage(Language language) {
            this.outputLanguage = language;
            return this;
        }

        /**
         * Specifies maximum depth level for inline object schema displaying (0 = no inline schemas)
         *
         * @param inlineSchemaDepthLevel
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withInlineSchemaDepthLevel(int inlineSchemaDepthLevel) {
            this.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
            return this;
        }
    }

}
