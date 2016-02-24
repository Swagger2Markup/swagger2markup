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
import io.github.robwin.swagger2markup.extension.Extension;
import io.github.robwin.swagger2markup.extension.Swagger2MarkupExtensionRegistry;
import io.github.robwin.swagger2markup.extension.SwaggerExtension;
import io.github.robwin.swagger2markup.utils.Consumer;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverter {

    public static class Context {
        public Swagger2MarkupConfig config;
        public Swagger2MarkupExtensionRegistry extensionRegistry;
        public Swagger swagger;
        public String swaggerLocation;
    }

    Context globalContext;

    /**
     * Creates a Swagger2MarkupConverter.Builder using a given Swagger source.
     *
     * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(String swaggerLocation) {
        Validate.notEmpty(swaggerLocation, "swaggerLocation must not be empty");
        return new Builder(swaggerLocation);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger model.
     *
     * @param swagger the Swagger source.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Swagger swagger) {
        Validate.notNull(swagger, "Swagger must not be null");
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
        Validate.notEmpty(swaggerAsString, "swaggerAsString must not be null");
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
        Validate.notNull(swaggerReader, "swaggerReader must not be null");
        Swagger swagger = new SwaggerParser().parse(IOUtils.toString(swaggerReader));
        if (swagger == null)
            throw new IllegalArgumentException("Swagger source is in the wrong format");

        return new Builder(swagger);
    }

    protected void applySwaggerExtensions() {
        for (SwaggerExtension swaggerExtension : globalContext.extensionRegistry.getExtensions(SwaggerExtension.class)) {
            swaggerExtension.apply(globalContext);
        }
    }

    /**
     * Builds the document with the given markup language and stores
     * the files in the given folder.
     *
     * @param targetFolderPath the target folder
     * @throws IOException if the files cannot be written
     */
    public void intoFolder(String targetFolderPath) throws IOException {
        Validate.notEmpty(targetFolderPath, "folderPath must not be null");

        applySwaggerExtensions();
        buildDocuments(targetFolderPath);
    }

    /**
     * Builds the document with the given markup language and returns it as a String
     *
     * @return a the document as a String
     * @throws java.io.IOException if files can not be read
     */
    public String asString() throws IOException {
        applySwaggerExtensions();
        return buildDocuments();
    }

    /**
     * Builds all documents and writes them to a directory
     *
     * @param directory the directory where the generated file should be stored
     * @throws IOException if a file cannot be written
     */
    private void buildDocuments(String directory) throws IOException {
        new OverviewDocument(globalContext, directory).build().writeToFile(directory, globalContext.config.getOverviewDocument(), StandardCharsets.UTF_8);
        new PathsDocument(globalContext, directory).build().writeToFile(directory, globalContext.config.getPathsDocument(), StandardCharsets.UTF_8);
        new DefinitionsDocument(globalContext, directory).build().writeToFile(directory, globalContext.config.getDefinitionsDocument(), StandardCharsets.UTF_8);
        new SecurityDocument(globalContext, directory).build().writeToFile(directory, globalContext.config.getSecurityDocument(), StandardCharsets.UTF_8);
    }

    /**
     * Returns all documents as a String
     *
     * @return a the document as a String
     */
    private String buildDocuments() {
        StringBuilder sb = new StringBuilder();
        sb.append(new OverviewDocument(globalContext, null).build().toString());
        sb.append(new PathsDocument(globalContext, null).build().toString());
        sb.append(new DefinitionsDocument(globalContext, null).build().toString());
        sb.append(new SecurityDocument(globalContext, null).build().toString());
        return sb.toString();
    }

    public static class Builder {
        private final Swagger swagger;
        private final String swaggerLocation;
        private Swagger2MarkupConfig config;
        private Swagger2MarkupExtensionRegistry extensionRegistry;

        @Deprecated
        private SwaggerExtension preProcessSwaggerExtension;

        /**
         * Creates a Builder using a given Swagger source.
         *
         * @param swaggerLocation the Swagger location. Can be a HTTP url or a path to a local file.
         */
        Builder(String swaggerLocation) {
            this.swaggerLocation = swaggerLocation;
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
            this.swaggerLocation = null;
        }

        /**
         * Customize the Swagger data in any useful way.<br/>
         * Use the new extension system instead, by providing a {@link io.github.robwin.swagger2markup.extension.SwaggerExtension} extension.
         *
         * @param preProcessor function object to mutate the swagger object
         * @return the Swagger2MarkupConverter.Builder
         */
        @Deprecated
        public Builder preProcessSwagger(final Consumer<Swagger> preProcessor) {
            this.preProcessSwaggerExtension = new SwaggerExtension() {
                @Override
                public void apply(Context globalContext) {
                    preProcessor.accept(globalContext.swagger);
                }
            };
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

        public Builder withExtensionRegistry(Swagger2MarkupExtensionRegistry registry) {
            this.extensionRegistry = registry;
            return this;
        }

        public Swagger2MarkupConverter build() {
            Context context = new Context();

            context.swagger = this.swagger;
            context.swaggerLocation = this.swaggerLocation;

            if (config == null)
                context.config = Swagger2MarkupConfig.ofDefaults().build();
            else
                context.config = config;
            context.config.setGlobalContext(context);

            if (extensionRegistry == null)
                context.extensionRegistry = Swagger2MarkupExtensionRegistry.ofDefaults().build();
            else
                context.extensionRegistry = extensionRegistry;
            if (preProcessSwaggerExtension != null)
                context.extensionRegistry.registerExtension(preProcessSwaggerExtension);
            for (Extension extension : context.extensionRegistry.getExtensions())
                extension.setGlobalContext(context);

            Swagger2MarkupConverter converter = new Swagger2MarkupConverter();
            converter.globalContext = context;

            return converter;
        }

    }

}
