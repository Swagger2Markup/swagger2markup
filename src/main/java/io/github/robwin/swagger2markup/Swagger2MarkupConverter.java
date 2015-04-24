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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.util.Json;
import com.wordnik.swagger.util.Yaml;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.builder.document.DefinitionsDocument;
import io.github.robwin.swagger2markup.builder.document.OverviewDocument;
import io.github.robwin.swagger2markup.builder.document.PathsDocument;
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

    private final Swagger swagger;
    private final MarkupLanguage markupLanguage;
    private final String examplesFolderPath;
    private final String schemasFolderPath;
    private static final String OVERVIEW_DOCUMENT = "overview";
    private static final String PATHS_DOCUMENT = "paths";
    private static final String DEFINITIONS_DOCUMENT = "definitions";

    /**
     * @param markupLanguage the markup language which is used to generate the files
     * @param swagger the Swagger object
     * @param examplesFolderPath the folderPath where examples are stored
     * @param schemasFolderPath the folderPath where (XML, JSON)-Schema  files are stored
     */
    Swagger2MarkupConverter(MarkupLanguage markupLanguage, Swagger swagger, String examplesFolderPath, String schemasFolderPath){
        this.markupLanguage = markupLanguage;
        this.swagger = swagger;
        this.examplesFolderPath = examplesFolderPath;
        this.schemasFolderPath = schemasFolderPath;
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
        Validate.notNull(swagger, "swagger must not be null!");
        return new Builder(swagger);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON String.
     *
     * @param swagger the Swagger YAML or JSON String.
     * @return a Swagger2MarkupConverter
     */
    public static Builder fromString(String swagger) throws IOException {
        Validate.notEmpty(swagger, "swagger must not be null!");
        ObjectMapper mapper;
        if(swagger.trim().startsWith("{")) {
            mapper = Json.mapper();
        }else {
            mapper = Yaml.mapper();
        }
        JsonNode rootNode = mapper.readTree(swagger);

        // must have swagger node set
        JsonNode swaggerNode = rootNode.get("swagger");
        if(swaggerNode == null)
            throw new IllegalArgumentException("Swagger String is in the wrong format");

        return new Builder(mapper.convertValue(rootNode, Swagger.class));
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
     */
    public String asString() throws IOException{
        return buildDocuments();
    }

    /**
     * Writes a file for the Paths (API) and a file for the Definitions (Model)

     * @param directory the directory where the generated file should be stored
     * @throws IOException if a file cannot be written
     */
    private void buildDocuments(String directory) throws IOException {
        new OverviewDocument(swagger, markupLanguage).build().writeToFile(directory, OVERVIEW_DOCUMENT, StandardCharsets.UTF_8);
        new PathsDocument(swagger, markupLanguage, examplesFolderPath).build().writeToFile(directory, PATHS_DOCUMENT, StandardCharsets.UTF_8);
        new DefinitionsDocument(swagger, markupLanguage, schemasFolderPath).build().writeToFile(directory, DEFINITIONS_DOCUMENT, StandardCharsets.UTF_8);
    }

    /**
     * Returns a file for the Paths (API) and a file for the Definitions (Model)

     * @return a the document as a String
     */
    private String buildDocuments() throws IOException {
        return new OverviewDocument(swagger, markupLanguage).build().toString().concat(
                new PathsDocument(swagger, markupLanguage, examplesFolderPath).build().toString()
                .concat(new DefinitionsDocument(swagger, markupLanguage, schemasFolderPath).build().toString()));
    }


    public static class Builder{
        private final Swagger swagger;
        private String examplesFolderPath;
        private String schemasFolderPath;
        private MarkupLanguage markupLanguage = MarkupLanguage.ASCIIDOC;

        /**
         * Creates a Builder using a given Swagger source.
         *
         * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
         */
        Builder(String swaggerLocation){
            swagger = new SwaggerParser().read(swaggerLocation);
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
            return new Swagger2MarkupConverter(markupLanguage, swagger, examplesFolderPath, schemasFolderPath);
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

    }

}
