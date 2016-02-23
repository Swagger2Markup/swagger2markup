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

import io.github.robwin.swagger2markup.builder.document.DefinitionsDocument;
import io.github.robwin.swagger2markup.builder.document.OverviewDocument;
import io.github.robwin.swagger2markup.builder.document.PathsDocument;
import io.github.robwin.swagger2markup.builder.document.SecurityDocument;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.utils.Consumer;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverter {
    private static final Logger LOG = LoggerFactory.getLogger(Swagger2MarkupConverter.class);

    private Swagger2MarkupConfig config;
    private Swagger swagger;

    /**
     * Creates a Swagger2MarkupConverter.Builder using a given Swagger source.
     *
     * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(String swaggerLocation) {
        Validate.notEmpty(swaggerLocation, "swaggerLocation must not be empty!");
        return new Builder(swaggerLocation);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger model.
     *
     * @param swagger the Swagger source.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Swagger swagger) {
        Validate.notNull(swagger, "Swagger must not be null!");
        return new Builder(swagger);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON String.
     * You should use {@link #from(Reader)} instead.
     *
     * @param swaggerAsString the Swagger YAML or JSON String.
     * @return a Swagger2MarkupConverter
     * @throws java.io.IOException if String can not be parsed
     */
    public static Builder fromString(String swaggerAsString) throws IOException {
        Validate.notEmpty(swaggerAsString, "swaggerAsString must not be null!");
        return from(new StringReader(swaggerAsString));
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON reader.
     *
     * @param swaggerReader the Swagger YAML or JSON reader.
     * @return a Swagger2MarkupConverter
     * @throws java.io.IOException if source can not be parsed
     */
    public static Builder from(Reader swaggerReader) throws IOException {
        Validate.notNull(swaggerReader, "swaggerReader must not be null!");
        Swagger swagger = new SwaggerParser().parse(IOUtils.toString(swaggerReader));
        if (swagger == null)
            throw new IllegalArgumentException("Swagger source is in the wrong format");

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
    public String asString() throws IOException {
        return buildDocuments();
    }

    /**
     * Builds all documents and writes them to a directory
     *
     * @param directory the directory where the generated file should be stored
     * @throws IOException if a file cannot be written
     */
    private void buildDocuments(String directory) throws IOException {
        new OverviewDocument(swagger,config, directory).build().writeToFile(directory, config.getOverviewDocument(), StandardCharsets.UTF_8);
        new PathsDocument(swagger,config, directory).build().writeToFile(directory, config.getPathsDocument(), StandardCharsets.UTF_8);
        new DefinitionsDocument(swagger,config, directory).build().writeToFile(directory, config.getDefinitionsDocument(), StandardCharsets.UTF_8);
        new SecurityDocument(swagger,config, directory).build().writeToFile(directory, config.getSecurityDocument(), StandardCharsets.UTF_8);
    }

    /**
     * Returns all documents as a String
     *
     * @return a the document as a String
     */
    private String buildDocuments() {
        StringBuilder sb = new StringBuilder();
        sb.append(new OverviewDocument(swagger,config, null).build().toString());
        sb.append(new PathsDocument(swagger,config, null).build().toString());
        sb.append(new DefinitionsDocument(swagger,config, null).build().toString());
        sb.append(new SecurityDocument(swagger,config, null).build().toString());
        return sb.toString();
    }

    public static class Builder {
        private final Swagger swagger;
        private Swagger2MarkupConfig config;

        /**
         * Creates a Builder using a given Swagger source.
         *
         * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
         */
        Builder(String swaggerLocation) {
            swagger = new SwaggerParser().read(swaggerLocation);
            if (swagger == null) {
                throw new IllegalArgumentException("Failed to read the Swagger source");
            }
        }

        /**
         * Creates a Builder using a given Swagger model.
         *
         * @param swagger the Swagger source.
         */
        Builder(Swagger swagger) {
            this.swagger = swagger;
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

        public Builder withConfig(Swagger2MarkupConfig config) {
            this.config = config;
            return this;
        }

        public Builder withConfig(Properties config) {
            this.config = Swagger2MarkupConfig.ofProperties(config).build();
            return this;
        }

        public Swagger2MarkupConverter build() {
            Swagger2MarkupConverter converter = new Swagger2MarkupConverter();
            converter.swagger = this.swagger;
            if (config == null)
                converter.config = Swagger2MarkupConfig.ofDefaults().build();
            else
                converter.config = config;

            return converter;
        }

    }

}
