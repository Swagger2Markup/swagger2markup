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
package io.github.swagger2markup.builder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import io.github.robwin.markup.builder.LineSeparator;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.OrderBy;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

public class Swagger2MarkupConfigBuilder  {

    private static final Logger logger = LoggerFactory.getLogger(Swagger2MarkupConfigBuilder.class);

    private static final String PROPERTIES_PREFIX = "swagger2markup.";
    private static final String PROPERTIES_DEFAULT = "/io/github/swagger2markup/config/default.properties";

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

    DefaultSwagger2MarkupConfig config = new DefaultSwagger2MarkupConfig();

    public Swagger2MarkupConfigBuilder() {
        this(new HashedMap<String, String>());
    }

    public Swagger2MarkupConfigBuilder(Properties properties) {
        this(convertPropertiesToMap(properties));
    }

    private static Map<String, String> convertPropertiesToMap(Properties properties) {
        Map<String, String> propertiesMap = new HashedMap<>();
        for(String propertyName : properties.stringPropertyNames()){
            propertiesMap.put(propertyName, properties.getProperty(propertyName));
        }
        return propertiesMap;
    }

    public Swagger2MarkupConfigBuilder(Map<String, String> properties) {
        Map<String, String> safeProperties = convertPropertiesToMap(defaultProperties());
        safeProperties.putAll(properties);

        config.markupLanguage = MarkupLanguage.valueOf(safeProperties.get(PROPERTIES_PREFIX + "markupLanguage"));
        config.generatedExamplesEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "generatedExamplesEnabled"));
        config.operationDescriptionsEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "operationDescriptionsEnabled"));
        if (safeProperties.containsKey(PROPERTIES_PREFIX + "operationDescriptionsUri"))
            config.operationDescriptionsUri = URI.create(safeProperties.get(PROPERTIES_PREFIX + "operationDescriptionsUri"));
        config.definitionDescriptionsEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "definitionDescriptionsEnabled"));
        if (safeProperties.containsKey(PROPERTIES_PREFIX + "definitionDescriptionsUri"))
            config.definitionDescriptionsUri = URI.create(safeProperties.get(PROPERTIES_PREFIX + "definitionDescriptionsUri"));
        config.separatedDefinitionsEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "separatedDefinitionsEnabled"));
        config.separatedOperationsEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "separatedOperationsEnabled"));
        config.operationsGroupedBy = GroupBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "operationsGroupedBy"));
        config.outputLanguage = Language.valueOf(safeProperties.get(PROPERTIES_PREFIX + "outputLanguage"));
        config.inlineSchemaDepthLevel = Integer.valueOf(safeProperties.get(PROPERTIES_PREFIX + "inlineSchemaDepthLevel"));
        config.interDocumentCrossReferencesEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "interDocumentCrossReferencesEnabled"));
        config.interDocumentCrossReferencesPrefix = safeProperties.get(PROPERTIES_PREFIX + "interDocumentCrossReferencesPrefix");
        config.flatBodyEnabled = Boolean.valueOf(safeProperties.get(PROPERTIES_PREFIX + "flatBodyEnabled"));
        config.anchorPrefix = safeProperties.get(PROPERTIES_PREFIX + "anchorPrefix");
        config.overviewDocument = safeProperties.get(PROPERTIES_PREFIX + "overviewDocument");
        config.pathsDocument = safeProperties.get(PROPERTIES_PREFIX + "pathsDocument");
        config.definitionsDocument = safeProperties.get(PROPERTIES_PREFIX + "definitionsDocument");
        config.securityDocument = safeProperties.get(PROPERTIES_PREFIX + "securityDocument");
        config.separatedOperationsFolder = safeProperties.get(PROPERTIES_PREFIX + "separatedOperationsFolder");
        config.separatedDefinitionsFolder = safeProperties.get(PROPERTIES_PREFIX + "separatedDefinitionsFolder");
        config.tagOrderBy = OrderBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "tagOrderBy"));
        config.operationOrderBy = OrderBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "operationOrderBy"));
        config.definitionOrderBy = OrderBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "definitionOrderBy"));
        config.parameterOrderBy = OrderBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "parameterOrderBy"));
        config.propertyOrderBy = OrderBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "propertyOrderBy"));
        config.responseOrderBy = OrderBy.valueOf(safeProperties.get(PROPERTIES_PREFIX + "responseOrderBy"));
        String lineSeparator = safeProperties.get(PROPERTIES_PREFIX + "lineSeparator");
        if(StringUtils.isNoneBlank(lineSeparator)){
            config.lineSeparator = LineSeparator.valueOf(lineSeparator);
        }

        config.extensionsProperties = Maps.filterKeys(safeProperties, new Predicate<String>(){
            @Override
            public boolean apply(@Nullable String propertyName) {
                return StringUtils.contains(propertyName, "extensions");
            }
        });
    }

    private Properties defaultProperties() {
        Properties defaultProperties = new Properties();
        try {
            InputStream defaultPropertiesStream = Swagger2MarkupConfigBuilder.class.getResourceAsStream(PROPERTIES_DEFAULT);
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
     * Specifies the markup language which should be used to generate the files.
     *
     * @param markupLanguage the markup language which is used to generate the files
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withMarkupLanguage(MarkupLanguage markupLanguage) {
        Validate.notNull(markupLanguage, "%s must not be null", "markupLanguage");
        config.markupLanguage = markupLanguage;
        return this;
    }

    /**
     * Include generated examples into the documents.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withGeneratedExamples() {
        config.generatedExamplesEnabled = true;
        return this;
    }

    /**
     * Include hand-written descriptions into the Paths document.
     *
     * @param operationDescriptionsUri the URI to the folder where the description documents reside.
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withOperationDescriptions(URI operationDescriptionsUri) {
        Validate.notNull(operationDescriptionsUri, "%s must not be null", "operationDescriptionsUri");
        config.operationDescriptionsEnabled = true;
        config.operationDescriptionsUri = operationDescriptionsUri;
        return this;
    }

    /**
     * Include hand-written descriptions into the Paths document.
     *
     * @param operationDescriptionsPath the path to the folder where the description documents reside.
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withOperationDescriptions(Path operationDescriptionsPath) {
        Validate.notNull(operationDescriptionsPath, "%s must not be null", "operationDescriptionsPath");
        return withOperationDescriptions(operationDescriptionsPath.toUri());
    }

    /**
     * Include hand-written descriptions into the Paths document.<br>
     * Use default URI.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withOperationDescriptions() {
        config.operationDescriptionsEnabled = true;
        return this;
    }

    /**
     * Include hand-written descriptions into the Definitions document.
     *
     * @param definitionDescriptionsUri the URI to the folder where the description documents reside.
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withDefinitionDescriptions(URI definitionDescriptionsUri) {
        Validate.notNull(definitionDescriptionsUri, "%s must not be null", "definitionDescriptionsUri");
        config.definitionDescriptionsEnabled = true;
        config.definitionDescriptionsUri = definitionDescriptionsUri;
        return this;
    }

    /**
     * Include hand-written descriptions into the Definitions document.
     *
     * @param definitionDescriptionsPath the path to the folder where the description documents reside.
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withDefinitionDescriptions(Path definitionDescriptionsPath) {
        Validate.notNull(definitionDescriptionsPath, "%s must not be null", "definitionDescriptionsPath");
        return withDefinitionDescriptions(definitionDescriptionsPath.toUri());
    }

    /**
     * Include hand-written descriptions into the Definitions document.<br>
     * Use default URI.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withDefinitionDescriptions() {
        config.definitionDescriptionsEnabled = true;
        return this;
    }

    /**
     * In addition to the Definitions file, also create separate definition files for each model definition.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withSeparatedDefinitions() {
        config.separatedDefinitionsEnabled = true;
        return this;
    }


    /**
     * In addition to the Paths file, also create separate operation files for each operation.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withSeparatedOperations() {
        config.separatedOperationsEnabled = true;
        return this;
    }


    /**
     * Specifies if the operations should be grouped by tags or stay as-is.
     *
     * @param pathsGroupedBy the GroupBy enum
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withPathsGroupedBy(GroupBy pathsGroupedBy) {
        Validate.notNull(pathsGroupedBy, "%s must not be null", "pathsGroupedBy");
        config.operationsGroupedBy = pathsGroupedBy;
        return this;
    }

    /**
     * Specifies labels language of output files.
     *
     * @param language the enum
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withOutputLanguage(Language language) {
        Validate.notNull(language, "%s must not be null", "language");
        config.outputLanguage = language;
        return this;
    }

    /**
     * Specifies maximum depth level for inline object schema displaying (0 = no inline schemasEnabled).
     *
     * @param inlineSchemaDepthLevel number of recursion levels for inline schemasEnabled display
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withInlineSchemaDepthLevel(int inlineSchemaDepthLevel) {
        Validate.isTrue(inlineSchemaDepthLevel >= 0, "%s must be >= 0", "inlineSchemaDepthLevel");
        config.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
        return this;
    }

    /**
     * Specifies tag ordering.<br>
     * By default tag ordering == {@link io.github.swagger2markup.OrderBy#NATURAL}.<br>
     * Use {@link #withTagOrdering(Comparator)} to set a custom ordering.
     *
     * @param orderBy tag ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withTagOrdering(OrderBy orderBy) {
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
    public Swagger2MarkupConfigBuilder withTagOrdering(Comparator<String> tagOrdering) {
        Validate.notNull(tagOrdering, "%s must not be null", "tagOrdering");
        config.tagOrderBy = OrderBy.CUSTOM;
        config.tagOrdering = tagOrdering;
        return this;
    }

    /**
     * Specifies operation ordering.<br>
     * By default operation ordering == {@link io.github.swagger2markup.OrderBy#AS_IS}.<br>
     * Use {@link #withOperationOrdering(Comparator)} to set a custom ordering.
     *
     * @param orderBy operation ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withOperationOrdering(OrderBy orderBy) {
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
    public Swagger2MarkupConfigBuilder withOperationOrdering(Comparator<PathOperation> operationOrdering) {
        Validate.notNull(operationOrdering, "%s must not be null", "operationOrdering");
        config.operationOrderBy = OrderBy.CUSTOM;
        config.operationOrdering = operationOrdering;
        return this;
    }

    /**
     * Specifies definition ordering.<br>
     * By default definition ordering == {@link io.github.swagger2markup.OrderBy#NATURAL}.<br>
     * Use {@link #withDefinitionOrdering(Comparator)} to set a custom ordering.
     *
     * @param orderBy definition ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withDefinitionOrdering(OrderBy orderBy) {
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
    public Swagger2MarkupConfigBuilder withDefinitionOrdering(Comparator<String> definitionOrdering) {
        Validate.notNull(definitionOrdering, "%s must not be null", "definitionOrdering");
        config.definitionOrderBy = OrderBy.CUSTOM;
        config.definitionOrdering = definitionOrdering;
        return this;
    }

    /**
     * Specifies parameter ordering.<br>
     * By default parameter ordering == {@link OrderBy#NATURAL}.<br>
     * Use {@link #withParameterOrdering(Comparator)} to set a custom ordering.
     *
     * @param orderBy parameter ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withParameterOrdering(OrderBy orderBy) {
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
    public Swagger2MarkupConfigBuilder withParameterOrdering(Comparator<Parameter> parameterOrdering) {
        Validate.notNull(parameterOrdering, "%s must not be null", "parameterOrdering");

        config.parameterOrderBy = OrderBy.CUSTOM;
        config.parameterOrdering = parameterOrdering;
        return this;
    }

    /**
     * Specifies property ordering.<br>
     * By default property ordering == {@link io.github.swagger2markup.OrderBy#NATURAL}.<br>
     * Use {@link #withPropertyOrdering(Comparator)} to set a custom ordering.
     *
     * @param orderBy property ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withPropertyOrdering(OrderBy orderBy) {
        Validate.notNull(orderBy, "%s must not be null", "orderBy");
        Validate.isTrue(orderBy != OrderBy.CUSTOM, "You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        config.propertyOrderBy = orderBy;
        return this;
    }

    /**
     * Specifies a custom comparator function to order properties.
     *
     * @param propertyOrdering property ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withPropertyOrdering(Comparator<String> propertyOrdering) {
        Validate.notNull(propertyOrdering, "%s must not be null", "propertyOrdering");

        config.propertyOrderBy = OrderBy.CUSTOM;
        config.propertyOrdering = propertyOrdering;
        return this;
    }

    /**
     * Specifies response ordering.<br>
     * By default response ordering == {@link io.github.swagger2markup.OrderBy#NATURAL}.<br>
     * Use {@link #withResponseOrdering(Comparator)} to set a custom ordering.
     *
     * @param orderBy response ordering
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withResponseOrdering(OrderBy orderBy) {
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
    public Swagger2MarkupConfigBuilder withResponseOrdering(Comparator<String> responseOrdering) {
        Validate.notNull(responseOrdering, "%s must not be null", "responseOrdering");

        config.responseOrderBy = OrderBy.CUSTOM;
        config.responseOrdering = responseOrdering;
        return this;
    }

    /**
     * Enable use of inter-document cross-references when needed.
     *
     * @param prefix Prefix to document in all inter-document cross-references.
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withInterDocumentCrossReferences(String prefix) {
        Validate.notNull(prefix, "%s must not be null", "prefix");
        config.interDocumentCrossReferencesEnabled = true;
        config.interDocumentCrossReferencesPrefix = prefix;
        return this;
    }

    /**
     * Enable use of inter-document cross-references when needed.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withInterDocumentCrossReferences() {
        config.interDocumentCrossReferencesEnabled = true;
        return this;
    }

    /**
     * Optionally isolate the body parameter, if any, from other parameters.
     *
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withFlatBody() {
        config.flatBodyEnabled = true;
        return this;
    }

    /**
     * Optionally prefix all anchors for uniqueness.
     *
     * @param anchorPrefix anchor prefix.
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withAnchorPrefix(String anchorPrefix) {
        Validate.notNull(anchorPrefix, "%s must no be null", "anchorPrefix");
        config.anchorPrefix = anchorPrefix;
        return this;
    }

    /**
     * Specifies the line separator which should be used.
     *
     * @param lineSeparator the lineSeparator
     * @return this builder
     */
    public Swagger2MarkupConfigBuilder withLineSeparator(LineSeparator lineSeparator) {
        Validate.notNull(lineSeparator, "%s must no be null", "lineSeparator");
        config.lineSeparator = lineSeparator;
        return this;
    }

    public Swagger2MarkupConfigBuilder withExtensionsProperties(Map<String, String> extensionsProperties) {
        Validate.notEmpty(extensionsProperties, "%s must no be null", "extensionsProperties");
        config.extensionsProperties = extensionsProperties;
        return this;
    }

    static class DefaultSwagger2MarkupConfig implements Swagger2MarkupConfig{

        private MarkupLanguage markupLanguage;
        private boolean generatedExamplesEnabled;
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
        private LineSeparator lineSeparator;

        private String overviewDocument;
        private String pathsDocument;
        private String definitionsDocument;
        private String securityDocument;
        private String separatedOperationsFolder;
        private String separatedDefinitionsFolder;

        private Map<String, String> extensionsProperties;

        @Override
        public MarkupLanguage getMarkupLanguage() {
            return markupLanguage;
        }

        @Override
        public boolean isGeneratedExamplesEnabled() {
            return generatedExamplesEnabled;
        }

        @Override
        public boolean isOperationDescriptionsEnabled() {
            return operationDescriptionsEnabled;
        }

        @Override
        public URI getOperationDescriptionsUri() {
            return operationDescriptionsUri;
        }

        @Override
        public boolean isDefinitionDescriptionsEnabled() {
            return definitionDescriptionsEnabled;
        }

        @Override
        public URI getDefinitionDescriptionsUri() {
            return definitionDescriptionsUri;
        }

        @Override
        public boolean isSeparatedDefinitionsEnabled() {
            return separatedDefinitionsEnabled;
        }

        @Override
        public boolean isSeparatedOperationsEnabled() {
            return separatedOperationsEnabled;
        }

        @Override
        public GroupBy getOperationsGroupedBy() {
            return operationsGroupedBy;
        }

        @Override
        public Language getOutputLanguage() {
            return outputLanguage;
        }

        @Override
        public int getInlineSchemaDepthLevel() {
            return inlineSchemaDepthLevel;
        }

        @Override
        public OrderBy getTagOrderBy() {
            return tagOrderBy;
        }

        @Override
        public Comparator<String> getTagOrdering() {
            return tagOrdering;
        }

        @Override
        public OrderBy getOperationOrderBy() {
            return operationOrderBy;
        }

        @Override
        public Comparator<PathOperation> getOperationOrdering() {
            return operationOrdering;
        }

        @Override
        public OrderBy getDefinitionOrderBy() {
            return definitionOrderBy;
        }

        @Override
        public Comparator<String> getDefinitionOrdering() {
            return definitionOrdering;
        }

        @Override
        public OrderBy getParameterOrderBy() {
            return parameterOrderBy;
        }

        @Override
        public Comparator<Parameter> getParameterOrdering() {
            return parameterOrdering;
        }

        @Override
        public OrderBy getPropertyOrderBy() {
            return propertyOrderBy;
        }

        @Override
        public Comparator<String> getPropertyOrdering() {
            return propertyOrdering;
        }

        @Override
        public OrderBy getResponseOrderBy() {
            return responseOrderBy;
        }

        @Override
        public Comparator<String> getResponseOrdering() {
            return responseOrdering;
        }

        @Override
        public boolean isInterDocumentCrossReferencesEnabled() {
            return interDocumentCrossReferencesEnabled;
        }

        @Override
        public String getInterDocumentCrossReferencesPrefix() {
            return interDocumentCrossReferencesPrefix;
        }

        @Override
        public boolean isFlatBodyEnabled() {
            return flatBodyEnabled;
        }

        @Override
        public String getAnchorPrefix() {
            return anchorPrefix;
        }

        @Override
        public String getOverviewDocument() {
            return overviewDocument;
        }

        @Override
        public String getPathsDocument() {
            return pathsDocument;
        }

        @Override
        public String getDefinitionsDocument() {
            return definitionsDocument;
        }

        @Override
        public String getSecurityDocument() {
            return securityDocument;
        }

        @Override
        public String getSeparatedOperationsFolder() {
            return separatedOperationsFolder;
        }

        @Override
        public String getSeparatedDefinitionsFolder() {
            return separatedDefinitionsFolder;
        }

        @Override
        public LineSeparator getLineSeparator() {
            return lineSeparator;
        }

        @Override
        public Map<String, String> getExtensionsProperties() {
            return extensionsProperties;
        }
    }
}