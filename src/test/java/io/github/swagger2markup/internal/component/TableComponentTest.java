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
import io.github.swagger2markup.assertions.DiffUtils;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static io.github.swagger2markup.helper.ContextUtils.createContext;

public class TableComponentTest extends AbstractComponentTest {

    private static final String COMPONENT_NAME = "table";
    private Path outputDirectory;

    @Before
    public void setUp() {
        outputDirectory = getOutputFile(COMPONENT_NAME);
        FileUtils.deleteQuietly(outputDirectory.toFile());
    }

    @Test
    public void testTable() throws URISyntaxException {
        StringColumn.Builder typeColumnBuilder = StringColumn.builder(StringColumnId.of("type"));
        typeColumnBuilder.add("type1").add("type2").add("type3");

        StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of("name"));
        nameColumnBuilder.add("name1").add("").add("name3");

        StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of("description"));
        descriptionColumnBuilder.add("").add("").add("");

        Swagger2MarkupConverter.Context context = createContext();
        MarkupDocBuilder markupDocBuilder = context.createMarkupDocBuilder();

        markupDocBuilder = new TableComponent(context).apply(markupDocBuilder,
                TableComponent.parameters(
                        typeColumnBuilder.build(),
                        nameColumnBuilder.build(),
                        descriptionColumnBuilder.build()));
        markupDocBuilder.writeToFileWithoutExtension(outputDirectory, StandardCharsets.UTF_8);

        Path expectedFile = getExpectedFile(COMPONENT_NAME);
        DiffUtils.assertThatFileIsEqual(expectedFile, outputDirectory, getReportName(COMPONENT_NAME));
    }


}
