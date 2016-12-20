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

import io.github.swagger2markup.Labels;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.component.DefinitionComponent;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentNameResolver;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverDefault;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverFromDefinition;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Model;
import org.apache.commons.collections4.MapUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.crossReference;
import static io.github.swagger2markup.spi.DefinitionsDocumentExtension.Context;
import static io.github.swagger2markup.spi.DefinitionsDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class DefinitionsDocument extends MarkupComponent<DefinitionsDocument.Parameters> {

    private static final String DEFINITIONS_ANCHOR = "definitions";

    private static final List<String> IGNORED_DEFINITIONS = Collections.singletonList("Void");
    private final DefinitionComponent definitionComponent;
    private final DefinitionDocumentResolverDefault definitionDocumentResolverDefault;
    private final DefinitionDocumentNameResolver definitionDocumentNameResolver;

    public DefinitionsDocument(Swagger2MarkupConverter.Context context) {
        super(context);
        if (config.isSeparatedDefinitionsEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated definition files is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated definition files is disabled.");
            }
        }
        this.definitionDocumentNameResolver = new DefinitionDocumentNameResolver(context);
        this.definitionComponent = new DefinitionComponent(context, new DefinitionDocumentResolverFromDefinition(context));
        this.definitionDocumentResolverDefault = new DefinitionDocumentResolverDefault(context);
    }

    public static DefinitionsDocument.Parameters parameters(Map<String, Model> definitions) {
        return new DefinitionsDocument.Parameters(definitions);
    }

    /**
     * Builds the definitions MarkupDocument.
     *
     * @return the definitions MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, DefinitionsDocument.Parameters params) {
        Map<String, Model> definitions = params.definitions;
        if (MapUtils.isNotEmpty(definitions)) {
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_BEFORE, markupDocBuilder));
            buildDefinitionsTitle(markupDocBuilder, labels.getLabel(Labels.DEFINITIONS));
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_BEGIN, markupDocBuilder));
            buildDefinitionsSection(markupDocBuilder, definitions);
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_END, markupDocBuilder));
            applyDefinitionsDocumentExtension(new Context(Position.DOCUMENT_AFTER, markupDocBuilder));
        }
        return markupDocBuilder;
    }

    private void buildDefinitionsTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.sectionTitleWithAnchorLevel1(title, DEFINITIONS_ANCHOR);
    }

    private void buildDefinitionsSection(MarkupDocBuilder markupDocBuilder, Map<String, Model> definitions) {
        Map<String, Model> sortedMap = toSortedMap(definitions, config.getDefinitionOrdering());
        sortedMap.forEach((String definitionName, Model model) -> {
            if (isNotBlank(definitionName)
                    && checkThatDefinitionIsNotInIgnoreList(definitionName)) {
                buildDefinition(markupDocBuilder, definitionName, model);
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
     * Generate definition files depending on the generation mode
     *
     * @param definitionName definition name to process
     * @param model          definition model to process
     */
    private void buildDefinition(MarkupDocBuilder markupDocBuilder, String definitionName, Model model) {
        if (logger.isDebugEnabled()) {
            logger.debug("Definition processed : '{}'", definitionName);
        }
        if (config.isSeparatedDefinitionsEnabled()) {
            MarkupDocBuilder defDocBuilder = copyMarkupDocBuilder(markupDocBuilder);
            applyDefinitionComponent(defDocBuilder, definitionName, model);
            Path definitionFile = context.getOutputPath().resolve(definitionDocumentNameResolver.apply(definitionName));
            defDocBuilder.writeToFileWithoutExtension(definitionFile, StandardCharsets.UTF_8);
            if (logger.isDebugEnabled()) {
                logger.debug("Separate definition file produced : '{}'", definitionFile);
            }

            definitionRef(markupDocBuilder, definitionName);

        } else {
            applyDefinitionComponent(markupDocBuilder, definitionName, model);
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
     * @param markupDocBuilder the markupDocBuilder do use for output
     * @param definitionName   the name of the definition
     * @param model            the Swagger Model of the definition
     */
    private void applyDefinitionComponent(MarkupDocBuilder markupDocBuilder, String definitionName, Model model) {
        definitionComponent.apply(markupDocBuilder, DefinitionComponent.parameters(
                definitionName,
                model,
                2));
    }

    /**
     * Builds a cross-reference to a separated definition file.
     *
     * @param definitionName definition name to target
     */
    private void definitionRef(MarkupDocBuilder markupDocBuilder, String definitionName) {
        buildDefinitionTitle(markupDocBuilder, crossReference(markupDocBuilder, definitionDocumentResolverDefault.apply(definitionName), definitionName, definitionName), "ref-" + definitionName);
    }

    /**
     * Builds definition title
     *
     * @param markupDocBuilder the markupDocBuilder do use for output
     * @param title            definition title
     * @param anchor           optional anchor (null => auto-generate from title)
     */
    private void buildDefinitionTitle(MarkupDocBuilder markupDocBuilder, String title, String anchor) {
        markupDocBuilder.sectionTitleWithAnchorLevel2(title, anchor);
    }

    public static class Parameters {
        private final Map<String, Model> definitions;

        public Parameters(Map<String, Model> definitions) {
            this.definitions = definitions;
        }
    }

}
