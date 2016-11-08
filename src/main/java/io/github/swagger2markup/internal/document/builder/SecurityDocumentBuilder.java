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

import ch.netzwerg.paleo.StringColumn;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.document.MarkupDocument;
import io.github.swagger2markup.internal.utils.Table;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.spi.SecurityDocumentExtension;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.collections4.MapUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.ResourceBundle;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Context;
import static io.github.swagger2markup.spi.SecurityDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class SecurityDocumentBuilder extends MarkupDocumentBuilder {

    private static final String SECURITY_ANCHOR = "securityScheme";
    private final String SECURITY;
    private final String TYPE;
    private final String NAME;
    private final String IN;
    private final String FLOW;
    private final String AUTHORIZATION_URL;
    private final String TOKEN_URL;

    public SecurityDocumentBuilder(Swagger2MarkupConverter.Context context, Swagger2MarkupExtensionRegistry extensionRegistry, Path outputPath) {
        super(context, extensionRegistry, outputPath);

        ResourceBundle labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
        SECURITY = labels.getString("security");
        TYPE = labels.getString("security_type");
        NAME = labels.getString("security_name");
        IN = labels.getString("security_in");
        FLOW = labels.getString("security_flow");
        AUTHORIZATION_URL = labels.getString("security_authorizationUrl");
        TOKEN_URL = labels.getString("security_tokenUrl");
    }

    /**
     * Builds the security MarkupDocument.
     *
     * @return the security MarkupDocument
     */
    @Override
    public MarkupDocument build(){
        Map<String, SecuritySchemeDefinition> definitions = globalContext.getSwagger().getSecurityDefinitions();
        if (MapUtils.isNotEmpty(definitions)) {
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_BEFORE, this.markupDocBuilder));
            buildSecurityTitle(SECURITY);
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_BEGIN, this.markupDocBuilder));
            buildSecuritySchemeDefinitionsSection(definitions);
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_END, this.markupDocBuilder));
            applySecurityDocumentExtension(new Context(Position.DOCUMENT_AFTER, this.markupDocBuilder));
        }
        return new MarkupDocument(markupDocBuilder);
    }

    private void buildSecurityTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, SECURITY_ANCHOR);
    }

    private void buildSecuritySchemeDefinitionsSection(Map<String, SecuritySchemeDefinition> securitySchemes) {
        Map<String, SecuritySchemeDefinition> securitySchemeNames = toSortedMap(securitySchemes, null); // TODO : provide a dedicated ordering configuration for security schemes
        securitySchemeNames.forEach((String securitySchemeName, SecuritySchemeDefinition securityScheme) -> {
            applySecurityDocumentExtension(new Context(Position.SECURITY_SCHEME_BEFORE, markupDocBuilder, securitySchemeName, securityScheme));
            buildSecuritySchemeDefinitionTitle(securitySchemeName);
            applySecurityDocumentExtension(new Context(Position.SECURITY_SCHEME_BEGIN, markupDocBuilder, securitySchemeName, securityScheme));
            buildDescriptionParagraph(securityScheme.getDescription(), this.markupDocBuilder);
            buildSecurityScheme(securityScheme);
            applySecurityDocumentExtension(new Context(Position.SECURITY_SCHEME_END, markupDocBuilder, securitySchemeName, securityScheme));
            applySecurityDocumentExtension(new Context(Position.SECURITY_SCHEME_AFTER, markupDocBuilder, securitySchemeName, securityScheme));
        });
    }

    private MarkupDocBuilder buildSecuritySchemeDefinitionTitle(String securitySchemeName) {
        return markupDocBuilder.sectionTitleWithAnchorLevel2(securitySchemeName);
    }

    private void buildSecurityScheme(SecuritySchemeDefinition securityScheme) {
        String type = securityScheme.getType();
        MarkupDocBuilder paragraph = copyMarkupDocBuilder();
        
        paragraph.italicText(TYPE).textLine(COLON + type);
        
        if (securityScheme instanceof ApiKeyAuthDefinition) {
            paragraph.italicText(NAME).textLine(COLON + ((ApiKeyAuthDefinition) securityScheme).getName());
            paragraph.italicText(IN).textLine(COLON + ((ApiKeyAuthDefinition) securityScheme).getIn());
            
            markupDocBuilder.paragraph(paragraph.toString(), true);
        } else if (securityScheme instanceof OAuth2Definition) {
            OAuth2Definition oauth2Scheme = (OAuth2Definition) securityScheme;
            String flow = oauth2Scheme.getFlow();
            paragraph.italicText(FLOW).textLine(COLON + flow);
            if (isNotBlank(oauth2Scheme.getAuthorizationUrl())) {
                paragraph.italicText(AUTHORIZATION_URL).textLine(COLON + oauth2Scheme.getAuthorizationUrl());
            }
            if (isNotBlank(oauth2Scheme.getTokenUrl())) {
                paragraph.italicText(TOKEN_URL).textLine(COLON + oauth2Scheme.getTokenUrl());
            }
            StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of(NAME_COLUMN))
                    .putMetaData(Table.WIDTH_RATIO, "3")
                    .putMetaData(Table.HEADER_COLUMN, "true");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(DESCRIPTION_COLUMN))
                    .putMetaData(Table.WIDTH_RATIO, "17")
                    .putMetaData(Table.HEADER_COLUMN, "true");

            for (Map.Entry<String, String> scope : oauth2Scheme.getScopes().entrySet()) {
                nameColumnBuilder.add(scope.getKey());
                descriptionColumnBuilder.add(scope.getValue());
            }

            Table table = Table.ofAll(
                    nameColumnBuilder.build(),
                    descriptionColumnBuilder.build());
            
            markupDocBuilder.paragraph(paragraph.toString(), true);
            markupDocBuilder.tableWithColumnSpecs(table.getColumnSpecs(), table.getCells());
        } else {
            markupDocBuilder.paragraph(paragraph.toString(), true);
        }
    }

    /**
     * Apply extension context to all SecurityContentExtension
     *
     * @param context context
     */
    private void applySecurityDocumentExtension(Context context) {
        for (SecurityDocumentExtension extension : extensionRegistry.getSecurityDocumentExtensions()) {
            extension.apply(context);
        }
    }
}
