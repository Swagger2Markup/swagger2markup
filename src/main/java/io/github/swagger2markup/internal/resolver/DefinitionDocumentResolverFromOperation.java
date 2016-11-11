package io.github.swagger2markup.internal.resolver;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

import java.io.File;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Overrides definition document resolver functor for inter-document cross-references from operations files.
 * This implementation adapt the relative paths to definitions files
 */public
class DefinitionDocumentResolverFromOperation extends DefinitionDocumentResolverDefault {

    public DefinitionDocumentResolverFromOperation(MarkupDocBuilder markupDocBuilder,
                                                   Swagger2MarkupConfig config,
                                                   Path outputPath) {
        super(markupDocBuilder, config, outputPath);
    }

    public String apply(String definitionName) {
        String defaultResolver = super.apply(definitionName);

        if (defaultResolver != null && config.isSeparatedOperationsEnabled())
            return defaultString(config.getInterDocumentCrossReferencesPrefix()) + new File("..", defaultResolver).getPath();
        else
            return defaultResolver;
    }
}