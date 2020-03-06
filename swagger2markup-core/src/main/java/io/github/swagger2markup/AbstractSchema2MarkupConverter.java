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

import io.github.swagger2markup.config.Labels;
import io.github.swagger2markup.config.Schema2MarkupConfig;
import io.github.swagger2markup.extension.Schema2MarkupExtensionRegistry;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Abstract implementation of Schema2Markup converter
 *
 * @author Balaji V
 */
public abstract class AbstractSchema2MarkupConverter<T> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final Context<T> context;

    public AbstractSchema2MarkupConverter(Context<T> context) {
        this.context = context;
    }

    /**
     * Returns the global Context
     *
     * @return the global Context
     */
    public Context<T> getContext() {
        return context;
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
     * Converts the Swagger specification into the given {@code outputDirectory}.
     *
     * @param outputDirectory the output directory path
     */
    public abstract void toFolder(Path outputDirectory);

    /**
     * Converts the Swagger specification the given {@code outputFile}.<br>
     * An extension identifying the markup language will be automatically added to file name.
     *
     * @param outputFile the output file
     */
    public abstract void toFile(Path outputFile);

    /**
     * Converts the Swagger specification the given {@code outputFile}.
     *
     * @param outputFile the output file
     */
    public abstract void toFileWithoutExtension(Path outputFile);

    /**
     * Builds the document returns it as a String.
     *
     * @return the document as a String
     */
    public abstract String toString();

    public abstract static class Context<T> {
        private final Schema2MarkupConfig config;
        private final T schema;
        private final URI swaggerLocation;
        private final Schema2MarkupExtensionRegistry extensionRegistry;
        private final Labels labels;
        private Path outputPath;

        public Context(Schema2MarkupConfig config,
                       Schema2MarkupExtensionRegistry extensionRegistry,
                       T schema,
                       URI swaggerLocation,
                       Labels labels) {
            this.config = config;
            this.extensionRegistry = extensionRegistry;
            this.schema = schema;
            this.swaggerLocation = swaggerLocation;
            this.labels = labels;
        }

        public Schema2MarkupConfig getConfig() {
            return config;
        }

        public T getSchema() {
            return schema;
        }

        public URI getSwaggerLocation() {
            return swaggerLocation;
        }

        public Schema2MarkupExtensionRegistry getExtensionRegistry() {
            return extensionRegistry;
        }

        public Labels getLabels() {
            return labels;
        }

        public Path getOutputPath() {
            return outputPath;
        }

        public void setOutputPath(Path outputPath) {
            this.outputPath = outputPath;
        }
    }

}
