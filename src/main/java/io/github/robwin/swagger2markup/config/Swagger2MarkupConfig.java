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
import org.apache.commons.lang3.Validate;
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
    private boolean generatedExamplesEnabled;
    private boolean schemasEnabled;
    private URI schemasUri;
    private boolean operationDescriptionsEnabled;
    private URI operationDescriptionsUri;
    private boolean definitionDescriptionsEnabled;
    private URI definitionDescriptionsUri;
    private boolean separatedDefinitionsEnabled;
    private boolean separatedOperationsEnabled;
    private GroupBy operationsGroupedBy;
    private Language outputLanguage;
    private int inlineSchemaDepthLevel;
    private OrderBy tagOrderBy;
    private Comparator<String> tagOrdering;
    private OrderBy operationOrderBy;
    private Comparator<PathOperation> operationOrdering;
    private OrderBy definitionOrderBy;
    private Comparator<String> definitionOrdering;
    private OrderBy parameterOrderBy;
    private Comparator<Parameter> parameterOrdering;
    private OrderBy propertyOrderBy;
    private Comparator<String> propertyOrdering;
    private OrderBy responseOrderBy;
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

    public boolean isGeneratedExamplesEnabled() {
        return generatedExamplesEnabled;
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

    public Language getOutputLanguage() {
        return outputLanguage;
    }

    public int getInlineSchemaDepthLevel() {
        return inlineSchemaDepthLevel;
    }

    public OrderBy getTagOrderBy() {
        return tagOrderBy;
    }

    public Comparator<String> getTagOrdering() {
        return tagOrdering;
    }

    public OrderBy getOperationOrderBy() {
        return operationOrderBy;
    }

    public Comparator<PathOperation> getOperationOrdering() {
        return operationOrdering;
    }

    public OrderBy getDefinitionOrderBy() {
        return definitionOrderBy;
    }

    public Comparator<String> getDefinitionOrdering() {
        return definitionOrdering;
    }

    public OrderBy getParameterOrderBy() {
        return parameterOrderBy;
    }

    public Comparator<Parameter> getParameterOrdering() {
        return parameterOrdering;
    }

    public OrderBy getPropertyOrderBy() {
        return propertyOrderBy;
    }

    public Comparator<String> getPropertyOrdering() {
        return propertyOrdering;
    }

    public OrderBy getResponseOrderBy() {
        return responseOrderBy;
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

        static final Ordering<PathOperation> OPERATION_METHOD_NATURAL_ORDERING = Ordering
                .explicit(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.HEAD, HttpMethod.OPTIONS)
                .onResultOf(new Function<PathOperation, HttpMethod>() {
                    public HttpMethod apply(PathOperation operation) {
                        return operation.getMethod();
                    }
                });

        static final Ordering<PathOperation> OPERATION_PATH_NATURAL_ORDERING = Ordering
                .natural()
                .onResultOf(new Function<PathOperation, String>() {
                    public String apply(PathOperation operation) {
                        return operation.getPath();
                    }
                });

        static final Ordering<Parameter> PARAMETER_IN_NATURAL_ORDERING = Ordering
                .explicit("header", "path", "query", "formData", "body")
                .onResultOf(new Function<Parameter, String>() {
                    public String apply(Parameter parameter) {
                        return parameter.getIn();
                    }
                });

        static final Ordering<Parameter> PARAMETER_NAME_NATURAL_ORDERING = Ordering
                .natural()
                .onResultOf(new Function<Parameter, String>() {
                    public String apply(Parameter parameter) {
                        return parameter.getName();
                    }
                });

        Swagger2MarkupConfig config = new Swagger2MarkupConfig();

        Builder() {
            this(new Properties());
        }

        Builder(Properties properties) {

            Properties safeProperties = new Properties(defaultProperties());
            safeProperties.putAll(properties);

            config.markupLanguage = MarkupLanguage.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "markupLanguage"));
            config.generatedExamplesEnabled = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "generatedExamplesEnabled"));
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
            config.tagOrderBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "tagOrderBy"));
            config.operationOrderBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "operationOrderBy"));
            config.definitionOrderBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "definitionOrderBy"));
            config.parameterOrderBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "parameterOrderBy"));
            config.propertyOrderBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "propertyOrderBy"));
            config.responseOrderBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "responseOrderBy"));
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
            buildNaturalOrdering();

            return config;
        }

        private void buildNaturalOrdering() {
            if (config.tagOrderBy == OrderBy.NATURAL)
                config.tagOrdering = Ordering.natural();
            if (config.operationOrderBy == OrderBy.NATURAL)
                config.operationOrdering = OPERATION_PATH_NATURAL_ORDERING.compound(OPERATION_METHOD_NATURAL_ORDERING);
            if (config.definitionOrderBy == OrderBy.NATURAL)
                config.definitionOrdering = Ordering.natural();
            if (config.parameterOrderBy == OrderBy.NATURAL)
                config.parameterOrdering = PARAMETER_IN_NATURAL_ORDERING.compound(PARAMETER_NAME_NATURAL_ORDERING);
            if (config.propertyOrderBy == OrderBy.NATURAL)
                config.propertyOrdering = Ordering.natural();
            if (config.responseOrderBy == OrderBy.NATURAL)
                config.responseOrdering = Ordering.natural();
        }

        /**
         * Specifies the markup language which should be used to generate the files
         *
         * @param markupLanguage the markup language which is used to generate the files
         * @return this builder
         */
        public Builder withMarkupLanguage(MarkupLanguage markupLanguage) {
            Validate.notNull(markupLanguage, "%s must not be null", "markupLanguage");
            config.markupLanguage = markupLanguage;
            return this;
        }

        /**
         * Include generated examples into the Paths document
         *
         * @return this builder
         */
        public Builder withGeneratedExamples() {
            config.generatedExamplesEnabled = true;
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document
         *
         * @param schemasUri the URI to the folder where the schema documents reside.
         * @return this builder
         */
        public Builder withSchemas(URI schemasUri) {
            Validate.notNull(schemasUri, "%s must not be null", "schemasUri");
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
            Validate.notNull(schemasPath, "%s must not be null", "schemasPath");
            return withSchemas(schemasPath.toUri());
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document.<br/>
         * This is an alias for {@link #withSchemas(URI) withSchemas(null)}.
         *
         * @return this builder
         */
        public Builder withSchemas() {
            config.schemasEnabled = true;
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths document
         *
         * @param operationDescriptionsUri the URI to the folder where the description documents reside. Use default URI if null.
         * @return this builder
         */
        public Builder withOperationDescriptions(URI operationDescriptionsUri) {
            Validate.notNull(operationDescriptionsUri, "%s must not be null", "operationDescriptionsUri");
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
            Validate.notNull(operationDescriptionsPath, "%s must not be null", "operationDescriptionsPath");
            return withOperationDescriptions(operationDescriptionsPath.toUri());
        }

        /**
         * Include hand-written descriptions into the Paths document.<br/>
         * This is an alias for {@link #withOperationDescriptions(URI) withOperationDescriptions(null)}.
         *
         * @return this builder
         */
        public Builder withOperationDescriptions() {
            config.operationDescriptionsEnabled = true;
            return this;
        }

        /**
         * Include hand-written descriptions into the Definitions document
         *
         * @param definitionDescriptionsUri the URI to the folder where the description documents reside. Use default URI if null.
         * @return this builder
         */
        public Builder withDefinitionDescriptions(URI definitionDescriptionsUri) {
            Validate.notNull(definitionDescriptionsUri, "%s must not be null", "definitionDescriptionsUri");
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
            Validate.notNull(definitionDescriptionsPath, "%s must not be null", "definitionDescriptionsPath");
            return withDefinitionDescriptions(definitionDescriptionsPath.toUri());
        }

        /**
         * Include hand-written descriptions into the Definitions document.<br/>
         * This is an alias for {@link #withDefinitionDescriptions(URI) withDefinitionDescriptions(null)}.
         *
         * @return this builder
         */
        public Builder withDefinitionDescriptions() {
            config.definitionDescriptionsEnabled = true;
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
            Validate.notNull(pathsGroupedBy, "%s must not be null", "pathsGroupedBy");
            config.operationsGroupedBy = pathsGroupedBy;
            return this;
        }

        /**
         * Specifies labels language of output files
         *
         * @param language the enum
         * @return this builder
         */
        public Builder withOutputLanguage(Language language) {
            Validate.notNull(language, "%s must not be null", "language");
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
            Validate.notNull(inlineSchemaDepthLevel, "%s must not be null", "inlineSchemaDepthLevel");
            config.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
            return this;
        }

        /**
         * Specifies tag ordering.<br/>
         * By default tag ordering == {@link io.github.robwin.swagger2markup.OrderBy#NATURAL}.<br/>
         * Use {@link #withTagOrdering(Comparator)} to set a custom ordering.
         *
         * @param orderBy tag ordering
         * @return this builder
         */
        public Builder withTagOrdering(OrderBy orderBy) {
            Validate.notNull(orderBy, "%s must not be null", "orderBy");
            Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
            config.tagOrderBy = orderBy;
            return this;
        }

        /**
         * Specifies a custom comparator function to order tags.
         *
         * @param tagOrdering tag ordering
         * @return this builder
         */
        public Builder withTagOrdering(Comparator<String> tagOrdering) {
            Validate.notNull(tagOrdering, "%s must not be null", "tagOrdering");
            config.tagOrderBy = OrderBy.CUSTOM;
            config.tagOrdering = tagOrdering;
            return this;
        }

        /**
         * Specifies operation ordering.<br/>
         * By default operation ordering == {@link io.github.robwin.swagger2markup.OrderBy#AS_IS}.<br/>
         * Use {@link #withOperationOrdering(Comparator)} to set a custom ordering.
         *
         * @param orderBy operation ordering
         * @return this builder
         */
        public Builder withOperationOrdering(OrderBy orderBy) {
            Validate.notNull(orderBy, "%s must not be null", "orderBy");
            Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
            config.operationOrderBy = orderBy;
            return this;
        }

        /**
         * Specifies a custom comparator function to order operations.
         *
         * @param operationOrdering operation ordering
         * @return this builder
         */
        public Builder withOperationOrdering(Comparator<PathOperation> operationOrdering) {
            Validate.notNull(operationOrdering, "%s must not be null", "operationOrdering");
            config.operationOrderBy = OrderBy.CUSTOM;
            config.operationOrdering = operationOrdering;
            return this;
        }

        /**
         * Specifies definition ordering.<br/>
         * By default definition ordering == {@link io.github.robwin.swagger2markup.OrderBy#NATURAL}.<br/>
         * Use {@link #withDefinitionOrdering(Comparator)} to set a custom ordering.
         *
         * @param orderBy definition ordering
         * @return this builder
         */
        public Builder withDefinitionOrdering(OrderBy orderBy) {
            Validate.notNull(orderBy, "%s must not be null", "orderBy");
            Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
            config.definitionOrderBy = orderBy;
            return this;
        }

        /**
         * Specifies a custom comparator function to order definitions.
         *
         * @param definitionOrdering definition ordering
         * @return this builder
         */
        public Builder withDefinitionOrdering(Comparator<String> definitionOrdering) {
            Validate.notNull(definitionOrdering, "%s must not be null", "definitionOrdering");
            config.definitionOrderBy = OrderBy.CUSTOM;
            config.definitionOrdering = definitionOrdering;
            return this;
        }

        /**
         * Specifies parameter ordering.<br/>
         * By default parameter ordering == {@link io.github.robwin.swagger2markup.OrderBy#NATURAL}.<br/>
         * Use {@link #withParameterOrdering(Comparator)} to set a custom ordering.
         *
         * @param orderBy parameter ordering
         * @return this builder
         */
        public Builder withParameterOrdering(OrderBy orderBy) {
            Validate.notNull(orderBy, "%s must not be null", "orderBy");
            Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
            config.parameterOrderBy = orderBy;
            return this;
        }

        /**
         * Specifies a custom comparator function to order parameters.
         *
         * @param parameterOrdering parameter ordering
         * @return this builder
         */
        public Builder withParameterOrdering(Comparator<Parameter> parameterOrdering) {
            Validate.notNull(parameterOrdering, "%s must not be null", "parameterOrdering");

            config.parameterOrderBy = OrderBy.CUSTOM;
            config.parameterOrdering = parameterOrdering;
            return this;
        }

        /**
         * Specifies property ordering.<br/>
         * By default property ordering == {@link io.github.robwin.swagger2markup.OrderBy#NATURAL}.<br/>
         * Use {@link #withPropertyOrdering(Comparator)} to set a custom ordering.
         *
         * @param orderBy property ordering
         * @return this builder
         */
        public Builder withPropertyOrdering(OrderBy orderBy) {
            Validate.notNull(orderBy, "%s must not be null", "orderBy");
            Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
            config.propertyOrderBy = orderBy;
            return this;
        }

        /**
         * Specifies a custom comparator function to order propertys.
         *
         * @param propertyOrdering property ordering
         * @return this builder
         */
        public Builder withPropertyOrdering(Comparator<String> propertyOrdering) {
            Validate.notNull(propertyOrdering, "%s must not be null", "propertyOrdering");

            config.propertyOrderBy = OrderBy.CUSTOM;
            config.propertyOrdering = propertyOrdering;
            return this;
        }

        /**
         * Specifies response ordering.<br/>
         * By default response ordering == {@link io.github.robwin.swagger2markup.OrderBy#NATURAL}.<br/>
         * Use {@link #withResponseOrdering(Comparator)} to set a custom ordering.
         *
         * @param orderBy response ordering
         * @return this builder
         */
        public Builder withResponseOrdering(OrderBy orderBy) {
            Validate.notNull(orderBy, "%s must not be null", "orderBy");
            Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
            config.responseOrderBy = orderBy;
            return this;
        }

        /**
         * Specifies a custom comparator function to order responses.
         *
         * @param responseOrdering response ordering
         * @return this builder
         */
        public Builder withResponseOrdering(Comparator<String> responseOrdering) {
            Validate.notNull(responseOrdering, "%s must not be null", "responseOrdering");

            config.responseOrderBy = OrderBy.CUSTOM;
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
            Validate.notNull(prefix, "%s must not be null", "prefix");
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
            Validate.notNull(anchorPrefix, "%s must no be null", "anchorPrefix");
            config.anchorPrefix = anchorPrefix;
            return this;
        }

    }

}