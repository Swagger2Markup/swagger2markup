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
package io.github.swagger2markup.internal.component;


import ch.netzwerg.paleo.StringColumn;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.MarkupComponent;
import io.github.swagger2markup.spi.SecurityDocumentExtension;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.lang3.Validate;

import java.util.Map;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.Labels.*;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static io.github.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SecuritySchemeDefinitionComponent extends MarkupComponent<SecuritySchemeDefinitionComponent.Parameters> {

    private final TableComponent tableComponent;

    public SecuritySchemeDefinitionComponent(Swagger2MarkupConverter.Context context) {
        super(context);
        this.tableComponent = new TableComponent(context);
    }

    public static SecuritySchemeDefinitionComponent.Parameters parameters(String securitySchemeDefinitionName,
                                                                          SecuritySchemeDefinition securitySchemeDefinition,
                                                                          int titleLevel) {
        return new SecuritySchemeDefinitionComponent.Parameters(securitySchemeDefinitionName, securitySchemeDefinition, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        String securitySchemeDefinitionName = params.securitySchemeDefinitionName;
        SecuritySchemeDefinition securitySchemeDefinition = params.securitySchemeDefinition;
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_BEFORE, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        markupDocBuilder.sectionTitleWithAnchorLevel(params.titleLevel, securitySchemeDefinitionName);
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_BEGIN, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        String description = securitySchemeDefinition.getDescription();
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(markupDescription(config.getSwaggerMarkupLanguage(), markupDocBuilder, description));
        }
        buildSecurityScheme(markupDocBuilder, securitySchemeDefinition);
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_END, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_AFTER, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        return markupDocBuilder;
    }

    private MarkupDocBuilder buildSecurityScheme(MarkupDocBuilder markupDocBuilder, SecuritySchemeDefinition securityScheme) {
        String type = securityScheme.getType();
        MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder(markupDocBuilder);

        paragraphBuilder.italicText(labels.getLabel(TYPE)).textLine(COLON + type);

        if (securityScheme instanceof ApiKeyAuthDefinition) {
            paragraphBuilder.italicText(labels.getLabel(NAME)).textLine(COLON + ((ApiKeyAuthDefinition) securityScheme).getName());
            paragraphBuilder.italicText(labels.getLabel(IN)).textLine(COLON + ((ApiKeyAuthDefinition) securityScheme).getIn());

            return markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        } else if (securityScheme instanceof OAuth2Definition) {
            OAuth2Definition oauth2Scheme = (OAuth2Definition) securityScheme;
            String flow = oauth2Scheme.getFlow();
            paragraphBuilder.italicText(labels.getLabel(FLOW)).textLine(COLON + flow);
            if (isNotBlank(oauth2Scheme.getAuthorizationUrl())) {
                paragraphBuilder.italicText(labels.getLabel(AUTHORIZATION_URL)).textLine(COLON + oauth2Scheme.getAuthorizationUrl());
            }
            if (isNotBlank(oauth2Scheme.getTokenUrl())) {
                paragraphBuilder.italicText(labels.getLabel(TOKEN_URL)).textLine(COLON + oauth2Scheme.getTokenUrl());
            }
            StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(NAME_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "3")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(DESCRIPTION_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "17")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");

            if (oauth2Scheme.getScopes() != null) {
                for (Map.Entry<String, String> scope : oauth2Scheme.getScopes().entrySet()) {
                    nameColumnBuilder.add(scope.getKey());
                    descriptionColumnBuilder.add(scope.getValue());
                }
            }

            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
            return tableComponent.apply(markupDocBuilder, TableComponent.parameters(nameColumnBuilder.build(),
                    descriptionColumnBuilder.build()));
        } else {
            return markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
    }

    /**
     * Apply extension context to all SecurityContentExtension
     *
     * @param context context
     */
    private void applySecurityDocumentExtension(SecurityDocumentExtension.Context context) {
        extensionRegistry.getSecurityDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final String securitySchemeDefinitionName;
        private final SecuritySchemeDefinition securitySchemeDefinition;
        private final int titleLevel;

        public Parameters(String securitySchemeDefinitionName,
                          SecuritySchemeDefinition securitySchemeDefinition,
                          int titleLevel) {
            this.securitySchemeDefinitionName = Validate.notBlank(securitySchemeDefinitionName, "SecuritySchemeDefinitionName must not be empty");
            this.securitySchemeDefinition = Validate.notNull(securitySchemeDefinition, "SecuritySchemeDefinition must not be null");
            this.titleLevel = titleLevel;
        }
    }
}
