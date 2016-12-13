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
import io.github.swagger2markup.spi.MarkupComponent;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static io.github.swagger2markup.Labels.SECURITY;
import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Context;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Position;

/**
 * @author Robert Winkler
 */
public class SecurityDocument extends MarkupComponent<SecurityDocument.Parameters> {

    private static final String SECURITY_ANCHOR = "securityScheme";
    private final SecuritySchemeDefinitionComponent securitySchemeDefinitionComponent;

    public SecurityDocument(Swagger2MarkupConverter.Context context) {
        super(context);
        this.securitySchemeDefinitionComponent = new SecuritySchemeDefinitionComponent(context);
    }

    public static SecurityDocument.Parameters parameters(Map<String, SecuritySchemeDefinition> securitySchemeDefinitions) {
        return new SecurityDocument.Parameters(securitySchemeDefinitions);
    }

    /**
     * Builds the security MarkupDocument.
     *
     * @return the security MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, SecurityDocument.Parameters params) {
        Map<String, SecuritySchemeDefinition> definitions = params.securitySchemeDefinitions;
        if (MapUtils.isNotEmpty(definitions)) {
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_BEFORE, markupDocBuilder));
            buildSecurityTitle(markupDocBuilder, labels.getLabel(SECURITY));
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_BEGIN, markupDocBuilder));
            buildSecuritySchemeDefinitionsSection(markupDocBuilder, definitions);
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_END, markupDocBuilder));
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_AFTER, markupDocBuilder));
        }
        return markupDocBuilder;
    }

    private void buildSecurityTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.sectionTitleWithAnchorLevel1(title, SECURITY_ANCHOR);
    }

    private void buildSecuritySchemeDefinitionsSection(MarkupDocBuilder markupDocBuilder, Map<String, SecuritySchemeDefinition> securitySchemes) {
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

    public static class Parameters {
        private final Map<String, SecuritySchemeDefinition> securitySchemeDefinitions;

        public Parameters(Map<String, SecuritySchemeDefinition> securitySchemeDefinitions) {
            this.securitySchemeDefinitions = securitySchemeDefinitions;
        }
    }
}
