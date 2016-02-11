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

import com.google.common.collect.Ordering;

import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.builder.document.DefinitionsDocument;
import io.github.robwin.swagger2markup.builder.document.OverviewDocument;
import io.github.robwin.swagger2markup.builder.document.PathsDocument;
import io.github.robwin.swagger2markup.builder.document.SecurityDocument;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.utils.Consumer;
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverter {
    private static final Logger LOG = LoggerFactory.getLogger(Swagger2MarkupConverter.class);

    private final Swagger2MarkupConfig swagger2MarkupConfig;

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
        new OverviewDocument(swagger2MarkupConfig, directory).build().writeToFile(directory, swagger2MarkupConfig.getOverviewDocument(), StandardCharsets.UTF_8);
        new PathsDocument(swagger2MarkupConfig, directory).build().writeToFile(directory, swagger2MarkupConfig.getPathsDocument(), StandardCharsets.UTF_8);
        new DefinitionsDocument(swagger2MarkupConfig, directory).build().writeToFile(directory, swagger2MarkupConfig.getDefinitionsDocument(), StandardCharsets.UTF_8);
        new SecurityDocument(swagger2MarkupConfig, directory).build().writeToFile(directory, swagger2MarkupConfig.getSecurityDocument(), StandardCharsets.UTF_8);
    }

    /**
     * Returns all documents as a String

     * @return a the document as a String
     */
    private String buildDocuments() {
        StringBuilder sb = new StringBuilder();
        sb.append(new OverviewDocument(swagger2MarkupConfig, null).build().toString());
        sb.append(new PathsDocument(swagger2MarkupConfig, null).build().toString());
        sb.append(new DefinitionsDocument(swagger2MarkupConfig, null).build().toString());
        sb.append(new SecurityDocument(swagger2MarkupConfig, null).build().toString());
        return sb.toString();
    }


    public static class Builder{
        private final Swagger swagger;
        private String examplesFolderPath;
        private String schemasFolderPath;
        private String descriptionsFolderPath;
        private boolean separatedDefinitions;
        private boolean separatedPaths;
        private GroupBy pathsGroupedBy = GroupBy.AS_IS;
        private OrderBy definitionsOrderedBy = OrderBy.NATURAL;
        private MarkupLanguage markupLanguage = MarkupLanguage.ASCIIDOC;
        private Language outputLanguage = Language.EN;
        private int inlineSchemaDepthLevel = 0;
        private Comparator<String> tagOrdering = Ordering.natural();
        private Comparator<String> pathOrdering = Ordering.natural();
        private Comparator<HttpMethod> pathMethodOrdering = Ordering.explicit(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.HEAD, HttpMethod.OPTIONS);
        private Comparator<String> definitionOrdering = Ordering.natural();
        private boolean interDocumentCrossReferences = false;
        private String interDocumentCrossReferencesPrefix = "";

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
                    schemasFolderPath, descriptionsFolderPath, separatedDefinitions, separatedPaths, pathsGroupedBy, definitionsOrderedBy,
                    outputLanguage, inlineSchemaDepthLevel, tagOrdering, pathOrdering, pathMethodOrdering, definitionOrdering,
                    interDocumentCrossReferences, interDocumentCrossReferencesPrefix));
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
        public Builder withSeparatedPaths() {
            this.separatedPaths = true;
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
        @Deprecated
        public Builder withDefinitionsOrderedBy(OrderBy definitionsOrderedBy) {
            this.definitionsOrderedBy = definitionsOrderedBy;
            this.definitionOrdering = Ordering.natural();
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

        /**
         * Specifies a custom comparator function to order tags.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param tagOrdering
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withTagOrdering(Comparator<String> tagOrdering) {
            this.tagOrdering = tagOrdering;
            return this;
        }

        /**
         * Specifies a custom comparator function to order paths.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param pathOrdering
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withPathOrdering(Comparator<String> pathOrdering) {
            this.pathOrdering = pathOrdering;
            return this;
        }

        /**
         * Specifies a custom comparator function to order paths methods.
         * By default, explicit ordering is applied : GET, PUT, POST, DELETE, PATCH, HEAD, OPTIONS
         * Set ordering to null to keep swagger original order
         *
         * @param pathMethodOrdering
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withPathMethodOrdering(Comparator<HttpMethod> pathMethodOrdering) {
            this.pathMethodOrdering = pathMethodOrdering;
            return this;
        }

        /**
         * Specifies a custom comparator function to order definitions.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param definitionOrdering
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withDefinitionOrdering(Comparator<String> definitionOrdering) {
            this.definitionOrdering = definitionOrdering;
            return this;
        }

        /**
         * Enable use of inter-document cross-references when needed
         *
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withInterDocumentCrossReferences() {
            this.interDocumentCrossReferences = true;
            return this;
        }

        /**
         * Enable use of inter-document cross-references when needed
         *
         * @param prefix Prefix to document in all inter-document cross-references
         * @return the Swagger2MarkupConverter.Builder
         */
        public Builder withInterDocumentCrossReferences(String prefix) {
            this.interDocumentCrossReferences = true;
            this.interDocumentCrossReferencesPrefix = prefix;
            return this;
        }
    }

}
