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
package io.github.swagger2markup;

import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.builder.Swagger2MarkupExtensionRegistryBuilder;
import io.github.swagger2markup.internal.document.builder.DefinitionsDocumentBuilder;
import io.github.swagger2markup.internal.document.builder.OverviewDocumentBuilder;
import io.github.swagger2markup.internal.document.builder.PathsDocumentBuilder;
import io.github.swagger2markup.internal.document.builder.SecurityDocumentBuilder;
import io.github.swagger2markup.spi.*;
import io.github.swagger2markup.utils.URIUtils;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverter {

    private Context context;

    private Swagger2MarkupExtensionRegistry extensionRegistry;

    public Swagger2MarkupConverter(Context globalContext, Swagger2MarkupExtensionRegistry extensionRegistry) {
        this.context = globalContext;
        this.extensionRegistry = extensionRegistry;
    }

    /**
     * Returns the global Context
     *
     * @return the global Context
     */
    Context getContext(){
        return context;
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a URI.
     *
     * @param swaggerUri the URI
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(URI swaggerUri) {
        Validate.notNull(swaggerUri, "swaggerUri must not be null");
        String scheme = swaggerUri.getScheme();
        if(scheme != null && swaggerUri.getScheme().startsWith("http")){
            try {
                return from(swaggerUri.toURL());
            }
            catch (MalformedURLException e) {
                throw new RuntimeException("Failed to convert URI to URL", e);
            }
        } else if(scheme != null && swaggerUri.getScheme().startsWith("file")){
            return from(Paths.get(swaggerUri));
        }
        else {
            return from(URIUtils.convertUriWithoutSchemeToFileScheme(swaggerUri));
        }
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder using a remote URL.
     *
     * @param swaggerURL the remote URL
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(URL swaggerURL){
        Validate.notNull(swaggerURL, "swaggerURL must not be null");
        return new Builder(swaggerURL);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder using a local Path.
     *
     * @param swaggerPath the local Path
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Path swaggerPath) {
        Validate.notNull(swaggerPath, "swaggerPath must not be null");
        if(Files.notExists(swaggerPath)){
            throw new IllegalArgumentException(String.format("swaggerPath does not exist: %s", swaggerPath));
        }
        if(Files.isReadable(swaggerPath)){
            throw new IllegalArgumentException(String.format("swaggerPath must be readable: %s", swaggerPath));
        }
        try {
            if(Files.isHidden(swaggerPath)){
                throw new IllegalArgumentException("swaggerPath must not be a hidden file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to check if swaggerPath is a hidden file");
        }
        return new Builder(swaggerPath);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger model.
     *
     * @param swagger the Swagger source.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Swagger swagger) {
        Validate.notNull(swagger, "swagger must not be null");
        return new Builder(swagger);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON String.
     *
     * @param swaggerString the Swagger YAML or JSON String.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(String swaggerString) {
        Validate.notEmpty(swaggerString, "swaggerString must not be null");
        return from(new StringReader(swaggerString));
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON reader.
     *
     * @param swaggerReader the Swagger YAML or JSON reader.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Reader swaggerReader) {
        Validate.notNull(swaggerReader, "swaggerReader must not be null");
        Swagger swagger;
        try {
            swagger = new SwaggerParser().parse(IOUtils.toString(swaggerReader));
        } catch (IOException e) {
            throw new RuntimeException("Swagger source can not be parsed", e);
        }
        if (swagger == null)
            throw new IllegalArgumentException("Swagger source is in a wrong format");

        return new Builder(swagger);
    }

    /**
     * Converts the Swagger specification into the given {@code outputDirectory}.
     *
     * @param outputDirectory the output directory path
     */
    public void toFolder(Path outputDirectory){
        Validate.notNull(outputDirectory, "outputDirectory must not be null");
        
        new OverviewDocumentBuilder(context, extensionRegistry, outputDirectory).build().writeToFile(outputDirectory.resolve(context.config.getOverviewDocument()), StandardCharsets.UTF_8);
        new PathsDocumentBuilder(context, extensionRegistry, outputDirectory).build().writeToFile(outputDirectory.resolve(context.config.getPathsDocument()), StandardCharsets.UTF_8);
        new DefinitionsDocumentBuilder(context, extensionRegistry, outputDirectory).build().writeToFile(outputDirectory.resolve(context.config.getDefinitionsDocument()), StandardCharsets.UTF_8);
        new SecurityDocumentBuilder(context, extensionRegistry, outputDirectory).build().writeToFile(outputDirectory.resolve(context.config.getSecurityDocument()), StandardCharsets.UTF_8);
    }

    /**
     * Converts the Swagger specification into the {@code outputPath} which can be either a directory (e.g /tmp) or a file without extension (e.g /tmp/swagger).
     * Internally the method invokes either {@code toFolder} or {@code toFile}. If the {@code outputPath} is a directory, the directory must exist.
     * Otherwise it cannot be determined if the {@code outputPath} is a directory or not.
     *
     * @param outputPath the output path
     */
    public void toPath(Path outputPath) {
        Validate.notNull(outputPath, "outputPath must not be null");
        if (Files.isDirectory(outputPath)) {
            toFolder(outputPath);
        } else {
            toFile(outputPath);
        }
    }

    /**
     * Converts the Swagger specification the given {@code outputFile}.<br>
     * An extension identifying the markup language will be automatically added to file name.
     *
     * @param outputFile the output file
     */
    public void toFile(Path outputFile) {
        Validate.notNull(outputFile, "outputFile must not be null");

        new OverviewDocumentBuilder(context,extensionRegistry,  null).build().writeToFile(outputFile, StandardCharsets.UTF_8);
        new PathsDocumentBuilder(context, extensionRegistry, null).build().writeToFile(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        new DefinitionsDocumentBuilder(context, extensionRegistry, null).build().writeToFile(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        new SecurityDocumentBuilder(context, extensionRegistry, null).build().writeToFile(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    /**
     * Converts the Swagger specification the given {@code outputFile}.
     *
     * @param outputFile the output file
     */
    public void toFileWithoutExtension(Path outputFile){
        Validate.notNull(outputFile, "outputFile must not be null");

        new OverviewDocumentBuilder(context, extensionRegistry, null).build().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8);
        new PathsDocumentBuilder(context, extensionRegistry, null).build().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        new DefinitionsDocumentBuilder(context, extensionRegistry, null).build().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        new SecurityDocumentBuilder(context, extensionRegistry, null).build().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    /**
     * Builds the document returns it as a String.
     *
     * @return the document as a String
     */
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(new OverviewDocumentBuilder(context,extensionRegistry,  null).build().toString());
        sb.append(new PathsDocumentBuilder(context, extensionRegistry, null).build().toString());
        sb.append(new DefinitionsDocumentBuilder(context, extensionRegistry, null).build().toString());
        sb.append(new SecurityDocumentBuilder(context, extensionRegistry, null).build().toString());
        return sb.toString();
    }

    public static class Builder {
        private final Swagger swagger;
        private final URI swaggerLocation;
        private Swagger2MarkupConfig config;
        private Swagger2MarkupExtensionRegistry extensionRegistry;

        /**
         * Creates a Builder from a remote URL.
         *
         * @param swaggerUrl the remote URL
         */
        Builder(URL swaggerUrl) {
            try {
                this.swaggerLocation = swaggerUrl.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("swaggerURL is in a wrong format", e);
            }
            this.swagger = readSwagger(swaggerUrl.toString());
        }

        /**
         * Creates a Builder from a local Path.
         *
         * @param swaggerPath the local Path
         */
        Builder(Path swaggerPath) {
            this.swaggerLocation = swaggerPath.toAbsolutePath().toUri();
            this.swagger = readSwagger(swaggerPath.toString());
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
         * Uses the SwaggerParser to read the Swagger source.
         *
         * @param swaggerLocation the location of the Swagger source
         * @return the Swagger model
         */
        private Swagger readSwagger(String swaggerLocation){
            Swagger swagger = new SwaggerParser().read(swaggerLocation);
            if (swagger == null) {
                throw new IllegalArgumentException("Failed to read the Swagger source");
            }
            return swagger;
        }

        public Builder withConfig(Swagger2MarkupConfig config) {
            Validate.notNull(config, "config must not be null");
            this.config = config;
            return this;
        }

        public Builder withExtensionRegistry(Swagger2MarkupExtensionRegistry registry) {
            Validate.notNull(registry, "registry must not be null");
            this.extensionRegistry = registry;
            return this;
        }

        public Swagger2MarkupConverter build() {
            if (config == null)
                config = new Swagger2MarkupConfigBuilder().build();

            if (extensionRegistry == null)
                extensionRegistry = new Swagger2MarkupExtensionRegistryBuilder().build();

            Context context = new Context(config, swagger, swaggerLocation);

            initExtensions(context);

            applySwaggerExtensions(context);

            return new Swagger2MarkupConverter(context, extensionRegistry);
        }

        private void initExtensions(Context context) {
            for (SwaggerModelExtension extension : extensionRegistry.getSwaggerModelExtensions())
                extension.setGlobalContext(context);

            for (OverviewDocumentExtension extension : extensionRegistry.getOverviewDocumentExtensions())
                extension.setGlobalContext(context);

            for (DefinitionsDocumentExtension extension : extensionRegistry.getDefinitionsDocumentExtensions())
                extension.setGlobalContext(context);

            for (PathsDocumentExtension extension : extensionRegistry.getPathsDocumentExtensions())
                extension.setGlobalContext(context);

            for (SecurityDocumentExtension extension : extensionRegistry.getSecurityDocumentExtensions())
                extension.setGlobalContext(context);
        }

        private void applySwaggerExtensions(Context context) {
            for (SwaggerModelExtension swaggerModelExtension : extensionRegistry.getSwaggerModelExtensions()) {
                swaggerModelExtension.apply(context.getSwagger());
            }
        }
    }

    public static class Context {
        private Swagger2MarkupConfig config;
        private Swagger swagger;
        private URI swaggerLocation;

        Context(Swagger2MarkupConfig config, Swagger swagger, URI swaggerLocation) {
            this.config = config;
            this.swagger = swagger;
            this.swaggerLocation = swaggerLocation;
        }

        public Swagger2MarkupConfig getConfig() {
            return config;
        }

        public Swagger getSwagger() {
            return swagger;
        }

        public URI getSwaggerLocation() {
            return swaggerLocation;
        }
    }

}
