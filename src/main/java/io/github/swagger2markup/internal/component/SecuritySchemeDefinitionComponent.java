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
import io.github.swagger2markup.internal.utils.Table;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.SecurityDocumentExtension;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.lang3.Validate;

import java.util.Map;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.internal.component.Labels.*;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SecuritySchemeDefinitionComponent extends MarkupComponent {

    private final String securitySchemeDefinitionName;
    private final SecuritySchemeDefinition securitySchemeDefinition;
    private final int titleLevel;

    public SecuritySchemeDefinitionComponent(Context context,
                                             String securitySchemeDefinitionName,
                                             SecuritySchemeDefinition securitySchemeDefinition,
                                             int titleLevel){
        super(context);
        this.securitySchemeDefinitionName = Validate.notBlank(securitySchemeDefinitionName, "SecuritySchemeDefinitionName must not be empty");
        this.securitySchemeDefinition = Validate.notNull(securitySchemeDefinition, "SecuritySchemeDefinition must not be null");
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_BEFORE, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        markupDocBuilder.sectionTitleWithAnchorLevel(titleLevel, securitySchemeDefinitionName);
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_BEGIN, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        String description = securitySchemeDefinition.getDescription();
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(swaggerMarkupDescription(description));
        }
        buildSecurityScheme(securitySchemeDefinition);
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_END, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));
        applySecurityDocumentExtension(new SecurityDocumentExtension.Context(Position.SECURITY_SCHEME_AFTER, markupDocBuilder, securitySchemeDefinitionName, securitySchemeDefinition));

        return markupDocBuilder;
    }

    private void buildSecurityScheme(SecuritySchemeDefinition securityScheme) {
        String type = securityScheme.getType();
        MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder();

        paragraphBuilder.italicText(labels.getString(TYPE)).textLine(COLON + type);

        if (securityScheme instanceof ApiKeyAuthDefinition) {
            paragraphBuilder.italicText(labels.getString(NAME)).textLine(COLON + ((ApiKeyAuthDefinition) securityScheme).getName());
            paragraphBuilder.italicText(labels.getString(IN)).textLine(COLON + ((ApiKeyAuthDefinition) securityScheme).getIn());

            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        } else if (securityScheme instanceof OAuth2Definition) {
            OAuth2Definition oauth2Scheme = (OAuth2Definition) securityScheme;
            String flow = oauth2Scheme.getFlow();
            paragraphBuilder.italicText(labels.getString(FLOW)).textLine(COLON + flow);
            if (isNotBlank(oauth2Scheme.getAuthorizationUrl())) {
                paragraphBuilder.italicText(labels.getString(AUTHORIZATION_URL)).textLine(COLON + oauth2Scheme.getAuthorizationUrl());
            }
            if (isNotBlank(oauth2Scheme.getTokenUrl())) {
                paragraphBuilder.italicText(labels.getString(TOKEN_URL)).textLine(COLON + oauth2Scheme.getTokenUrl());
            }
            StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getString(NAME_COLUMN)))
                    .putMetaData(Table.WIDTH_RATIO, "3")
                    .putMetaData(Table.HEADER_COLUMN, "true");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getString(DESCRIPTION_COLUMN)))
                    .putMetaData(Table.WIDTH_RATIO, "17")
                    .putMetaData(Table.HEADER_COLUMN, "true");

            if(oauth2Scheme.getScopes() != null) {
                for (Map.Entry<String, String> scope : oauth2Scheme.getScopes().entrySet()) {
                    nameColumnBuilder.add(scope.getKey());
                    descriptionColumnBuilder.add(scope.getValue());
                }
            }

            Table table = Table.ofAll(
                    nameColumnBuilder.build(),
                    descriptionColumnBuilder.build());

            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
            markupDocBuilder.tableWithColumnSpecs(table.getColumnSpecs(), table.getCells());
        } else {
            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
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
}
