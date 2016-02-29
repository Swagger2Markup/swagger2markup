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

import com.google.common.collect.Ordering;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.Language;
import io.github.robwin.swagger2markup.OrderBy;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import static org.assertj.core.api.BDDAssertions.assertThat;

public class Swagger2MarkupConfigTest {

    @Test
    public void testSwagger2MarkupConfigOfDefaults() {
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults().build();

        assertThat(config.getAnchorPrefix()).isNull();
        assertThat(config.getDefinitionOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getDefinitionsDocument()).isEqualTo("definitions");
        assertThat(config.getDefinitionsOrderedBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.isOperationDescriptionsEnabled()).isFalse();
        assertThat(config.getOperationDescriptionsUri()).isNull();
        assertThat(config.isDefinitionDescriptionsEnabled()).isFalse();
        assertThat(config.getDefinitionDescriptionsUri()).isNull();
        assertThat(config.isExamplesEnabled()).isFalse();
        assertThat(config.getExamplesUri()).isNull();
        assertThat(config.getInlineSchemaDepthLevel()).isEqualTo(0);
        assertThat(config.getInterDocumentCrossReferencesPrefix()).isNull();
        assertThat(config.getMarkupLanguage()).isEqualTo(MarkupLanguage.ASCIIDOC);
        assertThat(config.getOperationOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.OPERATION_PATH_COMPARATOR.compound(Swagger2MarkupConfig.Builder.OPERATION_METHOD_COMPARATOR));
        assertThat(config.getOutputLanguage()).isEqualTo(Language.EN);
        assertThat(config.getOverviewDocument()).isEqualTo("overview");
        assertThat(config.getParameterOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.PARAMETER_IN_COMPARATOR.compound(Swagger2MarkupConfig.Builder.PARAMETER_NAME_COMPARATOR));
        assertThat(config.getPathsDocument()).isEqualTo("paths");
        assertThat(config.getOperationsGroupedBy()).isEqualTo(GroupBy.AS_IS);
        assertThat(config.getPropertyOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getResponseOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.isSchemasEnabled()).isFalse();
        assertThat(config.getSchemasUri()).isNull();
        assertThat(config.getSecurityDocument()).isEqualTo("security");
        assertThat(config.getSeparatedDefinitionsFolder()).isEqualTo("definitions");
        assertThat(config.getSeparatedOperationsFolder()).isEqualTo("operations");
        assertThat(config.getTagOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.isFlatBodyEnabled()).isFalse();
        assertThat(config.isInterDocumentCrossReferencesEnabled()).isFalse();
        assertThat(config.isSeparatedDefinitionsEnabled()).isFalse();
        assertThat(config.isSeparatedOperationsEnabled()).isFalse();
    }


    @Test
    public void testSwagger2MarkupConfigOfProperties() throws IOException {

        Properties properties = new Properties();
        properties.load(Swagger2MarkupConfigTest.class.getResourceAsStream("/config/config.properties"));

        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofProperties(properties).build();

        assertThat(config.getAnchorPrefix()).isEqualTo("anchorPrefix");
        //assertThat(config.getDefinitionOrdering()).isEqualTo(Ordering.natural()); // Not supported
        //assertThat(config.getDefinitionsDocument()).isEqualTo(Swagger2MarkupConfig.Builder.DEFINITIONS_DOCUMENT); // Not supported
        assertThat(config.getDefinitionsOrderedBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.isOperationDescriptionsEnabled()).isTrue();
        assertThat(config.getOperationDescriptionsUri()).isEqualTo(URI.create("operationDescriptions"));
        assertThat(config.isDefinitionDescriptionsEnabled()).isTrue();
        assertThat(config.getDefinitionDescriptionsUri()).isEqualTo(URI.create("definitionDescriptions"));
        assertThat(config.isExamplesEnabled()).isTrue();
        assertThat(config.getExamplesUri()).isEqualTo(URI.create("examples"));
        assertThat(config.getInlineSchemaDepthLevel()).isEqualTo(2);
        assertThat(config.getInterDocumentCrossReferencesPrefix()).isEqualTo("xrefPrefix");
        assertThat(config.getMarkupLanguage()).isEqualTo(MarkupLanguage.MARKDOWN);
        //assertThat(config.getOperationOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.OPERATION_PATH_COMPARATOR.compound(Swagger2MarkupConfig.Builder.OPERATION_METHOD_COMPARATOR)); // Not supported
        assertThat(config.getOutputLanguage()).isEqualTo(Language.RU);
        //assertThat(config.getOverviewDocument()).isEqualTo(Swagger2MarkupConfig.Builder.OVERVIEW_DOCUMENT); // Not supported
        //assertThat(config.getParameterOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.PARAMETER_IN_COMPARATOR.compound(Swagger2MarkupConfig.Builder.PARAMETER_NAME_COMPARATOR)); // Not supported
        //assertThat(config.getPathsDocument()).isEqualTo(Swagger2MarkupConfig.Builder.PATHS_DOCUMENT); // Not supported
        assertThat(config.getOperationsGroupedBy()).isEqualTo(GroupBy.TAGS);
        //assertThat(config.getPropertyOrdering()).isEqualTo(Ordering.natural()); // Not supported
        //assertThat(config.getResponseOrdering()).isEqualTo(Ordering.natural()); // Not supported
        assertThat(config.isSchemasEnabled()).isTrue();
        assertThat(config.getSchemasUri()).isEqualTo(URI.create("schemas"));
        //assertThat(config.getSecurityDocument()).isEqualTo(Swagger2MarkupConfig.Builder.SECURITY_DOCUMENT); // Not supported
        //assertThat(config.getSeparatedDefinitionsFolder()).isEqualTo(Swagger2MarkupConfig.Builder.SEPARATED_DEFINITIONS_FOLDER); // Not supported
        //assertThat(config.getSeparatedOperationsFolder()).isEqualTo(Swagger2MarkupConfig.Builder.SEPARATED_OPERATIONS_FOLDER); // Not supported
        //assertThat(config.getTagOrdering()).isEqualTo(Ordering.natural()); // Not supported
        assertThat(config.isFlatBodyEnabled()).isTrue();
        assertThat(config.isInterDocumentCrossReferencesEnabled()).isTrue();
        assertThat(config.isSeparatedDefinitionsEnabled()).isTrue();
        assertThat(config.isSeparatedOperationsEnabled()).isTrue();
    }

}
