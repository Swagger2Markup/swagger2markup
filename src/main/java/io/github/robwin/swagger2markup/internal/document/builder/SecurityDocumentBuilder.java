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
package io.github.robwin.swagger2markup.internal.document.builder;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupTableColumn;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.internal.document.MarkupDocument;
import io.github.robwin.swagger2markup.spi.SecurityDocumentExtension;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.*;

import static io.github.robwin.swagger2markup.spi.SecurityDocumentExtension.Context;
import static io.github.robwin.swagger2markup.spi.SecurityDocumentExtension.Position;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Robert Winkler
 */
public class SecurityDocumentBuilder extends MarkupDocumentBuilder {

    private static final String SECURITY_ANCHOR = "security";
    private final String SECURITY;
    private final String TYPE;
    private final String NAME;
    private final String IN;
    private final String FLOW;
    private final String AUTHORIZATION_URL;
    private final String TOKEN_URL;

    public SecurityDocumentBuilder(Swagger2MarkupConverter.Context context, Path outputPath) {
        super(context, outputPath);

        ResourceBundle labels = ResourceBundle.getBundle("io/github/robwin/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
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
        }
        return new MarkupDocument(markupDocBuilder);
    }

    private void buildSecurityTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, SECURITY_ANCHOR);

    }

    private void buildSecuritySchemeDefinitionsSection(Map<String, SecuritySchemeDefinition> definitions) {
        for (Map.Entry<String, SecuritySchemeDefinition> entry : definitions.entrySet()) {
            String definitionName = entry.getKey();
            SecuritySchemeDefinition definition = entry.getValue();
            buildSecuritySchemeDefinitionTitle(definitionName);
            applySecurityDocumentExtension(new Context(Position.DEFINITION_BEGIN, markupDocBuilder, definitionName, definition));
            buildDescriptionParagraph(definition.getDescription());
            buildSecurityScheme(definition);
            applySecurityDocumentExtension(new Context(Position.DEFINITION_BEGIN, markupDocBuilder, definitionName, definition));
        }
    }

    private void buildDescriptionParagraph(String description) {
        if(StringUtils.isNotBlank(description)) {
            markupDocBuilder.paragraph(description);
        }
    }

    private MarkupDocBuilder buildSecuritySchemeDefinitionTitle(String definitionName) {
        return markupDocBuilder.sectionTitleLevel2(definitionName);
    }

    private void buildSecurityScheme(SecuritySchemeDefinition securityScheme) {
        String type = securityScheme.getType();
        markupDocBuilder.textLine(TYPE + " : " + type);
        if (securityScheme instanceof ApiKeyAuthDefinition) {
            markupDocBuilder.textLine(NAME + " : " + ((ApiKeyAuthDefinition) securityScheme).getName());
            markupDocBuilder.textLine(IN + " : " + ((ApiKeyAuthDefinition) securityScheme).getIn());
        } else if (securityScheme instanceof OAuth2Definition) {
            OAuth2Definition oauth2Scheme = (OAuth2Definition) securityScheme;
            String flow = oauth2Scheme.getFlow();
            markupDocBuilder.textLine(FLOW + " : " + flow);
            if (isNotBlank(oauth2Scheme.getAuthorizationUrl())) {
                markupDocBuilder.textLine(AUTHORIZATION_URL + " : " + oauth2Scheme.getAuthorizationUrl());
            }
            if (isNotBlank(oauth2Scheme.getTokenUrl())) {
                markupDocBuilder.textLine(TOKEN_URL + " : " + oauth2Scheme.getTokenUrl());
            }
            List<List<String>> cells = new ArrayList<>();
            List<MarkupTableColumn> cols = Arrays.asList(new MarkupTableColumn(NAME_COLUMN, 1),
                    new MarkupTableColumn(DESCRIPTION_COLUMN, 6));
            for (Map.Entry<String, String> scope : oauth2Scheme.getScopes().entrySet()) {
                List<String> content = Arrays.asList(scope.getKey(), scope.getValue());
                cells.add(content);
            }
            markupDocBuilder.tableWithColumnSpecs(cols, cells);
        }
    }

    /**
     * Apply extension context to all SecurityContentExtension
     *
     * @param context context
     */
    private void applySecurityDocumentExtension(Context context) {
        for (SecurityDocumentExtension extension : globalContext.getExtensionRegistry().getExtensions(SecurityDocumentExtension.class)) {
            extension.apply(context);
        }
    }
}
