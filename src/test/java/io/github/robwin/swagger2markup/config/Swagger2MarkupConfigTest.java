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
package io.github.robwin.swagger2markup.config;

import com.google.common.collect.*;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.Language;
import io.github.robwin.swagger2markup.OrderBy;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.BDDAssertions.assertThat;

public class Swagger2MarkupConfigTest {

    @Test
    public void testSwagger2MarkupConfigOfDefaults() {
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults().build();

        assertThat(config.getAnchorPrefix()).isNull();
        assertThat(config.getDefinitionExtensionsFolderPath()).isNull();
        assertThat(config.getDefinitionOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getDefinitionsDocument()).isEqualTo(Swagger2MarkupConfig.Builder.DEFINITIONS_DOCUMENT);
        assertThat(config.getDefinitionsOrderedBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getDescriptionsFolderPath()).isNull();
        assertThat(config.getExamplesFolderPath()).isNull();
        assertThat(config.getInlineSchemaDepthLevel()).isEqualTo(0);
        assertThat(config.getInterDocumentCrossReferencesPrefix()).isEqualTo("");
        assertThat(config.getMarkupLanguage()).isEqualTo(MarkupLanguage.ASCIIDOC);
        assertThat(config.getOperationExtensionsFolderPath()).isNull();
        assertThat(config.getOperationOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.OPERATION_PATH_COMPARATOR.compound(Swagger2MarkupConfig.Builder.OPERATION_METHOD_COMPARATOR));
        assertThat(config.getOutputLanguage()).isEqualTo(Language.EN);
        assertThat(config.getOverviewDocument()).isEqualTo(Swagger2MarkupConfig.Builder.OVERVIEW_DOCUMENT);
        assertThat(config.getParameterOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.PARAMETER_IN_COMPARATOR.compound(Swagger2MarkupConfig.Builder.PARAMETER_NAME_COMPARATOR));
        assertThat(config.getPathsDocument()).isEqualTo(Swagger2MarkupConfig.Builder.PATHS_DOCUMENT);
        assertThat(config.getPathsGroupedBy()).isEqualTo(GroupBy.AS_IS);
        assertThat(config.getPropertyOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getResponseOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getSchemasFolderPath()).isNull();
        assertThat(config.getSecurityDocument()).isEqualTo(Swagger2MarkupConfig.Builder.SECURITY_DOCUMENT);
        assertThat(config.getSeparatedDefinitionsFolder()).isEqualTo(Swagger2MarkupConfig.Builder.SEPARATED_DEFINITIONS_FOLDER);
        assertThat(config.getSeparatedOperationsFolder()).isEqualTo(Swagger2MarkupConfig.Builder.SEPARATED_OPERATIONS_FOLDER);
        assertThat(config.getTagOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.isFlatBody()).isFalse();
        assertThat(config.isInterDocumentCrossReferences()).isFalse();
        assertThat(config.isSeparatedDefinitions()).isFalse();
        assertThat(config.isSeparatedOperations()).isFalse();
    }


    @Test
    public void testSwagger2MarkupConfig() throws IOException {

        Properties properties = new Properties();
        properties.load(Swagger2MarkupConfigTest.class.getResourceAsStream("/config/config.properties"));

        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofProperties(properties).build();

        assertThat(config.getAnchorPrefix()).isEqualTo("anchorPrefix");
        assertThat(config.getDefinitionExtensionsFolderPath()).isEqualTo("definitionExtensions");
        //assertThat(config.getDefinitionOrdering()).isEqualTo(Ordering.natural()); // Not supported
        //assertThat(config.getDefinitionsDocument()).isEqualTo(Swagger2MarkupConfig.Builder.DEFINITIONS_DOCUMENT); // Not supported
        assertThat(config.getDefinitionsOrderedBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getDescriptionsFolderPath()).isEqualTo("descriptions");
        assertThat(config.getExamplesFolderPath()).isEqualTo("examples");
        assertThat(config.getInlineSchemaDepthLevel()).isEqualTo(2);
        assertThat(config.getInterDocumentCrossReferencesPrefix()).isEqualTo("xrefPrefix");
        assertThat(config.getMarkupLanguage()).isEqualTo(MarkupLanguage.MARKDOWN);
        assertThat(config.getOperationExtensionsFolderPath()).isEqualTo("operationExtensions");
        //assertThat(config.getOperationOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.OPERATION_PATH_COMPARATOR.compound(Swagger2MarkupConfig.Builder.OPERATION_METHOD_COMPARATOR)); // Not supported
        assertThat(config.getOutputLanguage()).isEqualTo(Language.RU);
        //assertThat(config.getOverviewDocument()).isEqualTo(Swagger2MarkupConfig.Builder.OVERVIEW_DOCUMENT); // Not supported
        //assertThat(config.getParameterOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.PARAMETER_IN_COMPARATOR.compound(Swagger2MarkupConfig.Builder.PARAMETER_NAME_COMPARATOR)); // Not supported
        //assertThat(config.getPathsDocument()).isEqualTo(Swagger2MarkupConfig.Builder.PATHS_DOCUMENT); // Not supported
        assertThat(config.getPathsGroupedBy()).isEqualTo(GroupBy.TAGS);
        //assertThat(config.getPropertyOrdering()).isEqualTo(Ordering.natural()); // Not supported
        //assertThat(config.getResponseOrdering()).isEqualTo(Ordering.natural()); // Not supported
        assertThat(config.getSchemasFolderPath()).isEqualTo("schemas");
        //assertThat(config.getSecurityDocument()).isEqualTo(Swagger2MarkupConfig.Builder.SECURITY_DOCUMENT); // Not supported
        //assertThat(config.getSeparatedDefinitionsFolder()).isEqualTo(Swagger2MarkupConfig.Builder.SEPARATED_DEFINITIONS_FOLDER); // Not supported
        //assertThat(config.getSeparatedOperationsFolder()).isEqualTo(Swagger2MarkupConfig.Builder.SEPARATED_OPERATIONS_FOLDER); // Not supported
        //assertThat(config.getTagOrdering()).isEqualTo(Ordering.natural()); // Not supported
        assertThat(config.isFlatBody()).isTrue();
        assertThat(config.isInterDocumentCrossReferences()).isTrue();
        assertThat(config.isSeparatedDefinitions()).isTrue();
        assertThat(config.isSeparatedOperations()).isTrue();
    }

}