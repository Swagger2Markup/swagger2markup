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

import io.github.swagger2markup.AsciidocConverterTest;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.assertions.DiffUtils;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverFromOperation;
import io.github.swagger2markup.internal.utils.PathUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class BodyParameterComponentTest extends AbstractComponentTest {

    private static final String COMPONENT_NAME = "body_parameter";
    private Path outputDirectory;

    @Before
    public void setUp() {
        outputDirectory = getOutputFile(COMPONENT_NAME);
        FileUtils.deleteQuietly(outputDirectory.toFile());
    }

    @Test
    public void testBodyParameterComponent() throws URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder().withFlatBody().build();
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(file).withConfig(config).build();
        Swagger swagger = converter.getContext().getSwagger();

        io.swagger.models.Path path = swagger.getPaths().get("/pets");
        List<PathOperation> pathOperations = PathUtils.toPathOperationsList("/pets", path);

        Swagger2MarkupConverter.Context context = converter.getContext();
        MarkupDocBuilder markupDocBuilder = context.createMarkupDocBuilder();

        //When
        markupDocBuilder = new BodyParameterComponent(converter.getContext(),
                new DefinitionDocumentResolverFromOperation(context))
                .apply(markupDocBuilder, BodyParameterComponent.parameters(pathOperations.get(0), new ArrayList<>()));

        markupDocBuilder.writeToFileWithoutExtension(outputDirectory, StandardCharsets.UTF_8);

        //Then
        Path expectedFile = getExpectedFile(COMPONENT_NAME);
        DiffUtils.assertThatFileIsEqual(expectedFile, outputDirectory, getReportName(COMPONENT_NAME));

    }
}
