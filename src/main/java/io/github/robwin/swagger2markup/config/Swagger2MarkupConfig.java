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

import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.Language;
import io.github.robwin.swagger2markup.OrderBy;
import io.github.robwin.swagger2markup.PathOperation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

import java.util.Comparator;

public class Swagger2MarkupConfig {

    private final Swagger swagger;
    private final MarkupLanguage markupLanguage;
    private final String examplesFolderPath;
    private final String schemasFolderPath;
    private final String descriptionsFolderPath;
    private final String operationExtensionsFolderPath;
    private final String definitionExtensionsFolderPath;
    private final boolean separatedDefinitions;
    private final boolean separatedOperations;
    private final GroupBy pathsGroupedBy;
    @Deprecated
    private final OrderBy definitionsOrderedBy;
    private final Language outputLanguage;
    private final int inlineSchemaDepthLevel;
    private final Comparator<String> tagOrdering;
    private final Comparator<PathOperation> operationOrdering;
    private final Comparator<String> definitionOrdering;
    private final Comparator<Parameter> parameterOrdering;
    private final Comparator<String> propertyOrdering;
    private final Comparator<String> responseOrdering;
    private final boolean interDocumentCrossReferences;
    private final String interDocumentCrossReferencesPrefix;
    private final boolean flatBody;
    private final String anchorPrefix;

    private static final String OVERVIEW_DOCUMENT = "overview";
    private static final String PATHS_DOCUMENT = "paths";
    private static final String DEFINITIONS_DOCUMENT = "definitions";
    private static final String SECURITY_DOCUMENT = "security";

    private static final String SEPARATED_DEFINITIONS_FOLDER = "definitions";
    private static final String SEPARATED_OPERATIONS_FOLDER = "operations";

    /**
     * @param swagger the Swagger source
     * @param markupLanguage the markup language which is used to generate the files
     * @param examplesFolderPath examplesFolderPath the path to the folder where the example documents reside
     * @param schemasFolderPath the path to the folder where the schema documents reside
     * @param descriptionsFolderPath the path to the folder where the description documents reside
     * @param operationExtensionsFolderPath the path to the folder where the operation extension documents reside
     * @param definitionExtensionsFolderPath the path to the folder where the definition extension documents reside
     * @param separatedDefinitions specified if in addition to the definitions file, also separate definition files for each model definition should be created
     * @param separatedOperations specified if in addition to the paths file, also separate operation files for each operation should be created
     * @param pathsGroupedBy specifies if the paths should be grouped by tags or stay as-is
     * @param definitionsOrderedBy specifies if the definitions should be ordered by natural ordering or stay as-is
     * @param outputLanguage specifies language of labels in output files
     * @param inlineSchemaDepthLevel specifies the max depth for inline object schema display (0 = no inline schemas)
     * @param tagOrdering specifies a custom comparator function to order tags (null = as-is ordering)
     * @param operationOrdering specifies a custom comparator function to order operations (null = as-is ordering)
     * @param definitionOrdering specifies a custom comparator function to order definitions (null = as-is ordering)
     * @param parameterOrdering specifies a custom comparator function to order parameters (null = as-is ordering)
     * @param propertyOrdering specifies a custom comparator function to order properties (null = as-is ordering)
     * @param responseOrdering specifies a custom comparator function to order responses (null = as-is ordering)
     * @param interDocumentCrossReferences enable use of inter-document cross-references when needed
     * @param interDocumentCrossReferencesPrefix set an optional prefix for inter-document cross-references
     * @param flatBody optionally isolate the body parameter, if any, from other parameters
     * @param anchorPrefix optionally prefix all anchors for unicity
     */
    public Swagger2MarkupConfig(Swagger swagger, MarkupLanguage markupLanguage, String examplesFolderPath,
                                String schemasFolderPath, String descriptionsFolderPath, String operationExtensionsFolderPath, String definitionExtensionsFolderPath,
                                boolean separatedDefinitions, boolean separatedOperations,
                                GroupBy pathsGroupedBy, OrderBy definitionsOrderedBy, Language outputLanguage,
                                int inlineSchemaDepthLevel, Comparator<String> tagOrdering, Comparator<PathOperation> operationOrdering,
                                Comparator<String> definitionOrdering, Comparator<Parameter> parameterOrdering, Comparator<String> propertyOrdering,
                                Comparator<String> responseOrdering,
                                boolean interDocumentCrossReferences, String interDocumentCrossReferencesPrefix,
                                boolean flatBody, String anchorPrefix) {
        this.swagger = swagger;
        this.markupLanguage = markupLanguage;
        this.examplesFolderPath = examplesFolderPath;
        this.schemasFolderPath = schemasFolderPath;
        this.descriptionsFolderPath = descriptionsFolderPath;
        this.operationExtensionsFolderPath = operationExtensionsFolderPath;
        this.definitionExtensionsFolderPath = definitionExtensionsFolderPath;
        this.separatedDefinitions = separatedDefinitions;
        this.separatedOperations = separatedOperations;
        this.pathsGroupedBy = pathsGroupedBy;
        this.definitionsOrderedBy = definitionsOrderedBy;
        this.outputLanguage = outputLanguage;
        this.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
        this.tagOrdering = tagOrdering;
        this.operationOrdering = operationOrdering;
        this.definitionOrdering = definitionOrdering;
        this.parameterOrdering = parameterOrdering;
        this.propertyOrdering = propertyOrdering;
        this.responseOrdering = responseOrdering;
        this.interDocumentCrossReferences = interDocumentCrossReferences;
        this.interDocumentCrossReferencesPrefix = interDocumentCrossReferencesPrefix;
        this.flatBody = flatBody;
        this.anchorPrefix = anchorPrefix;
    }

    public Swagger getSwagger() {
        return swagger;
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

    public String getOverviewDocument() {
        return OVERVIEW_DOCUMENT;
    }

    public String getPathsDocument() {
        return PATHS_DOCUMENT;
    }

    public String getDefinitionsDocument() {
        return DEFINITIONS_DOCUMENT;
    }

    public String getSecurityDocument() {
        return SECURITY_DOCUMENT;
    }

    public String getSeparatedDefinitionsFolder() {
        return SEPARATED_DEFINITIONS_FOLDER;
    }

    public String getSeparatedOperationsFolder() {
        return SEPARATED_OPERATIONS_FOLDER;
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
}
