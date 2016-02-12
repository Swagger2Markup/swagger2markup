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
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;

import java.util.Comparator;

public class Swagger2MarkupConfig {

    private final Swagger swagger;
    private final MarkupLanguage markupLanguage;
    private final String examplesFolderPath;
    private final String schemasFolderPath;
    private final String descriptionsFolderPath;
    private final boolean separatedDefinitions;
    private final boolean separatedOperations;
    private final GroupBy pathsGroupedBy;
    @Deprecated
    private final OrderBy definitionsOrderedBy;
    private final Language outputLanguage;
    private final int inlineSchemaDepthLevel;
    private final Comparator<String> tagOrdering;
    private final Comparator<String> pathOrdering;
    private final Comparator<HttpMethod> pathMethodOrdering;
    private final Comparator<String> definitionOrdering;
    private final boolean interDocumentCrossReferences;
    private final String interDocumentCrossReferencesPrefix;

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
     * @param separatedDefinitions specified if in addition to the definitions file, also separate definition files for each model definition should be created
     * @param separatedOperations specified if in addition to the paths file, also separate operation files for each operation should be created
     * @param pathsGroupedBy specifies if the paths should be grouped by tags or stay as-is
     * @param definitionsOrderedBy specifies if the definitions should be ordered by natural ordering or stay as-is
     * @param outputLanguage specifies language of labels in output files
     * @param inlineSchemaDepthLevel specifies the max depth for inline object schema display (0 = no inline schemas)
     * @param tagOrdering specifies a custom comparator function to order tags (null = as-is ordering)
     * @param pathOrdering specifies a custom comparator function to order paths (null = as-is ordering)
     * @param pathMethodOrdering specifies a custom comparator function to order paths methods (null = as-is ordering)
     * @param definitionOrdering specifies a custom comparator function to order definitions (null = as-is ordering)
     * @param interDocumentCrossReferences enable use of inter-document cross-references when needed
     * @param interDocumentCrossReferencesPrefix set an optional prefix for inter-document cross-references
     */
    public Swagger2MarkupConfig(Swagger swagger, MarkupLanguage markupLanguage, String examplesFolderPath,
                                String schemasFolderPath, String descriptionsFolderPath, boolean separatedDefinitions, boolean separatedOperations,
                                GroupBy pathsGroupedBy, OrderBy definitionsOrderedBy, Language outputLanguage,
                                int inlineSchemaDepthLevel, Comparator<String> tagOrdering, Comparator<String> pathOrdering,
                                Comparator<HttpMethod> pathMethodOrdering, Comparator<String> definitionOrdering,
                                boolean interDocumentCrossReferences, String interDocumentCrossReferencesPrefix) {
        this.swagger = swagger;
        this.markupLanguage = markupLanguage;
        this.examplesFolderPath = examplesFolderPath;
        this.schemasFolderPath = schemasFolderPath;
        this.descriptionsFolderPath = descriptionsFolderPath;
        this.separatedDefinitions = separatedDefinitions;
        this.separatedOperations = separatedOperations;
        this.pathsGroupedBy = pathsGroupedBy;
        this.definitionsOrderedBy = definitionsOrderedBy;
        this.outputLanguage = outputLanguage;
        this.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
        this.tagOrdering = tagOrdering;
        this.pathOrdering = pathOrdering;
        this.pathMethodOrdering = pathMethodOrdering;
        this.definitionOrdering = definitionOrdering;
        this.interDocumentCrossReferences = interDocumentCrossReferences;
        this.interDocumentCrossReferencesPrefix = interDocumentCrossReferencesPrefix;
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

    public Comparator<String> getPathOrdering() {
        return pathOrdering;
    }

    public Comparator<HttpMethod> getPathMethodOrdering() {
        return pathMethodOrdering;
    }

    public Comparator<String> getDefinitionOrdering() {
        return definitionOrdering;
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
}
