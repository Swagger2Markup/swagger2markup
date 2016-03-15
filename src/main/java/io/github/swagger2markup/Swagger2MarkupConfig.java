
package io.github.swagger2markup;

import io.github.robwin.markup.builder.LineSeparator;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.parameters.Parameter;

import java.net.URI;
import java.util.Comparator;

public interface Swagger2MarkupConfig {
    MarkupLanguage getMarkupLanguage();

    boolean isGeneratedExamplesEnabled();

    boolean isOperationDescriptionsEnabled();

    URI getOperationDescriptionsUri();

    boolean isDefinitionDescriptionsEnabled();

    URI getDefinitionDescriptionsUri();

    boolean isSeparatedDefinitionsEnabled();

    boolean isSeparatedOperationsEnabled();

    GroupBy getOperationsGroupedBy();

    Language getOutputLanguage();

    int getInlineSchemaDepthLevel();

    OrderBy getTagOrderBy();

    Comparator<String> getTagOrdering();

    OrderBy getOperationOrderBy();

    Comparator<PathOperation> getOperationOrdering();

    OrderBy getDefinitionOrderBy();

    Comparator<String> getDefinitionOrdering();

    OrderBy getParameterOrderBy();

    Comparator<Parameter> getParameterOrdering();

    OrderBy getPropertyOrderBy();

    Comparator<String> getPropertyOrdering();

    OrderBy getResponseOrderBy();

    Comparator<String> getResponseOrdering();

    boolean isInterDocumentCrossReferencesEnabled();

    String getInterDocumentCrossReferencesPrefix();

    boolean isFlatBodyEnabled();

    String getAnchorPrefix();

    String getOverviewDocument();

    String getPathsDocument();

    String getDefinitionsDocument();

    String getSecurityDocument();

    String getSeparatedOperationsFolder();

    String getSeparatedDefinitionsFolder();

    LineSeparator getLineSeparator();
}
