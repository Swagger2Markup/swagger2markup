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
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.Language;
import io.github.robwin.swagger2markup.OrderBy;
import io.github.robwin.swagger2markup.PathOperation;
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Properties;

public class Swagger2MarkupConfig {

    private MarkupLanguage markupLanguage;
    private String examplesFolderPath;
    private String schemasFolderPath;
    private String descriptionsFolderPath;
    private String operationExtensionsFolderPath;
    private String definitionExtensionsFolderPath;
    private boolean separatedDefinitions;
    private boolean separatedOperations;
    private GroupBy pathsGroupedBy;
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

    public MarkupLanguage getMarkupLanguage() {
        return markupLanguage;
    }

    public String getExamplesFolderPath() {
        return examplesFolderPath;
    }

    public String getSchemasFolderPath() {
        return schemasFolderPath;
    }

    public String getDescriptionsFolderPath() {
        return descriptionsFolderPath;
    }

    public String getOperationExtensionsFolderPath() {
        return operationExtensionsFolderPath;
    }

    public String getDefinitionExtensionsFolderPath() {
        return definitionExtensionsFolderPath;
    }

    public boolean isSeparatedDefinitions() {
        return separatedDefinitions;
    }

    public boolean isSeparatedOperations() {
        return separatedOperations;
    }

