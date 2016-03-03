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
    public void testConfigOfDefaults() {
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults().build();

        assertThat(config.getAnchorPrefix()).isNull();
        assertThat(config.getDefinitionOrderBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getDefinitionOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getDefinitionsDocument()).isEqualTo("definitions");
        assertThat(config.isOperationDescriptionsEnabled()).isFalse();
        assertThat(config.getOperationDescriptionsUri()).isNull();
        assertThat(config.isDefinitionDescriptionsEnabled()).isFalse();
        assertThat(config.getDefinitionDescriptionsUri()).isNull();
        assertThat(config.isGeneratedExamplesEnabled()).isFalse();
        assertThat(config.getInlineSchemaDepthLevel()).isEqualTo(0);
        assertThat(config.getInterDocumentCrossReferencesPrefix()).isNull();
        assertThat(config.getMarkupLanguage()).isEqualTo(MarkupLanguage.ASCIIDOC);
        assertThat(config.getOperationOrderBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getOperationOrdering()).isNull();
        assertThat(config.getOutputLanguage()).isEqualTo(Language.EN);
        assertThat(config.getOverviewDocument()).isEqualTo("overview");
        assertThat(config.getParameterOrderBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getParameterOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.PARAMETER_IN_NATURAL_ORDERING.compound(Swagger2MarkupConfig.Builder.PARAMETER_NAME_NATURAL_ORDERING));
        assertThat(config.getPathsDocument()).isEqualTo("paths");
        assertThat(config.getOperationsGroupedBy()).isEqualTo(GroupBy.AS_IS);
        assertThat(config.getPropertyOrderBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getPropertyOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.getResponseOrderBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getResponseOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.isSchemasEnabled()).isFalse();
        assertThat(config.getSchemasUri()).isNull();
        assertThat(config.getSecurityDocument()).isEqualTo("security");
        assertThat(config.getSeparatedDefinitionsFolder()).isEqualTo("definitions");
        assertThat(config.getSeparatedOperationsFolder()).isEqualTo("operations");
        assertThat(config.getTagOrderBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getTagOrdering()).isEqualTo(Ordering.natural());
        assertThat(config.isFlatBodyEnabled()).isFalse();
        assertThat(config.isInterDocumentCrossReferencesEnabled()).isFalse();
        assertThat(config.isSeparatedDefinitionsEnabled()).isFalse();
        assertThat(config.isSeparatedOperationsEnabled()).isFalse();
    }


    @Test
    public void testConfigOfProperties() throws IOException {

        Properties properties = new Properties();
        properties.load(Swagger2MarkupConfigTest.class.getResourceAsStream("/config/config.properties"));

        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofProperties(properties).build();

        assertThat(config.getAnchorPrefix()).isEqualTo("anchorPrefix");
        assertThat(config.getDefinitionOrderBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getDefinitionOrdering()).isNull();
        assertThat(config.getDefinitionsDocument()).isEqualTo("definitionsTest");
        assertThat(config.isOperationDescriptionsEnabled()).isTrue();
        assertThat(config.getOperationDescriptionsUri()).isEqualTo(URI.create("operationDescriptions"));
        assertThat(config.isDefinitionDescriptionsEnabled()).isTrue();
        assertThat(config.getDefinitionDescriptionsUri()).isEqualTo(URI.create("definitionDescriptions"));
        assertThat(config.isGeneratedExamplesEnabled()).isTrue();
        assertThat(config.getInlineSchemaDepthLevel()).isEqualTo(2);
        assertThat(config.getInterDocumentCrossReferencesPrefix()).isEqualTo("xrefPrefix");
        assertThat(config.getMarkupLanguage()).isEqualTo(MarkupLanguage.MARKDOWN);
        assertThat(config.getOperationOrderBy()).isEqualTo(OrderBy.NATURAL);
        assertThat(config.getOperationOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.OPERATION_PATH_NATURAL_ORDERING.compound(Swagger2MarkupConfig.Builder.OPERATION_METHOD_NATURAL_ORDERING));
        assertThat(config.getOutputLanguage()).isEqualTo(Language.RU);
        assertThat(config.getOverviewDocument()).isEqualTo("overviewTest");
        assertThat(config.getParameterOrderBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getParameterOrdering()).isNull();
        assertThat(config.getPathsDocument()).isEqualTo("pathsTest");
        assertThat(config.getOperationsGroupedBy()).isEqualTo(GroupBy.TAGS);
        assertThat(config.getPropertyOrderBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getPropertyOrdering()).isNull();
        assertThat(config.getResponseOrderBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getResponseOrdering()).isNull();
        assertThat(config.isSchemasEnabled()).isTrue();
        assertThat(config.getSchemasUri()).isEqualTo(URI.create("schemas"));
        assertThat(config.getSecurityDocument()).isEqualTo("securityTest");
        assertThat(config.getSeparatedDefinitionsFolder()).isEqualTo("definitionsTest");
        assertThat(config.getSeparatedOperationsFolder()).isEqualTo("operationsTest");
        assertThat(config.getTagOrderBy()).isEqualTo(OrderBy.AS_IS);
        assertThat(config.getTagOrdering()).isNull();
        assertThat(config.isFlatBodyEnabled()).isTrue();
        assertThat(config.isInterDocumentCrossReferencesEnabled()).isTrue();
        assertThat(config.isSeparatedDefinitionsEnabled()).isTrue();
        assertThat(config.isSeparatedOperationsEnabled()).isTrue();
    }

    @Test
    public void testConfigBuilder() {
        Swagger2MarkupConfig.Builder builder = Swagger2MarkupConfig.ofDefaults();

        try {
            builder.withTagOrdering(OrderBy.CUSTOM);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        }
        builder.withTagOrdering(Ordering.<String>natural());
        assertThat(builder.config.getTagOrderBy()).isEqualTo(OrderBy.CUSTOM);
        assertThat(builder.config.getTagOrdering()).isEqualTo(Ordering.natural());

        try {
            builder.withOperationOrdering(OrderBy.CUSTOM);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        }
        builder.withOperationOrdering(Swagger2MarkupConfig.Builder.OPERATION_PATH_NATURAL_ORDERING);
        assertThat(builder.config.getOperationOrderBy()).isEqualTo(OrderBy.CUSTOM);
        assertThat(builder.config.getOperationOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.OPERATION_PATH_NATURAL_ORDERING);

        try {
            builder.withDefinitionOrdering(OrderBy.CUSTOM);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        }
        builder.withDefinitionOrdering(Ordering.<String>natural());
        assertThat(builder.config.getDefinitionOrderBy()).isEqualTo(OrderBy.CUSTOM);
        assertThat(builder.config.getDefinitionOrdering()).isEqualTo(Ordering.natural());

        try {
            builder.withParameterOrdering(OrderBy.CUSTOM);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        }
        builder.withParameterOrdering(Swagger2MarkupConfig.Builder.PARAMETER_NAME_NATURAL_ORDERING);
        assertThat(builder.config.getParameterOrderBy()).isEqualTo(OrderBy.CUSTOM);
        assertThat(builder.config.getParameterOrdering()).isEqualTo(Swagger2MarkupConfig.Builder.PARAMETER_NAME_NATURAL_ORDERING);

        try {
            builder.withPropertyOrdering(OrderBy.CUSTOM);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        }
        builder.withPropertyOrdering(Ordering.<String>natural());
        assertThat(builder.config.getPropertyOrderBy()).isEqualTo(OrderBy.CUSTOM);
        assertThat(builder.config.getPropertyOrdering()).isEqualTo(Ordering.natural());

        try {
            builder.withResponseOrdering(OrderBy.CUSTOM);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("You must provide a custom comparator if orderBy == OrderBy.CUSTOM");
        }
        builder.withResponseOrdering(Ordering.<String>natural());
        assertThat(builder.config.getResponseOrderBy()).isEqualTo(OrderBy.CUSTOM);
        assertThat(builder.config.getResponseOrdering()).isEqualTo(Ordering.natural());
    }

}
