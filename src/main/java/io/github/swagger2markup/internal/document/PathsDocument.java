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

import com.google.common.collect.Multimap;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Labels;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.component.PathOperationComponent;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverFromOperation;
import io.github.swagger2markup.internal.resolver.OperationDocumentNameResolver;
import io.github.swagger2markup.internal.resolver.OperationDocumentResolverDefault;
import io.github.swagger2markup.internal.resolver.SecurityDocumentResolver;
import io.github.swagger2markup.internal.utils.PathUtils;
import io.github.swagger2markup.internal.utils.RegexUtils;
import io.github.swagger2markup.internal.utils.TagUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Path;
import io.swagger.models.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.crossReference;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Context;
import static io.github.swagger2markup.spi.PathsDocumentExtension.Position;
import static io.github.swagger2markup.utils.IOUtils.normalizeName;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupComponent<PathsDocument.Parameters> {

    private static final String PATHS_ANCHOR = "paths";
    private final PathOperationComponent pathOperationComponent;
    private final OperationDocumentNameResolver operationDocumentNameResolver;
    private final OperationDocumentResolverDefault operationDocumentResolverDefault;

    public PathsDocument(Swagger2MarkupConverter.Context context) {
        super(context);
        this.pathOperationComponent = new PathOperationComponent(context,
                new DefinitionDocumentResolverFromOperation(context),
                new SecurityDocumentResolver(context));
        this.operationDocumentNameResolver = new OperationDocumentNameResolver(context);
        this.operationDocumentResolverDefault = new OperationDocumentResolverDefault(context);

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
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Create separated operation files is disabled.");
            }
        }
    }

    public static PathsDocument.Parameters parameters(Map<String, Path> paths) {
        return new PathsDocument.Parameters(paths);
    }

    /**
     * Builds the paths MarkupDocument.
     *
     * @return the paths MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, PathsDocument.Parameters params) {
        Map<String, Path> paths = params.paths;
        if (MapUtils.isNotEmpty(paths)) {
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_BEFORE, markupDocBuilder));
            buildPathsTitle(markupDocBuilder);
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_BEGIN, markupDocBuilder));
            buildsPathsSection(markupDocBuilder, paths);
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_END, markupDocBuilder));
            applyPathsDocumentExtension(new Context(Position.DOCUMENT_AFTER, markupDocBuilder));
        }
        return markupDocBuilder;
    }

    /**
     * Builds the paths section. Groups the paths either as-is, by tags or using regex.
     *
     * @param paths the Swagger paths
     */
    private void buildsPathsSection(MarkupDocBuilder markupDocBuilder, Map<String, Path> paths) {
        List<PathOperation> pathOperations = PathUtils.toPathOperationsList(paths, getBasePath(), config.getOperationOrdering());
        if (CollectionUtils.isNotEmpty(pathOperations)) {
            if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
                pathOperations.forEach(operation -> buildOperation(markupDocBuilder, operation));
            } else if (config.getPathsGroupedBy() == GroupBy.TAGS) {
                Validate.notEmpty(context.getSwagger().getTags(), "Tags must not be empty, when operations are grouped by tags");
                // Group operations by tag
                Multimap<String, PathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(pathOperations, config.getOperationOrdering());

                Map<String, Tag> tagsMap = TagUtils.toSortedMap(context.getSwagger().getTags(), config.getTagOrdering());

                tagsMap.forEach((String tagName, Tag tag) -> {
                    markupDocBuilder.sectionTitleWithAnchorLevel2(WordUtils.capitalize(tagName), tagName + "_resource");
                    String description = tag.getDescription();
                    if (StringUtils.isNotBlank(description)) {
                        markupDocBuilder.paragraph(description);
                    }
                    operationsGroupedByTag.get(tagName).forEach(operation -> buildOperation(markupDocBuilder, operation));

                });
            } else if (config.getPathsGroupedBy() == GroupBy.REGEX) {
                Validate.notNull(config.getHeaderPattern(), "Header regex pattern must not be empty when operations are grouped using regex");

                Pattern headerPattern = config.getHeaderPattern();
                Multimap<String, PathOperation> operationsGroupedByRegex = RegexUtils.groupOperationsByRegex(pathOperations, headerPattern);
                Set<String> keys = operationsGroupedByRegex.keySet();
                String[] sortedHeaders = RegexUtils.toSortedArray(keys);

                for (String header : sortedHeaders) {
                    markupDocBuilder.sectionTitleWithAnchorLevel2(WordUtils.capitalize(header), header + "_resource");
                    operationsGroupedByRegex.get(header).forEach(operation -> buildOperation(markupDocBuilder, operation));
                }
            }
        }
    }

    /**
     * Builds the path title depending on the operationsGroupedBy configuration setting.
     */
    private void buildPathsTitle(MarkupDocBuilder markupDocBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            buildPathsTitle(markupDocBuilder, labels.getLabel(Labels.PATHS));
        } else if (config.getPathsGroupedBy() == GroupBy.REGEX) {
            buildPathsTitle(markupDocBuilder, labels.getLabel(Labels.OPERATIONS));
        } else {
            buildPathsTitle(markupDocBuilder, labels.getLabel(Labels.RESOURCES));
        }
    }

    /**
     * Returns the basePath which should be prepended to the relative path
     *
     * @return either the relative or the full path
     */
    private String getBasePath() {
        if (config.isBasePathPrefixEnabled()) {
            return StringUtils.defaultString(context.getSwagger().getBasePath());
        }
        return "";
    }

    private void buildPathsTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.sectionTitleWithAnchorLevel1(title, PATHS_ANCHOR);
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
     * Builds a path operation depending on generation mode.
     *
     * @param operation operation
     */
    private void buildOperation(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        if (config.isSeparatedOperationsEnabled()) {
            MarkupDocBuilder pathDocBuilder = copyMarkupDocBuilder(markupDocBuilder);
            applyPathOperationComponent(pathDocBuilder, operation);
            java.nio.file.Path operationFile = context.getOutputPath().resolve(operationDocumentNameResolver.apply(operation));
            pathDocBuilder.writeToFileWithoutExtension(operationFile, StandardCharsets.UTF_8);
            if (logger.isDebugEnabled()) {
                logger.debug("Separate operation file produced : '{}'", operationFile);
            }
            buildOperationRef(markupDocBuilder, operation);

        } else {
            applyPathOperationComponent(markupDocBuilder, operation);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Operation processed : '{}' (normalized id = '{}')", operation, normalizeName(operation.getId()));
        }
    }

    /**
     * Builds a path operation.
     *
     * @param markupDocBuilder the docbuilder do use for output
     * @param operation        the Swagger Operation
     */
    private void applyPathOperationComponent(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        if (operation != null) {
            pathOperationComponent.apply(markupDocBuilder, PathOperationComponent.parameters(operation));
        }
    }

    /**
     * Builds a cross-reference to a separated operation file
     *
     * @param markupDocBuilder the markupDocBuilder do use for output
     * @param operation        the Swagger Operation
     */
    private void buildOperationRef(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        buildOperationTitle(markupDocBuilder, crossReference(markupDocBuilder, operationDocumentResolverDefault.apply(operation), operation.getId(), operation.getTitle()), "ref-" + operation.getId());
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title  the operation title
     * @param anchor optional anchor (null => auto-generate from title)
     */
    private void buildOperationTitle(MarkupDocBuilder markupDocBuilder, String title, String anchor) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            markupDocBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }

    public static class Parameters {
        private final Map<String, Path> paths;

        public Parameters(Map<String, Path> paths) {
            this.paths = paths;
        }
    }
}