    public GroupBy getPathsGroupedBy() {
        return pathsGroupedBy;
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

        static final String OVERVIEW_DOCUMENT = "overview";
        static final String PATHS_DOCUMENT = "paths";
        static final String DEFINITIONS_DOCUMENT = "definitions";
        static final String SECURITY_DOCUMENT = "security";

        static final String SEPARATED_DEFINITIONS_FOLDER = "definitions";
        static final String SEPARATED_OPERATIONS_FOLDER = "operations";

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

        private static final String PROPERTIES_PREFIX = "swagger2markup.";

        private Swagger2MarkupConfig config = new Swagger2MarkupConfig();

        public Builder() {
            config.markupLanguage = MarkupLanguage.ASCIIDOC;
            config.separatedDefinitions = false;
            config.separatedOperations = false;
            config.pathsGroupedBy = GroupBy.AS_IS;
            config.definitionsOrderedBy = OrderBy.NATURAL;
            config.outputLanguage = Language.EN;
            config.inlineSchemaDepthLevel = 0;
            config.tagOrdering = Ordering.natural();
            config.operationOrdering = OPERATION_PATH_COMPARATOR.compound(OPERATION_METHOD_COMPARATOR);
            config.definitionOrdering = Ordering.natural();
            config.parameterOrdering = PARAMETER_IN_COMPARATOR.compound(PARAMETER_NAME_COMPARATOR);
            config.propertyOrdering = Ordering.natural();
            config.responseOrdering = Ordering.natural();
            config.interDocumentCrossReferences = false;
            config.interDocumentCrossReferencesPrefix = "";
            config.flatBody = false;

            config.overviewDocument = OVERVIEW_DOCUMENT;
            config.pathsDocument = PATHS_DOCUMENT;
            config.definitionsDocument = DEFINITIONS_DOCUMENT;
            config.securityDocument = SECURITY_DOCUMENT;
            config.separatedOperationsFolder = SEPARATED_OPERATIONS_FOLDER;
            config.separatedDefinitionsFolder = SEPARATED_DEFINITIONS_FOLDER;

        }

        public Builder(Properties properties) {
            this();

            if (properties.containsKey(PROPERTIES_PREFIX + "markupLanguage"))
                config.markupLanguage = MarkupLanguage.valueOf(properties.getProperty(PROPERTIES_PREFIX + "markupLanguage"));
            if (properties.containsKey(PROPERTIES_PREFIX + "examplesFolderPath"))
                config.examplesFolderPath = properties.getProperty(PROPERTIES_PREFIX + "examplesFolderPath");
            if (properties.containsKey(PROPERTIES_PREFIX + "schemasFolderPath"))
                config.schemasFolderPath = properties.getProperty(PROPERTIES_PREFIX + "schemasFolderPath");
            if (properties.containsKey(PROPERTIES_PREFIX + "descriptionsFolderPath"))
                config.descriptionsFolderPath = properties.getProperty(PROPERTIES_PREFIX + "descriptionsFolderPath");
            if (properties.containsKey(PROPERTIES_PREFIX + "operationExtensionsFolderPath"))
                config.operationExtensionsFolderPath = properties.getProperty(PROPERTIES_PREFIX + "operationExtensionsFolderPath");
            if (properties.containsKey(PROPERTIES_PREFIX + "definitionExtensionsFolderPath"))
                config.definitionExtensionsFolderPath = properties.getProperty(PROPERTIES_PREFIX + "definitionExtensionsFolderPath");
            if (properties.containsKey(PROPERTIES_PREFIX + "separatedDefinitions"))
                config.separatedDefinitions = Boolean.valueOf(properties.getProperty(PROPERTIES_PREFIX + "separatedDefinitions"));
            if (properties.containsKey(PROPERTIES_PREFIX + "separatedOperations"))
                config.separatedOperations = Boolean.valueOf(properties.getProperty(PROPERTIES_PREFIX + "separatedOperations"));
            if (properties.containsKey(PROPERTIES_PREFIX + "pathsGroupedBy"))
                config.pathsGroupedBy = GroupBy.valueOf(properties.getProperty(PROPERTIES_PREFIX + "pathsGroupedBy"));
            if (properties.containsKey(PROPERTIES_PREFIX + "definitionsOrderedBy"))
                config.definitionsOrderedBy = OrderBy.valueOf(properties.getProperty(PROPERTIES_PREFIX + "definitionsOrderedBy"));
            if (properties.containsKey(PROPERTIES_PREFIX + "outputLanguage"))
                config.outputLanguage = Language.valueOf(properties.getProperty(PROPERTIES_PREFIX + "outputLanguage"));
            if (properties.containsKey(PROPERTIES_PREFIX + "inlineSchemaDepthLevel"))
                config.inlineSchemaDepthLevel = Integer.valueOf(properties.getProperty(PROPERTIES_PREFIX + "inlineSchemaDepthLevel"));
            if (properties.containsKey(PROPERTIES_PREFIX + "interDocumentCrossReferences"))
                config.interDocumentCrossReferences = Boolean.valueOf(properties.getProperty(PROPERTIES_PREFIX + "interDocumentCrossReferences"));
            if (properties.containsKey(PROPERTIES_PREFIX + "interDocumentCrossReferencesPrefix"))
                config.interDocumentCrossReferencesPrefix = properties.getProperty(PROPERTIES_PREFIX + "interDocumentCrossReferencesPrefix");
            if (properties.containsKey(PROPERTIES_PREFIX + "flatBody"))
                config.flatBody = Boolean.valueOf(properties.getProperty(PROPERTIES_PREFIX + "flatBody"));
            if (properties.containsKey(PROPERTIES_PREFIX + "anchorPrefix"))
                config.anchorPrefix = properties.getProperty(PROPERTIES_PREFIX + "anchorPrefix");
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
         * @param examplesFolderPath the path to the folder where the example documents reside
         * @return this builder
         */
        public Builder withExamples(String examplesFolderPath) {
            config.examplesFolderPath = examplesFolderPath;
            return this;
        }

        /**
         * Include (JSON, XML) schemas into the Definitions document
         *
         * @param schemasFolderPath the path to the folder where the schema documents reside
         * @return this builder
         */
        public Builder withSchemas(String schemasFolderPath) {
            config.schemasFolderPath = schemasFolderPath;
            return this;
        }

        /**
         * Include hand-written descriptions into the Paths and Definitions document
         *
         * @param descriptionsFolderPath the path to the folder where the description documents reside
         * @return this builder
         */
        public Builder withDescriptions(String descriptionsFolderPath) {
            config.descriptionsFolderPath = descriptionsFolderPath;
            return this;
        }

        /**
         * Include extensions into Paths document
         *
         * @param operationExtensionsFolderPath the path to the folder where the operation extension documents reside
         * @return this builder
         */
        public Builder withOperationExtensions(String operationExtensionsFolderPath) {
            config.operationExtensionsFolderPath = operationExtensionsFolderPath;
            return this;
        }


        /**
         * Include extensions into Definitions document
         *
         * @param definitionExtensionsFolderPath the path to the folder where the definition extension documents reside
         * @return this builder
         */
        public Builder withDefinitionExtensions(String definitionExtensionsFolderPath) {
            config.definitionExtensionsFolderPath = definitionExtensionsFolderPath;
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
            config.pathsGroupedBy = pathsGroupedBy;
            return this;
        }

        /**
         * Specifies if the definitions should be ordered by natural ordering or stay as-is.
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
         * @return this builder
         */
        public Builder withInterDocumentCrossReferences() {
            config.interDocumentCrossReferences = true;
            return this;
        }

        /**
         * Enable use of inter-document cross-references when needed
         *
         * @param prefix Prefix to document in all inter-document cross-references (null = no prefix)
         * @return this builder
         */
        public Builder withInterDocumentCrossReferences(String prefix) {
            if (prefix == null)
                return withInterDocumentCrossReferences();

            config.interDocumentCrossReferences = true;
            config.interDocumentCrossReferencesPrefix = prefix;
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