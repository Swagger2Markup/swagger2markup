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
package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.Labels;
import io.github.swagger2markup.internal.component.DefinitionComponent;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverDefault;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverFromDefinition;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.Model;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static io.github.swagger2markup.spi.DefinitionsDocumentExtension.Context;
import static io.github.swagger2markup.spi.DefinitionsDocumentExtension.Position;
import static io.github.swagger2markup.utils.IOUtils.normalizeName;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class DefinitionsDocument extends MarkupDocument {
    
    private static final String DEFINITIONS_ANCHOR = "definitions";

    private static final List<String> IGNORED_DEFINITIONS = Collections.singletonList("Void");
    private final DefinitionComponent definitionComponent;

    public DefinitionsDocument(Swagger2MarkupConverter.Context context) {
        this(context, null);
    }

    public DefinitionsDocument(Swagger2MarkupConverter.Context context, Path outputPath) {
        super(context, outputPath);
        if (config.isSeparatedDefinitionsEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated definition files is enabled.");
            }
            Validate.notNull(outputPath, "Output directory is required for separated definition files!");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated definition files is disabled.");
            }
        }
        this.definitionComponent = new DefinitionComponent(context,
                new DefinitionDocumentResolverFromDefinition(markupDocBuilder, config, outputPath));
    }

    /**
     * Builds the definitions MarkupDocument.
     *
     * @return the definitions MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply() {
        if (MapUtils.isNotEmpty(globalContext.getSwagger().getDefinitions())) {
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_BEFORE, this.markupDocBuilder));
            buildDefinitionsTitle(labels.getString(Labels.DEFINITIONS));
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
            buildDefinitionsSection();
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_AFTER, this.markupDocBuilder));
        }
        return markupDocBuilder;
    }

    private void buildDefinitionsTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, DEFINITIONS_ANCHOR);
    }

    private void buildDefinitionsSection() {
        Map<String, Model> sortedMap = toSortedMap(globalContext.getSwagger().getDefinitions(), config.getDefinitionOrdering());
        sortedMap.forEach((String definitionName, Model model) -> {
            if(isNotBlank(definitionName)
                    && checkThatDefinitionIsNotInIgnoreList(definitionName)){
                buildDefinition(definitionName, model);
            }
        });
    }

    /**
     * Apply extension context to all DefinitionsContentExtension
     *
     * @param context context
     */
    private void applyDefinitionsDocumentExtension(Context context) {
        extensionRegistry.getDefinitionsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    /**
     * Create the definition filename depending on the generation mode
     *
     * @param definitionName definition name
     * @return definition filename
     */
    private String resolveDefinitionDocument(String definitionName) {
        if (config.isSeparatedDefinitionsEnabled())
            return new File(config.getSeparatedDefinitionsFolder(), markupDocBuilder.addFileExtension(normalizeName(definitionName))).getPath();
        else
            return markupDocBuilder.addFileExtension(config.getDefinitionsDocument());
    }

    /**
     * Generate definition files depending on the generation mode
     *
     * @param definitionName definition name to process
     * @param model          definition model to process
     */
    private void buildDefinition(String definitionName, Model model) {
        if (logger.isInfoEnabled()) {
            logger.info("Definition processed : '{}'", definitionName);
        }
        if (config.isSeparatedDefinitionsEnabled()) {
            MarkupDocBuilder defDocBuilder = copyMarkupDocBuilder();
            buildDefinition(defDocBuilder, definitionName, model);
            Path definitionFile = outputPath.resolve(resolveDefinitionDocument(definitionName));
            defDocBuilder.writeToFileWithoutExtension(definitionFile, StandardCharsets.UTF_8);
            if (logger.isInfoEnabled()) {
                logger.info("Separate definition file produced : '{}'", definitionFile);
            }

            definitionRef(this.markupDocBuilder, definitionName);

        } else {
            buildDefinition(this.markupDocBuilder, definitionName, model);
        }
    }

    /**
     * Checks that the definition is not in the list of ignored definitions.
     *
     * @param definitionName the name of the definition
     * @return true if the definition can be processed
     */
    private boolean checkThatDefinitionIsNotInIgnoreList(String definitionName) {
        return !IGNORED_DEFINITIONS.contains(definitionName);
    }

    /**
     * Builds a concrete definition
     *
     * @param markupDocBuilder  the markupDocBuilder do use for output
     * @param definitionName the name of the definition
     * @param model          the Swagger Model of the definition
     */
    private void buildDefinition(MarkupDocBuilder markupDocBuilder, String definitionName, Model model) {
        definitionComponent.apply(markupDocBuilder, DefinitionComponent.parameters(
                definitionName,
                model,
                2));
    }

    /**
     * Builds a cross-reference to a separated definition file.
     *
     * @param definitionName definition name to target
     * @param docBuilder     the docbuilder do use for output
     */
    private void definitionRef(MarkupDocBuilder docBuilder, String definitionName) {
        buildDefinitionTitle(copyMarkupDocBuilder().crossReference(new DefinitionDocumentResolverDefault(markupDocBuilder, config, outputPath).apply(definitionName), definitionName, definitionName).toString(), "ref-" + definitionName, docBuilder);
    }

    /**
     * Builds definition title
     *
     * @param title      definition title
     * @param anchor     optional anchor (null => auto-generate from title)
     * @param docBuilder the docbuilder do use for output
     */
    private void buildDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.sectionTitleWithAnchorLevel2(title, anchor);
    }

}
