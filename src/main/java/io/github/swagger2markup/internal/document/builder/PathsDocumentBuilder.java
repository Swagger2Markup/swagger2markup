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
package io.github.swagger2markup.internal.document.builder;

import com.google.common.collect.Multimap;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.component.MarkupComponent;
import io.github.swagger2markup.internal.component.PathOperationComponent;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolver;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverFromOperation;
import io.github.swagger2markup.internal.resolver.SecurityDocumentResolver;
import io.github.swagger2markup.internal.utils.PathUtils;
import io.github.swagger2markup.internal.utils.TagUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.github.swagger2markup.spi.PathsDocumentExtension.Context;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Position;
import static io.github.swagger2markup.utils.IOUtils.normalizeName;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * @author Robert Winkler
 */
public class PathsDocumentBuilder extends MarkupDocumentBuilder {

    private final String PATHS;
    private final String RESOURCES;


    private static final String PATHS_ANCHOR = "paths";

    private final DefinitionDocumentResolver definitionDocumentResolver;

    public PathsDocumentBuilder(Swagger2MarkupConverter.Context globalContext, Swagger2MarkupExtensionRegistry extensionRegistry, java.nio.file.Path outputPath) {
        super(globalContext, extensionRegistry, outputPath);

        definitionDocumentResolver = new DefinitionDocumentResolverFromOperation(markupDocBuilder, config, outputPath);

        PATHS = labels.getString("paths");
        RESOURCES = labels.getString("resources");

        if (config.isGeneratedExamplesEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Generate examples is enabled.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Generate examples is disabled.");
            }
        }

        if (config.isSeparatedOperationsEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is enabled.");
            }
            Validate.notNull(outputPath, "Output directory is required for separated operation files!");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is disabled.");
            }
        }
    }

    /**
     * Builds the paths MarkupDocument.
     *
     * @return the paths MarkupDocument
     */
    @Override
    public MarkupDocument build() {
        Map<String, Path> paths = globalContext.getSwagger().getPaths();
        if (MapUtils.isNotEmpty(paths)) {
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_BEFORE, this.markupDocBuilder));
            buildPathsTitle();
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
            buildsPathsSection(paths);
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_AFTER, this.markupDocBuilder));
        }
        return new MarkupDocument(markupDocBuilder);
    }

    /**
     * Builds the paths section. Groups the paths either as-is or by tags.
     *
     * @param paths the Swagger paths
     */
    private void buildsPathsSection(Map<String, Path> paths) {
        List<PathOperation> pathOperations = PathUtils.toPathOperationsList(paths, getBasePath(), config.getOperationOrdering());
        if (CollectionUtils.isNotEmpty(pathOperations)) {
            if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
                pathOperations.forEach(this::buildOperation);
            } else {
                Validate.notEmpty(globalContext.getSwagger().getTags(), "Tags must not be empty, when operations are grouped by tags");
                // Group operations by tag
                Multimap<String, PathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(pathOperations, config.getOperationOrdering());

                Map<String, Tag> tagsMap = TagUtils.toSortedMap(globalContext.getSwagger().getTags(), config.getTagOrdering());

                tagsMap.forEach((String tagName, Tag tag) -> {
                    markupDocBuilder.sectionTitleWithAnchorLevel2(WordUtils.capitalize(tagName), tagName + "_resource");
                    String description = tag.getDescription();
                    if(StringUtils.isNotBlank(description)){
                        markupDocBuilder.paragraph(description);
                    }
                    operationsGroupedByTag.get(tagName).forEach(this::buildOperation);

                });
            }
        }
    }

    /**
     * Builds the path title depending on the operationsGroupedBy configuration setting.
     */
    private void buildPathsTitle() {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            buildPathsTitle(PATHS);
        } else {
            buildPathsTitle(RESOURCES);
        }
    }

    /**
     * Returns the basePath which should be prepended to the relative path
     *
     * @return either the relative or the full path
     */
    private String getBasePath() {
        if(config.isBasePathPrefixEnabled()){
            return StringUtils.defaultString(globalContext.getSwagger().getBasePath());
        }
        return "";
    }

    private void buildPathsTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, PATHS_ANCHOR);
    }

    /**
     * Apply extension context to all OperationsContentExtension.
     *
     * @param context context
     */
    private void applyPathsDocumentExtension(Context context) {
        extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    /**
     * Create the operation filename depending on the generation mode
     *
     * @param operation operation
     * @return operation filename
     */
    private String resolveOperationDocument(PathOperation operation) {
        if (config.isSeparatedOperationsEnabled())
            return new File(config.getSeparatedOperationsFolder(), this.markupDocBuilder.addFileExtension(normalizeName(operation.getId()))).getPath();
        else
            return this.markupDocBuilder.addFileExtension(config.getPathsDocument());
    }

    /**
     * Builds a path operation depending on generation mode.
     *
     * @param operation operation
     */
    private void buildOperation(PathOperation operation) {
        if (config.isSeparatedOperationsEnabled()) {
            MarkupDocBuilder pathDocBuilder = copyMarkupDocBuilder();
            buildOperation(operation, pathDocBuilder);
            java.nio.file.Path operationFile = outputPath.resolve(resolveOperationDocument(operation));
            pathDocBuilder.writeToFileWithoutExtension(operationFile, StandardCharsets.UTF_8);
            if (logger.isInfoEnabled()) {
                logger.info("Separate operation file produced : '{}'", operationFile);
            }

            buildOperationRef(operation, this.markupDocBuilder);

        } else {
            buildOperation(operation, this.markupDocBuilder);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Operation processed : '{}' (normalized id = '{}')", operation, normalizeName(operation.getId()));
        }
    }

    /**
     * Builds a path operation.
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildOperation(PathOperation operation, MarkupDocBuilder docBuilder) {
        if (operation != null) {
            Swagger swagger = globalContext.getSwagger();
            new PathOperationComponent(new MarkupComponent.Context(config ,docBuilder, extensionRegistry),
                    operation,
                    swagger.getDefinitions(),
                    swagger.getSecurityDefinitions(),
                    definitionDocumentResolver,
                    new SecurityDocumentResolver(docBuilder, config, outputPath)
                    ).render();
        }
    }

    /**
     * Builds a cross-reference to a separated operation file
     *
     * @param operation  the Swagger Operation
     * @param docBuilder the docbuilder do use for output
     */
    private void buildOperationRef(PathOperation operation, MarkupDocBuilder docBuilder) {
        String document;
        if (!config.isInterDocumentCrossReferencesEnabled() || outputPath == null)
            document = null;
        else if (config.isSeparatedOperationsEnabled())
            document = defaultString(config.getInterDocumentCrossReferencesPrefix()) + resolveOperationDocument(operation);
        else
            document = defaultString(config.getInterDocumentCrossReferencesPrefix()) + resolveOperationDocument(operation);

        buildOperationTitle(copyMarkupDocBuilder().crossReference(document, operation.getId(), operation.getTitle()).toString(), "ref-" + operation.getId(), docBuilder);
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title      the operation title
     * @param anchor     optional anchor (null => auto-generate from title)
     * @param docBuilder the MarkupDocBuilder to use
     */
    private void buildOperationTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            docBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            docBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }
}
