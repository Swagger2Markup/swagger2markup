/*
 * Copyright 2017 Robert Winkler
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

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.github.swagger2markup.config.OpenAPILabels;
import io.github.swagger2markup.config.builder.OpenAPI2MarkupConfigBuilder;
import io.github.swagger2markup.extension.OpenAPI2MarkupExtensionRegistry;
import io.github.swagger2markup.extension.builder.OpenAPI2MarkupExtensionRegistryBuilder;
import io.github.swagger2markup.internal.document.ComponentsDocument;
import io.github.swagger2markup.internal.document.OverviewDocument;
import io.github.swagger2markup.internal.document.PathsDocument;
import io.github.swagger2markup.internal.document.SecurityDocument;
import io.github.swagger2markup.utils.URIUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;

import java.io.BufferedWriter;
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

public class OpenAPI2MarkupConverter extends AbstractSchema2MarkupConverter<OpenAPI> {
    private final OverviewDocument overviewDocument;
    private final PathsDocument pathsDocument;
    private final ComponentsDocument componentsDocument;
    private final SecurityDocument securityDocument;
    private final OpenAPIContext openAPIContext;


    public OpenAPI2MarkupConverter(OpenAPIContext context) {
        super(context);
        this.openAPIContext = context;
        this.overviewDocument = new OverviewDocument(context);
        this.pathsDocument = new PathsDocument(context);
        this.componentsDocument = new ComponentsDocument(context);
        this.securityDocument = new SecurityDocument(context);
    }


    /**
     * Creates a OpenAPI2MarkupConverter.Builder from a URI.
     *
     * @param swaggerUri the URI
     * @return a OpenAPI2MarkupConverter
     */
    public static Builder from(URI swaggerUri) {
        Validate.notNull(swaggerUri, "swaggerUri must not be null");
        String scheme = swaggerUri.getScheme();
        if (scheme != null && swaggerUri.getScheme().startsWith("http")) {
            try {
                return from(swaggerUri.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to convert URI to URL", e);
            }
        } else if (scheme != null && swaggerUri.getScheme().startsWith("file")) {
            return from(Paths.get(swaggerUri));
        } else {
            return from(URIUtils.convertUriWithoutSchemeToFileScheme(swaggerUri));
        }
    }

    /**
     * Creates a OpenAPI2MarkupConverter.Builder using a remote URL.
     *
     * @param swaggerURL the remote URL
     * @return a OpenAPI2MarkupConverter
     */
    public static Builder from(URL swaggerURL) {
        Validate.notNull(swaggerURL, "swaggerURL must not be null");
        return new Builder(swaggerURL);
    }

    /**
     * Creates a OpenAPI2MarkupConverter.Builder using a local Path.
     *
     * @param swaggerPath the local Path
     * @return a OpenAPI2MarkupConverter
     */
    public static Builder from(Path swaggerPath) {
        Validate.notNull(swaggerPath, "swaggerPath must not be null");
        if (Files.notExists(swaggerPath)) {
            throw new IllegalArgumentException(String.format("swaggerPath does not exist: %s", swaggerPath));
        }
        try {
            if (Files.isHidden(swaggerPath)) {
                throw new IllegalArgumentException("swaggerPath must not be a hidden file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to check if swaggerPath is a hidden file", e);
        }
        return new Builder(swaggerPath);
    }

    /**
     * Creates a OpenAPI2MarkupConverter.Builder from a given Swagger model.
     *
     * @param openAPI the Swagger source.
     * @return a OpenAPI2MarkupConverter
     */
    public static Builder from(OpenAPI openAPI) {
        Validate.notNull(openAPI, "schema must not be null");
        return new Builder(openAPI);
    }

    /**
     * Creates a OpenAPI2MarkupConverter.Builder from a given Swagger YAML or JSON String.
     *
     * @param swaggerString the Swagger YAML or JSON String.
     * @return a OpenAPI2MarkupConverter
     */
    public static Builder from(String swaggerString) {
        Validate.notEmpty(swaggerString, "swaggerString must not be null");
        return from(new StringReader(swaggerString));
    }

    /**
     * Creates a OpenAPI2MarkupConverter.Builder from a given Swagger YAML or JSON reader.
     *
     * @param schemaReader the schema YAML or JSON reader.
     * @return a OpenAPI2MarkupConverter
     */
    public static Builder from(Reader schemaReader) {
        Validate.notNull(schemaReader, "swaggerReader must not be null");
        OpenAPI openAPI;
        try {
            //TODO
            openAPI = new OpenAPIV3Parser().read(IOUtils.toString(schemaReader));
        } catch (IOException e) {
            throw new RuntimeException("Swagger source can not be parsed", e);
        }
        if (openAPI == null)
            throw new IllegalArgumentException("Swagger source is in a wrong format");

        return new Builder(openAPI);
    }


    @Override
    public void toFolder(Path outputDirectory) {
        Validate.notNull(outputDirectory, "outputDirectory must not be null");
        openAPIContext.setOutputPath(outputDirectory);
        writeToFile(applyOverviewDocument(), outputDirectory.resolve(openAPIContext.config.getOverviewDocument()));
        writeToFile(applyPathsDocument(), outputDirectory.resolve(openAPIContext.config.getPathsDocument()));
        writeToFile(applyComponentsDocument(), outputDirectory.resolve(openAPIContext.config.getDefinitionsDocument()));
        writeToFile(applySecurityDocument(), outputDirectory.resolve(openAPIContext.config.getSecurityDocument()));
    }

    @Override
    public void toFile(Path outputFile) {
        Validate.notNull(outputFile, "outputFile must not be null");

        writeToFile(applyOverviewDocument(), outputFile);
        writeToFile(applyPathsDocument(), outputFile);
        writeToFile(applyComponentsDocument(), outputFile);
        writeToFile(applySecurityDocument(), outputFile);
    }

    @Override
    public void toFileWithoutExtension(Path outputFile) {
        Validate.notNull(outputFile, "outputFile must not be null");

        writeToFileWithoutExtension(applyOverviewDocument(), outputFile);
        writeToFileWithoutExtension(applyPathsDocument(), outputFile);
        writeToFileWithoutExtension(applyComponentsDocument(), outputFile);
        writeToFileWithoutExtension(applySecurityDocument(), outputFile);
    }

    @Override
    public String toString() {
        return applyOverviewDocument().convert() +
                applyPathsDocument().convert() +
                applyComponentsDocument().convert() +
                applySecurityDocument().convert();
    }

    private Document applyOverviewDocument() {
        return overviewDocument.apply(
                openAPIContext.createDocument(),
                OverviewDocument.parameters(openAPIContext.getSchema()));
    }

    private Document applyPathsDocument() {
        return pathsDocument.apply(
                openAPIContext.createDocument(),
                PathsDocument.parameters(openAPIContext.getSchema()));
    }

    private Document applyComponentsDocument() {
        return componentsDocument.apply(
                openAPIContext.createDocument(),
                ComponentsDocument.parameters(openAPIContext.getSchema().getComponents()));
    }

    private Document applySecurityDocument() {
        return securityDocument.apply(
                openAPIContext.createDocument(),
                SecurityDocument.parameters(openAPIContext.getSchema()));
    }

    private void writeToFile(Document document, Path path) {
        MarkupLanguage markupLanguage = openAPIContext.config.getMarkupLanguage();
        if (isMarkupLanguageSupported(markupLanguage)) {
            String fileExtension = markupLanguage.getFileNameExtensions().get(0);
            writeToFileWithoutExtension(document, path.resolveSibling(path.getFileName().toString() + fileExtension));
        } else {
            throw new RuntimeException("Given Markup language '"+markupLanguage+"' is not supported by "+getClass().getName());
        }
    }

    private boolean isMarkupLanguageSupported(MarkupLanguage markupLanguage) {
        return markupLanguage == MarkupLanguage.ASCIIDOC;
    }

    private void writeToFileWithoutExtension(Document document, Path file) {
        if (file.getParent() != null) {
            try {
                Files.createDirectories(file.getParent());
            } catch (IOException e) {
                throw new RuntimeException("Failed create directory", e);
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(document.convert());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Markup document written to: {}", file);
        }
    }

    public static class OpenAPIContext extends Context<OpenAPI> {
        private OpenSchema2MarkupConfig config;
        private OpenAPI2MarkupExtensionRegistry extensionRegistry;

        public OpenAPIContext(OpenSchema2MarkupConfig config,
                              OpenAPI2MarkupExtensionRegistry extensionRegistry,
                              OpenAPI schema, URI swaggerLocation, Labels labels) {
            super(config, extensionRegistry, schema, swaggerLocation, labels);
            this.config = config;
            this.extensionRegistry = extensionRegistry;
        }

        @Override
        public OpenSchema2MarkupConfig getConfig() {
            return config;
        }

        @Override
        public OpenAPI2MarkupExtensionRegistry getExtensionRegistry() {
            return extensionRegistry;
        }

        public Document createDocument() {
            return new DocumentImpl();
        }
    }

    public static class Builder {
        private final OpenAPI openAPI;
        private final URI schemaLocation;
        private OpenSchema2MarkupConfig config;
        private OpenAPI2MarkupExtensionRegistry extensionRegistry;

        /**
         * Creates a Builder from a remote URL.
         *
         * @param schemaUrl the remote URL
         */
        Builder(URL schemaUrl) {
            try {
                this.schemaLocation = schemaUrl.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("swaggerURL is in a wrong format", e);
            }
            this.openAPI = readSchema(schemaUrl.toString());
        }

        /**
         * Creates a Builder from a local Path.
         *
         * @param swaggerPath the local Path
         */
        Builder(Path swaggerPath) {
            this.schemaLocation = swaggerPath.toAbsolutePath().toUri();
            this.openAPI = readSchema(swaggerPath.toString());
        }

        /**
         * Creates a Builder using a given Swagger model.
         *
         * @param openAPI the Swagger source.
         */
        Builder(OpenAPI openAPI) {
            this.openAPI = openAPI;
            this.schemaLocation = null;
        }

        /**
         * Uses the SwaggerParser to read the Swagger source.
         *
         * @param schemaLocation the location of the Swagger source
         * @return the Swagger model
         */
        private OpenAPI readSchema(String schemaLocation) {
            OpenAPI openAPI = new OpenAPIV3Parser().read(schemaLocation);
            if (openAPI == null) {
                throw new IllegalArgumentException("Failed to read the schema");
            }
            return openAPI;
        }

        public Builder withConfig(OpenSchema2MarkupConfig config) {
            Validate.notNull(config, "config must not be null");
            this.config = config;
            return this;
        }

        public Builder withExtensionRegistry(OpenAPI2MarkupExtensionRegistry registry) {
            Validate.notNull(registry, "registry must not be null");
            this.extensionRegistry = registry;
            return this;
        }

        public OpenAPI2MarkupConverter build() {
            if (config == null)
                config = new OpenAPI2MarkupConfigBuilder().build();

            if (extensionRegistry == null)
                extensionRegistry = new OpenAPI2MarkupExtensionRegistryBuilder().build();
            OpenAPILabels openApiLabels = new OpenAPILabels(config);
            OpenAPIContext context = new OpenAPIContext(config, extensionRegistry, openAPI, schemaLocation, openApiLabels);

            initExtensions(context);

            applySwaggerExtensions(context);

            return new OpenAPI2MarkupConverter(context);
        }

        private void initExtensions(OpenAPIContext context) {
            extensionRegistry.getSwaggerModelExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getOverviewDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getDefinitionsDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getSecurityDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
        }

        private void applySwaggerExtensions(OpenAPIContext context) {
            extensionRegistry.getSwaggerModelExtensions().forEach(extension -> extension.apply(context.getSchema()));
        }
    }
}
