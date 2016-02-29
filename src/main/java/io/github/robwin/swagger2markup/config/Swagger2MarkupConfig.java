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
package io.github.robwin.swagger2markup.config;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.*;
import io.github.robwin.swagger2markup.utils.IOUtils;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;

public class Swagger2MarkupConfig {

    private static final Logger logger = LoggerFactory.getLogger(Swagger2MarkupConfig.class);

    private MarkupLanguage markupLanguage;
    private boolean examplesEnabled;
    private URI examplesUri;
    private boolean schemasEnabled;
    private URI schemasUri;
    private boolean operationDescriptionsEnabled;
    private URI operationDescriptionsUri;
    private boolean definitionDescriptionsEnabled;
    private URI definitionDescriptionsUri;
    private boolean separatedDefinitionsEnabled;
    private boolean separatedOperationsEnabled;
    private GroupBy operationsGroupedBy;
    @Deprecated
    private OrderBy definitionsOrderedBy;
    private Language outputLanguage;
    private int inlineSchemaDepthLevel;
    private Comparator<String> tagOrdering;
    private Comparator<PathOperation> operationOrdering;
    private Comparator<String> definitionOrdering;
    private Comparator<Parameter> parameterOrdering;
    private Comparator<String> propertyOrdering;
    private Comparator<String> responseOrdering;
    private boolean interDocumentCrossReferencesEnabled;
    private String interDocumentCrossReferencesPrefix;
    private boolean flatBodyEnabled;
    private String anchorPrefix;

    private String overviewDocument;
    private String pathsDocument;
    private String definitionsDocument;
    private String securityDocument;
    private String separatedOperationsFolder;
    private String separatedDefinitionsFolder;

    public static Builder ofDefaults() {
        return new Builder();
    }

    public static Builder ofProperties(Properties properties) {
        return new Builder(properties);
    }

