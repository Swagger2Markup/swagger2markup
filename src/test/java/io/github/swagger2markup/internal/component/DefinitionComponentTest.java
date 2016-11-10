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
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class DefinitionComponentTest extends AbstractComponentTest{

    private static final String COMPONENT_NAME = "definition";
    private Path outputDirectory;

    @Before
    public void setUp(){
        outputDirectory = getOutputFile(COMPONENT_NAME);
        FileUtils.deleteQuietly(outputDirectory.toFile());
    }

    @Test
    public void testDefinitionComponent() throws URISyntaxException {
        Map<String, Model> definitions = new HashMap<>();
        String definitionName = "TestName";
        Model model = new ModelImpl().name("TestName")
                .description("Blablabla *blabla*")
                .property("StringProperty", new StringProperty())
                .property("IntProperty", new IntegerProperty())
                .required("StringProperty");

        model.setTitle("Title");

        MarkupDocBuilder markupDocBuilder = new DefinitionComponent(getComponentContext(),
                definitions,
                definitionName,
                model,
                null,
                OverviewDocumentBuilder.SECTION_TITLE_LEVEL).render();
        markupDocBuilder.writeToFileWithoutExtension(outputDirectory,  StandardCharsets.UTF_8);

        Path expectedFile = getExpectedFile(COMPONENT_NAME);
        DiffUtils.assertThatFileIsEqual(expectedFile, outputDirectory, getReportName(COMPONENT_NAME));

    }
}
