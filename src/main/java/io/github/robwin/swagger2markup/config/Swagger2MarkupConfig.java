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
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Properties;

public class Swagger2MarkupConfig {

    private static final Logger logger = LoggerFactory.getLogger(Swagger2MarkupConfig.class);

    private MarkupLanguage markupLanguage;
    private boolean examples;
    private String examplesPath;
    private boolean schemas;
    private String schemasPath;
    private boolean operationDescriptions;
    private String operationDescriptionsPath;
    private boolean definitionDescriptions;
    private String definitionDescriptionsPath;
    private boolean separatedDefinitions;
    private boolean separatedOperations;
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
    private boolean interDocumentCrossReferences;
    private String interDocumentCrossReferencesPrefix;
    private boolean flatBody;
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
        configureDefaultContentPaths(globalContext.swaggerLocation != null ? new File(globalContext.swaggerLocation).getParentFile() : null);
        onUpdateGlobalContext(globalContext);
    }

    /**
     * Overridable onUpdateGlobalContext event listener.
     *
     * @param globalContext Partially initialized global context (globalContext.extensionRegistry == null)
     */
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        /* must be left empty */
    }

    /**
     * Automatically set default path for external content files based on specified {@code basePath}.<br/>
     * If {@code basePath} is null, default path can't be set and a RuntimeException is thrown.
     *
     * @param basePath base path to set default paths
     * @throws RuntimeException if basePath == null and any path is not configured
     */
    private void configureDefaultContentPaths(File basePath) {
        if (examples && examplesPath == null) {
            if (basePath == null) {
                if (logger.isWarnEnabled())
                    logger.warn("No explicit '%s' set and no default available > Disable %s", "examplesPath", "examples");
                examples = false;
            } else
                examplesPath = basePath.getPath();
        }

        if (schemas && schemasPath == null) {
            if (basePath == null) {
                if (logger.isWarnEnabled())
                    logger.warn("No explicit '%s' set and no default available > Disable %s", "schemasPath", "schemas");
                schemas = false;
            } else
                schemasPath = basePath.getPath();
        }

        if (operationDescriptions && operationDescriptionsPath == null) {
            if (basePath == null) {
                if (logger.isWarnEnabled())
                    logger.warn("No explicit '%s' set and no default available > Disable %s", "operationDescriptionsPath", "operationDescriptions");
                operationDescriptions = false;
            } else
                operationDescriptionsPath = basePath.getPath();
        }

        if (definitionDescriptions && definitionDescriptionsPath == null) {
            if (basePath == null) {
                if (logger.isWarnEnabled())
                    logger.warn("No explicit '%s' set and no default available > Disable %s", "definitionDescriptionsPath", "definitionDescriptions");
                definitionDescriptions = false;
            } else
                definitionDescriptionsPath = basePath.getPath();
        }
    }

    public MarkupLanguage getMarkupLanguage() {
        return markupLanguage;
    }

    public boolean isExamples() {
        return examples;
    }

    public String getExamplesPath() {
        return examplesPath;
    }

    public boolean isSchemas() {
        return schemas;
    }

    public String getSchemasPath() {
        return schemasPath;
    }

    public boolean isOperationDescriptions() {
        return operationDescriptions;
    }

    public String getOperationDescriptionsPath() {
        return operationDescriptionsPath;
    }

    public boolean isDefinitionDescriptions() {
        return definitionDescriptions;
    }

    public String getDefinitionDescriptionsPath() {
        return definitionDescriptionsPath;
    }

    public boolean isSeparatedDefinitions() {
        return separatedDefinitions;
    }

    public boolean isSeparatedOperations() {
        return separatedOperations;
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

    public boolean isInterDocumentCrossReferences() {
        return interDocumentCrossReferences;
    }

    public String getInterDocumentCrossReferencesPrefix() {
        return interDocumentCrossReferencesPrefix;
    }

    public boolean isFlatBody() {
        return flatBody;
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

        public Builder() {
            this(new Properties());
        }

        public Builder(Properties properties) {

            Properties safeProperties = new Properties(defaultProperties());
            safeProperties.putAll(properties);

            config.markupLanguage = MarkupLanguage.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "markupLanguage"));
            config.examples = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "examples"));
            config.examplesPath = safeProperties.getProperty(PROPERTIES_PREFIX + "examplesPath");
            config.schemas = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "schemas"));
            config.schemasPath = safeProperties.getProperty(PROPERTIES_PREFIX + "schemasPath");
            config.operationDescriptions = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "operationDescriptions"));
            config.operationDescriptionsPath = safeProperties.getProperty(PROPERTIES_PREFIX + "operationDescriptionsPath");
            config.definitionDescriptions = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "definitionDescriptions"));
            config.definitionDescriptionsPath = safeProperties.getProperty(PROPERTIES_PREFIX + "definitionDescriptionsPath");
            config.separatedDefinitions = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "separatedDefinitions"));
            config.separatedOperations = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "separatedOperations"));
            config.operationsGroupedBy = GroupBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "operationsGroupedBy"));
            config.definitionsOrderedBy = OrderBy.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "definitionsOrderedBy"));
            config.outputLanguage = Language.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "outputLanguage"));
            config.inlineSchemaDepthLevel = Integer.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "inlineSchemaDepthLevel"));
            config.interDocumentCrossReferences = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "interDocumentCrossReferences"));
            config.interDocumentCrossReferencesPrefix = safeProperties.getProperty(PROPERTIES_PREFIX + "interDocumentCrossReferencesPrefix");
            config.flatBody = Boolean.valueOf(safeProperties.getProperty(PROPERTIES_PREFIX + "flatBody"));
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
         * @param examplesPath the path to the folder where the example documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withExamples(String examplesPath) {
            config.examples = true;

            config.examplesPath = examplesPath;
            return this;
        }

        /**
         * Include examples into the Paths document.<br/>
         * This is an alias for {@link #withExamples(String) withExamples(null)}.
         *
         * @return this builder
         */
        public Builder withExamples() {
            withExamples(null);
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document
         *
         * @param schemasPath the path to the folder where the schema documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withSchemas(String schemasPath) {
            config.schemas = true;
            config.schemasPath = schemasPath;
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document.<br/>
         * This is an alias for {@link #withSchemas(String) withSchemas(null)}.
         *
         * @return this builder
         */
        public Builder withSchemas() {
            withSchemas(null);
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths document
         *
         * @param operationDescriptionsPath the path to the folder where the description documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withOperationDescriptions(String operationDescriptionsPath) {
            config.operationDescriptions = true;
            config.operationDescriptionsPath = operationDescriptionsPath;
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths document.<br/>
         * This is an alias for {@link #withOperationDescriptions(String) withOperationDescriptions(null)}.
         *
         * @return this builder
         */
        public Builder withOperationDescriptions() {
            withOperationDescriptions(null);
            return this;
        }

        /**
         * Include hand-written descriptions into the Definitions document
         *
         * @param definitionDescriptionsPath the path to the folder where the description documents reside. Use default path if null.
         * @return this builder
         */
        public Builder withDefinitionDescriptions(String definitionDescriptionsPath) {
            config.definitionDescriptions = true;
            config.definitionDescriptionsPath = definitionDescriptionsPath;
            return this;
        }

        /**
         * Include hand-written descriptions into the Definitions document.<br/>
         * This is an alias for {@link #withDefinitionDescriptions(String) withDefinitionDescriptions(null)}.
         *
         * @return this builder
         */
        public Builder withDefinitionDescriptions() {
            withDefinitionDescriptions(null);
            return this;
        }

        /**
         * In addition to the definitions file, also create separate definition files for each model definition.
         *
         * @return this builder
         */
        public Builder withSeparatedDefinitions() {
            config.separatedDefinitions = true;
            return this;
        }


        /**
         * In addition to the paths file, also create separate path files for each path.
         *
         * @return this builder
         */
        public Builder withSeparatedOperations() {
            config.separatedOperations = true;
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
         * Specifies maximum depth level for inline object schema displaying (0 = no inline schemas)
         *
         * @param inlineSchemaDepthLevel number of recursion levels for inline schemas display
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
            config.interDocumentCrossReferences = true;
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
            config.flatBody = true;
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