    /**
     * Global context lazy initialization
     *
     * @param globalContext Partially initialized global context (globalContext.extensionRegistry == null)
     */
    public void setGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        configureDefaultContentPaths(globalContext.swaggerLocation);
    }

    /**
     * Automatically set default path for external content files based on specified {@code swaggerLocation}.<br/>
     * If {@code swaggerLocation} is null, default path can't be set and features are disabled.<br/>
     * Paths have to be explicitly set when swaggerLocation.scheme != 'file' to limit the number of URL requests.
     *
     * @param swaggerLocation base path to set default paths
     * @throws RuntimeException if basePath == null and any path is not configured
     */
    private void configureDefaultContentPaths(URI swaggerLocation) {
        URI baseURI = null;

        if (swaggerLocation != null) {
            if (swaggerLocation.getScheme().equals("file"))
                baseURI = IOUtils.uriParent(swaggerLocation);
        }

        if (examplesEnabled && examplesUri == null) {
            if (baseURI == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable {} > No explicit '{}' set and no default available", "examplesEnabled", "examplesUri");
                examplesEnabled = false;
            } else
                examplesUri = baseURI;
        }

        if (schemasEnabled && schemasUri == null) {
            if (baseURI == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable {} > No explicit '{}' set and no default available > Disable {}", "schemas", "schemasUri");
                schemasEnabled = false;
            } else
                schemasUri = baseURI;
        }

        if (operationDescriptionsEnabled && operationDescriptionsUri == null) {
            if (baseURI == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable {} > No explicit '{}' set and no default available > Disable {}", "operationDescriptionsEnabled", "operationDescriptionsUri");
                operationDescriptionsEnabled = false;
            } else
                operationDescriptionsUri = baseURI;
        }

        if (definitionDescriptionsEnabled && definitionDescriptionsUri == null) {
            if (baseURI == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable {} > No explicit '{}' set and no default available > Disable {}", "definitionDescriptionsEnabled", "definitionDescriptionsUri");
                definitionDescriptionsEnabled = false;
            } else
                definitionDescriptionsUri = baseURI;
        }
    }

    public MarkupLanguage getMarkupLanguage() {
        return markupLanguage;
    }

    public boolean isExamplesEnabled() {
        return examplesEnabled;
    }

    public URI getExamplesUri() {
        return examplesUri;
    }

    public boolean isSchemasEnabled() {
        return schemasEnabled;
    }

    public URI getSchemasUri() {
        return schemasUri;
    }

    public boolean isOperationDescriptionsEnabled() {
        return operationDescriptionsEnabled;
    }

    public URI getOperationDescriptionsUri() {
        return operationDescriptionsUri;
    }

    public boolean isDefinitionDescriptionsEnabled() {
        return definitionDescriptionsEnabled;
    }

    public URI getDefinitionDescriptionsUri() {
        return definitionDescriptionsUri;
    }

    public boolean isSeparatedDefinitionsEnabled() {
        return separatedDefinitionsEnabled;
    }

    public boolean isSeparatedOperationsEnabled() {
        return separatedOperationsEnabled;
    }

    public GroupBy getOperationsGroupedBy() {
        return operationsGroupedBy;
    }

    public OrderBy getDefinitionsOrderedBy() {
        return definitionsOrderedBy;
    }

    public Language getOutputLanguage() {
        return outputLanguage;
    }

    public int getInlineSchemaDepthLevel() {
        return inlineSchemaDepthLevel;
    }

    public Comparator<String> getTagOrdering() {
        return tagOrdering;
    }

    public Comparator<PathOperation> getOperationOrdering() {
        return operationOrdering;
    }

    public Comparator<String> getDefinitionOrdering() {
        return definitionOrdering;
    }

    public Comparator<Parameter> getParameterOrdering() {
        return parameterOrdering;
    }

    public Comparator<String> getPropertyOrdering() {
        return propertyOrdering;
    }

    public Comparator<String> getResponseOrdering() {
        return responseOrdering;
    }

    public boolean isInterDocumentCrossReferencesEnabled() {
        return interDocumentCrossReferencesEnabled;
    }

    public String getInterDocumentCrossReferencesPrefix() {
        return interDocumentCrossReferencesPrefix;
    }

    public boolean isFlatBodyEnabled() {
        return flatBodyEnabled;
    }

    public String getAnchorPrefix() {
        return anchorPrefix;
    }

    public String getOverviewDocument() {
        return overviewDocument;
    }

    public String getPathsDocument() {
        return pathsDocument;
    }

    public String getDefinitionsDocument() {
        return definitionsDocument;
    }

    public String getSecurityDocument() {
        return securityDocument;
    }

    public String getSeparatedOperationsFolder() {
        return separatedOperationsFolder;
    }

    public String getSeparatedDefinitionsFolder() {
        return separatedDefinitionsFolder;
    }

    public static class Builder {

        private static final String PROPERTIES_PREFIX = "swagger2markup.";
        private static final String PROPERTIES_DEFAULT = "/io/github/robwin/swagger2markup/config/default.properties";

        static final Ordering<PathOperation> OPERATION_METHOD_COMPARATOR = Ordering
                .explicit(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.HEAD, HttpMethod.OPTIONS)
                .onResultOf(new Function<PathOperation, HttpMethod>() {
                    public HttpMethod apply(PathOperation operation) {
                        return operation.getMethod();
                    }
                });

        static final Ordering<PathOperation> OPERATION_PATH_COMPARATOR = Ordering
                .natural()
                .onResultOf(new Function<PathOperation, String>() {
                    public String apply(PathOperation operation) {
                        return operation.getPath();
                    }
                });

        static final Ordering<Parameter> PARAMETER_IN_COMPARATOR = Ordering
                .explicit("header", "path", "query", "formData", "body")
                .onResultOf(new Function<Parameter, String>() {
                    public String apply(Parameter parameter) {
                        return parameter.getIn();
                    }
                });

        static final Ordering<Parameter> PARAMETER_NAME_COMPARATOR = Ordering
                .natural()
                .onResultOf(new Function<Parameter, String>() {
                    public String apply(Parameter parameter) {
                        return parameter.getName();
                    }
                });

        private Swagger2MarkupConfig config = new Swagger2MarkupConfig();

        Builder() {
            this(new Properties());
        }

        Builder(Properties properties) {

            Properties safeProperties = new Properties(defaultProperties());
            safeProperties.putAll(properties);

            config.markupLanguage = MarkupLanguage.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "markupLanguage"));
            config.examplesEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "examplesEnabled"));
            if (safeProperties.containsKey(PROPERTIES_PREFIX + "examplesUri"))
                config.examplesUri = URI.create(safeProperties.getProperty(PROPERTIES_PREFIX + "examplesUri"));
            config.schemasEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "schemasEnabled"));
            if (safeProperties.containsKey(PROPERTIES_PREFIX + "schemasUri"))
                config.schemasUri = URI.create(safeProperties.getProperty(PROPERTIES_PREFIX + "schemasUri"));
            config.operationDescriptionsEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "operationDescriptionsEnabled"));
            if (safeProperties.containsKey(PROPERTIES_PREFIX + "operationDescriptionsUri"))
                config.operationDescriptionsUri = URI.create(safeProperties.getProperty(PROPERTIES_PREFIX + "operationDescriptionsUri"));
            config.definitionDescriptionsEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "definitionDescriptionsEnabled"));
            if (safeProperties.containsKey(PROPERTIES_PREFIX + "definitionDescriptionsUri"))
                config.definitionDescriptionsUri = URI.create(safeProperties.getProperty(PROPERTIES_PREFIX + "definitionDescriptionsUri"));
            config.separatedDefinitionsEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "separatedDefinitionsEnabled"));
            config.separatedOperationsEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "separatedOperationsEnabled"));
            config.operationsGroupedBy = GroupBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "operationsGroupedBy"));
            config.definitionsOrderedBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "definitionsOrderedBy"));
            config.outputLanguage = Language.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "outputLanguage"));
            config.inlineSchemaDepthLevel = Integer.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "inlineSchemaDepthLevel"));
            config.interDocumentCrossReferencesEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "interDocumentCrossReferencesEnabled"));
            config.interDocumentCrossReferencesPrefix = safeProperties.getProperty(PROPERTIES_PREFIX + "interDocumentCrossReferencesPrefix");
            config.flatBodyEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "flatBodyEnabled"));
            config.anchorPrefix = safeProperties.getProperty(PROPERTIES_PREFIX + "anchorPrefix");
            config.overviewDocument = safeProperties.getProperty(PROPERTIES_PREFIX + "overviewDocument");
            config.pathsDocument = safeProperties.getProperty(PROPERTIES_PREFIX + "pathsDocument");
            config.definitionsDocument = safeProperties.getProperty(PROPERTIES_PREFIX + "definitionsDocument");
            config.securityDocument = safeProperties.getProperty(PROPERTIES_PREFIX + "securityDocument");
            config.separatedOperationsFolder = safeProperties.getProperty(PROPERTIES_PREFIX + "separatedOperationsFolder");
            config.separatedDefinitionsFolder = safeProperties.getProperty(PROPERTIES_PREFIX + "separatedDefinitionsFolder");

            config.tagOrdering = Ordering.natural();
            config.operationOrdering = OPERATION_PATH_COMPARATOR.compound(OPERATION_METHOD_COMPARATOR);
            config.definitionOrdering = Ordering.natural();
            config.parameterOrdering = PARAMETER_IN_COMPARATOR.compound(PARAMETER_NAME_COMPARATOR);
            config.propertyOrdering = Ordering.natural();
            config.responseOrdering = Ordering.natural();
        }

        private Properties defaultProperties() {
            Properties defaultProperties = new Properties();
            try {
                InputStream defaultPropertiesStream = Swagger2MarkupConfig.class.getResourceAsStream(PROPERTIES_DEFAULT);
                if (defaultPropertiesStream == null)
                    throw new RuntimeException(String.format("Can't load default properties '%s'", PROPERTIES_DEFAULT));
                defaultProperties.load(defaultPropertiesStream);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Can't load default properties '%s'", PROPERTIES_DEFAULT), e);
            }

            return defaultProperties;
        }

        public Swagger2MarkupConfig build() {
            return config;
        }

        /**
         * Specifies the markup language which should be used to generate the files
         *
         * @param markupLanguage the markup language which is used to generate the files
         * @return this builder
         */
        public Builder withMarkupLanguage(MarkupLanguage markupLanguage) {
            config.markupLanguage = markupLanguage;
            return this;
        }

        /**
         * Include examples into the Paths document
         *
         * @param examplesUri the URI to the folder where the example documents reside. Use default URI if null.
         * @return this builder
         */
        public Builder withExamples(URI examplesUri) {
            config.examplesEnabled = true;

            config.examplesUri = examplesUri;
            return this;
        }

        /**
         * Include examples into the Paths document
         *
         * @param examplesPath the path to the folder where the example documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withExamples(Path examplesPath) {
            return withExamples(examplesPath.toUri());
        }

        /**
         * Include examples into the Paths document.<br/>
         * This is an alias for {@link #withExamples(URI) withExamples(null)}.
         *
         * @return this builder
         */
        public Builder withExamples() {
            withExamples((URI) null);
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document
         *
         * @param schemasUri the URI to the folder where the schema documents reside. Use default URI if null.
         * @return this builder
         */
        public Builder withSchemas(URI schemasUri) {
            config.schemasEnabled = true;
            config.schemasUri = schemasUri;
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document
         *
         * @param schemasPath the path to the folder where the schema documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withSchemas(Path schemasPath) {
            return withSchemas(schemasPath.toUri());
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document.<br/>
         * This is an alias for {@link #withSchemas(URI) withSchemas(null)}.
         *
         * @return this builder
         */
        public Builder withSchemas() {
            withSchemas((URI) null);
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths document
         *
         * @param operationDescriptionsUri the URI to the folder where the description documents reside. Use default URI if null.
         * @return this builder
         */
        public Builder withOperationDescriptions(URI operationDescriptionsUri) {
            config.operationDescriptionsEnabled = true;
            config.operationDescriptionsUri = operationDescriptionsUri;
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths document
         *
         * @param operationDescriptionsPath the path to the folder where the description documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withOperationDescriptions(Path operationDescriptionsPath) {
            return withOperationDescriptions(operationDescriptionsPath.toUri());
        }

        /**
         * Include hand-written descriptions into the Paths document.<br/>
         * This is an alias for {@link #withOperationDescriptions(URI) withOperationDescriptions(null)}.
         *
         * @return this builder
         */
        public Builder withOperationDescriptions() {
            withOperationDescriptions((URI) null);
            return this;
        }

        /**
         * Include hand-written descriptions into the Definitions document
         *
         * @param definitionDescriptionsUri the URI to the folder where the description documents reside. Use default URI if null.
         * @return this builder
         */
        public Builder withDefinitionDescriptions(URI definitionDescriptionsUri) {
            config.definitionDescriptionsEnabled = true;
            config.definitionDescriptionsUri = definitionDescriptionsUri;
            return this;
        }

        /**
         * Include hand-written descriptions into the Definitions document
         *
         * @param definitionDescriptionsPath the path to the folder where the description documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withDefinitionDescriptions(Path definitionDescriptionsPath) {
            return withDefinitionDescriptions(definitionDescriptionsPath.toUri());
        }

        /**
         * Include hand-written descriptions into the Definitions document.<br/>
         * This is an alias for {@link #withDefinitionDescriptions(URI) withDefinitionDescriptions(null)}.
         *
         * @return this builder
         */
        public Builder withDefinitionDescriptions() {
            withDefinitionDescriptions((URI) null);
            return this;
        }

        /**
         * In addition to the definitions file, also create separate definition files for each model definition.
         *
         * @return this builder
         */
        public Builder withSeparatedDefinitions() {
            config.separatedDefinitionsEnabled = true;
            return this;
        }


        /**
         * In addition to the paths file, also create separate path files for each path.
         *
         * @return this builder
         */
        public Builder withSeparatedOperations() {
            config.separatedOperationsEnabled = true;
            return this;
        }


        /**
         * Specifies if the paths should be grouped by tags or stay as-is.
         *
         * @param pathsGroupedBy the GroupBy enum
         * @return this builder
         */
        public Builder withPathsGroupedBy(GroupBy pathsGroupedBy) {
            config.operationsGroupedBy = pathsGroupedBy;
            return this;
        }

        /**
         * Specifies if the definitions should be ordered by natural ordering or stay as-is.<br/>
         * Use {@link #withDefinitionOrdering(Comparator)} instead.
         *
         * @param definitionsOrderedBy the OrderBy enum
         * @return this builder
         */
        @Deprecated
        public Builder withDefinitionsOrderedBy(OrderBy definitionsOrderedBy) {
            config.definitionsOrderedBy = definitionsOrderedBy;
            config.definitionOrdering = Ordering.natural();
            return this;
        }


        /**
         * Specifies labels language of output files
         *
         * @param language the enum
         * @return this builder
         */
        public Builder withOutputLanguage(Language language) {
            config.outputLanguage = language;
            return this;
        }


        /**
         * Specifies maximum depth level for inline object schema displaying (0 = no inline schemasEnabled)
         *
         * @param inlineSchemaDepthLevel number of recursion levels for inline schemasEnabled display
         * @return this builder
         */
        public Builder withInlineSchemaDepthLevel(int inlineSchemaDepthLevel) {
            config.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
            return this;
        }


        /**
         * Specifies a custom comparator function to order tags.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param tagOrdering tag comparator
         * @return this builder
         */
        public Builder withTagOrdering(Comparator<String> tagOrdering) {
            config.tagOrdering = tagOrdering;
            return this;
        }


        /**
         * Specifies a custom comparator function to order operations.
         * By default, natural ordering is applied on operation 'path', then explicit ordering is applied on operation 'method'
         * Set ordering to null to keep swagger original order
         *
         * @param operationOrdering operation comparator
         * @return this builder
         */
        public Builder withOperationOrdering(Comparator<PathOperation> operationOrdering) {
            config.operationOrdering = operationOrdering;
            return this;
        }


        /**
         * Specifies a custom comparator function to order definitions.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param definitionOrdering definition comparator
         * @return this builder
         */
        public Builder withDefinitionOrdering(Comparator<String> definitionOrdering) {
            config.definitionOrdering = definitionOrdering;
            return this;
        }


        /**
         * Specifies a custom comparator function to order parameters.
         * By default, explicit ordering is applied on parameter 'in', then natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param parameterOrdering parameter comparator
         * @return this builder
         */
        public Builder withParameterOrdering(Comparator<Parameter> parameterOrdering) {
            config.parameterOrdering = parameterOrdering;
            return this;
        }


        /**
         * Specifies a custom comparator function to order properties.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param propertyOrdering property comparator
         * @return this builder
         */
        public Builder withPropertyOrdering(Comparator<String> propertyOrdering) {
            config.propertyOrdering = propertyOrdering;
            return this;
        }


        /**
         * Specifies a custom comparator function to order responses.
         * By default, natural ordering is applied.
         * Set ordering to null to keep swagger original order
         *
         * @param responseOrdering response comparator
         * @return this builder
         */
        public Builder withResponseOrdering(Comparator<String> responseOrdering) {
            config.responseOrdering = responseOrdering;
            return this;
        }

        /**
         * Enable use of inter-document cross-references when needed
         *
         * @param prefix Prefix to document in all inter-document cross-references (null = no prefix)
         * @return this builder
         */
        public Builder withInterDocumentCrossReferences(String prefix) {
            config.interDocumentCrossReferencesEnabled = true;
            config.interDocumentCrossReferencesPrefix = prefix;
            return this;
        }

        /**
         * Enable use of inter-document cross-references when needed.<br/>
         * This is an alias for {@link #withInterDocumentCrossReferences(String) withInterDocumentCrossReferences(null)}.
         *
         * @return this builder
         */
        public Builder withInterDocumentCrossReferences() {
            withInterDocumentCrossReferences(null);
            return this;
        }

        /**
         * Optionally isolate the body parameter, if any, from other parameters
         *
         * @return this builder
         */
        public Builder withFlatBody() {
            config.flatBodyEnabled = true;
            return this;
        }

        /**
         * Optionally prefix all anchors for unicity
         *
         * @param anchorPrefix anchor prefix (null = no prefix)
         * @return this builder
         */
        public Builder withAnchorPrefix(String anchorPrefix) {
            config.anchorPrefix = anchorPrefix;
            return this;
        }

    }

}