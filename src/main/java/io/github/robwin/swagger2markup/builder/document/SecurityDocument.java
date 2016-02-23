/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup.builder.document;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import io.github.robwin.markup.builder.MarkupTableColumn;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.swagger.models.Swagger;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.collections.MapUtils;

import com.google.common.base.Joiner;

/**
 * @author Robert Winkler
 */
public class SecurityDocument extends MarkupDocument {

    private static final String SECURITY_ANCHOR = "security";
    private final String SECURITY;
    private final String TYPE;
    private final String NAME;
    private final String IN;
    private final String FLOW;
    private final String AUTHORIZATION_URL;
    private final String TOKEN_URL;

    public SecurityDocument(Swagger swagger, Swagger2MarkupConfig swagger2MarkupConfig, String outputDirectory) {
        super(swagger, swagger2MarkupConfig, outputDirectory);

        ResourceBundle labels = ResourceBundle.getBundle("lang/labels",
                swagger2MarkupConfig.getOutputLanguage().toLocale());
        SECURITY = labels.getString("security");
        TYPE = labels.getString("security_type");
        NAME = labels.getString("security_name");
        IN = labels.getString("security_in");
        FLOW = labels.getString("security_flow");
        AUTHORIZATION_URL = labels.getString("security_authorizationUrl");
        TOKEN_URL = labels.getString("security_tokenUrl");
    }

    /**
     * Builds the security markup document.
     *
     * @return the the security markup document
     */
    @Override
    public MarkupDocument build(){
        security();
        return this;
    }

    private void addSecurityTitle(String title) {
        this.markupDocBuilder.sectionTitleWithAnchorLevel1(title, SECURITY_ANCHOR);

    }

    /**
     * Builds all security definition of the Swagger model.
     */
    private void security() {
        Map<String, SecuritySchemeDefinition> definitions = swagger.getSecurityDefinitions();
        if (MapUtils.isNotEmpty(definitions)) {
            addSecurityTitle(SECURITY);
            for (Map.Entry<String, SecuritySchemeDefinition> entry : definitions.entrySet()) {
                markupDocBuilder.sectionTitleLevel2(entry.getKey());
                SecuritySchemeDefinition securityScheme = entry.getValue();
                String type = securityScheme.getType();
                markupDocBuilder.textLine(TYPE + type);
                if (securityScheme instanceof ApiKeyAuthDefinition) {
                    markupDocBuilder.textLine(NAME + ((ApiKeyAuthDefinition) securityScheme).getName());
                    markupDocBuilder.textLine(IN + ((ApiKeyAuthDefinition) securityScheme).getIn());
                } else if (securityScheme instanceof OAuth2Definition) {
                    OAuth2Definition oauth2Scheme = (OAuth2Definition) securityScheme;
                    String flow = oauth2Scheme.getFlow();
                    markupDocBuilder.textLine(FLOW + flow);
                    if (isNotBlank(oauth2Scheme.getAuthorizationUrl())) {
                        markupDocBuilder.textLine(AUTHORIZATION_URL + oauth2Scheme.getAuthorizationUrl());
                    }
                    if (isNotBlank(oauth2Scheme.getTokenUrl())) {
                        markupDocBuilder.textLine(TOKEN_URL + oauth2Scheme.getTokenUrl());
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
        }
    }
}
