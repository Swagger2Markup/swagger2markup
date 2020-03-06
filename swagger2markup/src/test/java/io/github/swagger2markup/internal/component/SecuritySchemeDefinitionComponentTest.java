/*
 * Copyright 2017 Robert Winkler
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

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.assertions.DiffUtils;
import io.github.swagger2markup.internal.document.OverviewDocument;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.Swagger;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SecuritySchemeDefinitionComponentTest extends AbstractComponentTest {

    private static final String O_AUTH_NAME = "security_scheme_definition_oauth";
    private static final String API_KEY_NAME = "security_scheme_definition_api_key";
    private Path oauthOutputDirectory;
    private Path apiKeyOutputDirectory;

    @Before
    public void setUp() {
        oauthOutputDirectory = getOutputFile(O_AUTH_NAME);
        apiKeyOutputDirectory = getOutputFile(API_KEY_NAME);
        FileUtils.deleteQuietly(oauthOutputDirectory.toFile());
        FileUtils.deleteQuietly(apiKeyOutputDirectory.toFile());
    }

    @Test
    public void testSecuritySchemeDefinitionComponentWithOAuth() throws URISyntaxException {
        //Given
        Path file = Paths.get(SecuritySchemeDefinitionComponentTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(file).build();
        Swagger swagger = converter.getContext().getSchema();

        SecuritySchemeDefinition securitySchemeDefinition = swagger.getSecurityDefinitions().get("petstore_auth");

        Swagger2MarkupConverter.SwaggerContext context = converter.getContext();
        MarkupDocBuilder markupDocBuilder = context.createMarkupDocBuilder();

        markupDocBuilder = new SecuritySchemeDefinitionComponent(context).apply(
                markupDocBuilder, SecuritySchemeDefinitionComponent.parameters("petstore_auth",
                        securitySchemeDefinition,
                        OverviewDocument.SECTION_TITLE_LEVEL));
        markupDocBuilder.writeToFileWithoutExtension(oauthOutputDirectory, StandardCharsets.UTF_8);

        Path expectedFile = getExpectedFile(O_AUTH_NAME);
        DiffUtils.assertThatFileIsEqual(expectedFile, oauthOutputDirectory, getReportName(O_AUTH_NAME));

    }

    @Test
    public void testSecuritySchemeDefinitionComponentWithApiKey() throws URISyntaxException {
        //Given
        Path file = Paths.get(SecuritySchemeDefinitionComponentTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(file).build();
        Swagger swagger = converter.getContext().getSchema();

        SecuritySchemeDefinition securitySchemeDefinition = swagger.getSecurityDefinitions().get("api_key");

        Swagger2MarkupConverter.SwaggerContext context = converter.getContext();
        MarkupDocBuilder markupDocBuilder = context.createMarkupDocBuilder();

        markupDocBuilder = new SecuritySchemeDefinitionComponent(context).apply(
                markupDocBuilder, SecuritySchemeDefinitionComponent.parameters("api_key",
                        securitySchemeDefinition,
                        OverviewDocument.SECTION_TITLE_LEVEL));
        markupDocBuilder.writeToFileWithoutExtension(apiKeyOutputDirectory, StandardCharsets.UTF_8);

        Path expectedFile = getExpectedFile(API_KEY_NAME);
        DiffUtils.assertThatFileIsEqual(expectedFile, apiKeyOutputDirectory, getReportName(API_KEY_NAME));

    }


}
