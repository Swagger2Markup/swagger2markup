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
import io.swagger.models.Swagger;

public class Swagger2MarkupConfig {

    private final Swagger swagger;
    private final MarkupLanguage markupLanguage;
    private final String examplesFolderPath;
    private final String schemasFolderPath;
    private final String descriptionsFolderPath;
    private final boolean separatedDefinitions;
    private final GroupBy pathsGroupedBy;
    private final OrderBy definitionsOrderedBy;
    private final Language outputLanguage;
    private final Integer inlineSchemaDepthLevel;

    /**
     * @param swagger the Swagger source
     * @param markupLanguage the markup language which is used to generate the files
     * @param examplesFolderPath examplesFolderPath the path to the folder where the example documents reside
     * @param schemasFolderPath the path to the folder where the schema documents reside
     * @param descriptionsFolderPath the path to the folder where the description documents reside
     * @param separatedDefinitions specified if in addition to the definitions file, also separate definition files for each model definition should be created
     * @param pathsGroupedBy specifies if the paths should be grouped by tags or stay as-is
     * @param definitionsOrderedBy specifies if the definitions should be ordered by natural ordering or stay as-is
     * @param outputLanguage specifies language of labels in output files
     * @param inlineSchemaDepthLevel specifies the max depth for inline object schema display (0 = no inline schemas)
     */
    public Swagger2MarkupConfig(Swagger swagger, MarkupLanguage markupLanguage, String examplesFolderPath,
                                String schemasFolderPath, String descriptionsFolderPath, boolean separatedDefinitions,
                                GroupBy pathsGroupedBy, OrderBy definitionsOrderedBy, Language outputLanguage,
                                Integer inlineSchemaDepthLevel) {
        this.swagger = swagger;
        this.markupLanguage = markupLanguage;
        this.examplesFolderPath = examplesFolderPath;
        this.schemasFolderPath = schemasFolderPath;
        this.descriptionsFolderPath = descriptionsFolderPath;
        this.separatedDefinitions = separatedDefinitions;
        this.pathsGroupedBy = pathsGroupedBy;
        this.definitionsOrderedBy = definitionsOrderedBy;
        this.outputLanguage = outputLanguage;
        this.inlineSchemaDepthLevel = inlineSchemaDepthLevel;
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

    public GroupBy getPathsGroupedBy() {
        return pathsGroupedBy;
    }

    public OrderBy getDefinitionsOrderedBy() {
        return definitionsOrderedBy;
    }

    public Language getOutputLanguage() {
        return outputLanguage;
    }

    public Integer getInlineSchemaDepthLevel() {
        return inlineSchemaDepthLevel;
    }
}
