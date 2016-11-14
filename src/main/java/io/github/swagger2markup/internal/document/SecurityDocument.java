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
import io.github.swagger2markup.internal.component.SecuritySchemeDefinitionComponent;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.collections4.MapUtils;

import java.nio.file.Path;
import java.util.Map;

import static io.github.swagger2markup.internal.Labels.SECURITY;
import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Context;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Position;

/**
 * @author Robert Winkler
 */
public class SecurityDocument extends MarkupDocument {

    private static final String SECURITY_ANCHOR = "securityScheme";
    private final SecuritySchemeDefinitionComponent securitySchemeDefinitionComponent;

    public SecurityDocument(Swagger2MarkupConverter.Context context) {
        this(context, null);

    }

    public SecurityDocument(Swagger2MarkupConverter.Context context, Path outputPath) {
        super(context, outputPath);
        this.securitySchemeDefinitionComponent = new SecuritySchemeDefinitionComponent(context);
    }

    /**
     * Builds the security MarkupDocument.
     *
     * @return the security MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(){
        Map<String, SecuritySchemeDefinition> definitions = globalContext.getSwagger().getSecurityDefinitions();
        if (MapUtils.isNotEmpty(definitions)) {
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_BEFORE, this.markupDocBuilder));
            buildSecurityTitle(labels.getString(SECURITY));
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
            buildSecuritySchemeDefinitionsSection(definitions);
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_AFTER, this.markupDocBuilder));
        }
        return markupDocBuilder;
    }

    private void buildSecurityTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, SECURITY_ANCHOR);
    }

    private void buildSecuritySchemeDefinitionsSection(Map<String, SecuritySchemeDefinition> securitySchemes) {
        Map<String, SecuritySchemeDefinition> securitySchemeNames = toSortedMap(securitySchemes, null); // TODO : provide a dedicated ordering configuration for security schemes
        securitySchemeNames.forEach((String securitySchemeName, SecuritySchemeDefinition securityScheme) ->
                securitySchemeDefinitionComponent.apply(markupDocBuilder, SecuritySchemeDefinitionComponent.parameters(
                        securitySchemeName, securityScheme, 2
                )));
    }

    /**
     * Apply extension context to all SecurityContentExtension
     *
     * @param context context
     */
    private void applySecurityDocumentExtension(Context context) {
        extensionRegistry.getSecurityDocumentExtensions().forEach(extension -> extension.apply(context));
    }
}
