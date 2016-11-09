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

import io.github.swagger2markup.assertions.DiffUtils;
import io.github.swagger2markup.internal.document.builder.OverviewDocumentBuilder;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.auth.OAuth2Definition;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


public class SecuritySchemeDefinitionComponentTest extends AbstractComponentTest{

    private static final String COMPONENT_NAME = "security_scheme_definition";
    private Path outputDirectory;

    @Before
    public void setUp(){
        outputDirectory = getOutputFile(COMPONENT_NAME);
        FileUtils.deleteQuietly(outputDirectory.toFile());
    }

    @Test
    public void testSecuritySchemeDefinitionComponent() throws URISyntaxException {

        String securitySchemeDefinitionName = "SecuritySchemeDefinitionName";
        OAuth2Definition securitySchemeDefinition = new OAuth2Definition();
        securitySchemeDefinition.implicit("http://petstore.swagger.io/api/oauth/dialog");
        securitySchemeDefinition.setDescription("Bla bla *blabla*");
        securitySchemeDefinition.addScope("write_pets", "modify pets in your account");
        securitySchemeDefinition.addScope("read_pets", "read pets in your account");
        MarkupDocBuilder markupDocBuilder = new SecuritySchemeDefinitionComponent(getComponentContext(), securitySchemeDefinitionName, securitySchemeDefinition, OverviewDocumentBuilder.SECTION_TITLE_LEVEL).render();
        markupDocBuilder.writeToFileWithoutExtension(outputDirectory,  StandardCharsets.UTF_8);

        Path expectedFile = getExpectedFile(COMPONENT_NAME);
        DiffUtils.assertThatFileIsEqual(expectedFile, outputDirectory, getReportName(COMPONENT_NAME));

    }
}
