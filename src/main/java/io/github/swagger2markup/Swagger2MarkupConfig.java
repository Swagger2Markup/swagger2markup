
package io.github.swagger2markup;

import io.github.robwin.markup.builder.LineSeparator;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.parameters.Parameter;

import java.net.URI;
import java.util.Comparator;
import java.util.Map;

/**
 * Swagger2Markup configuration interface.
 */
public interface Swagger2MarkupConfig {

    /**
     * Prefix for Swagger2Markup properties
     */
    String PROPERTIES_PREFIX = "swagger2markup.";

    /**
     * Prefix for Swagger2Markup extension properties
     */
    String EXTENSION_PREFIX = PROPERTIES_PREFIX + "extensions.";

    /**
     * Specifies the markup language which should be used to generate the files.
     */
    MarkupLanguage getMarkupLanguage();

    /**
     * Include generated examples into the documents.
     */
    boolean isGeneratedExamplesEnabled();

    /**
     * Include hand-written descriptions into the Paths document.
     */
    boolean isOperationDescriptionsEnabled();

    /**
     * Hand-written operation descriptions URI.
     */
    URI getOperationDescriptionsUri();

    /**
     * Include hand-written descriptions into the Definitions document.
     */
    boolean isDefinitionDescriptionsEnabled();

    /**
     * Hand-written definition descriptions URI.
     */
    URI getDefinitionDescriptionsUri();

    /**
     * In addition to the Definitions file, also create separate definition files for each model definition.
     */
    boolean isSeparatedDefinitionsEnabled();

    /**
     * In addition to the Paths file, also create separate operation files for each operation.
     */
    boolean isSeparatedOperationsEnabled();

    /**
     * Specifies if the operations should be grouped by tags or stay as-is.
     */
    GroupBy getOperationsGroupedBy();

    /**
     * Specifies labels language of output files.
     */
    Language getOutputLanguage();

    /**
     * Specifies maximum depth level for inline object schema displaying (0 = no inline schemasEnabled).
     */
    int getInlineSchemaDepthLevel();

    /**
     * Specifies tag ordering.
     */
    OrderBy getTagOrderBy();

    /**
     * Specifies a custom comparator function to order tags.
     */
    Comparator<String> getTagOrdering();

    /**
     * Specifies operation ordering.
     */
    OrderBy getOperationOrderBy();

    /**
     * Specifies a custom comparator function to order operations.
     */
    Comparator<PathOperation> getOperationOrdering();

    /**
     * Specifies definition ordering.
     */
    OrderBy getDefinitionOrderBy();

    /**
     * Specifies a custom comparator function to order definitions.
     */
    Comparator<String> getDefinitionOrdering();

    /**
     * Specifies parameter ordering.
     */
    OrderBy getParameterOrderBy();

    /**
     * Specifies a custom comparator function to order parameters.
     */
    Comparator<Parameter> getParameterOrdering();

    /**
     * Specifies property ordering.
     */
    OrderBy getPropertyOrderBy();

    /**
     * Specifies a custom comparator function to order properties.
     */
    Comparator<String> getPropertyOrdering();

    /**
     * Specifies response ordering.
     */
    OrderBy getResponseOrderBy();

    /**
     * Specifies a custom comparator function to order responses.
     */
    Comparator<String> getResponseOrdering();

    /**
     * Enable use of inter-document cross-references when needed.
     */
    boolean isInterDocumentCrossReferencesEnabled();

    /**
     * Inter-document cross-references optional prefix.
     */
    String getInterDocumentCrossReferencesPrefix();

    /**
     * Optionally isolate the body parameter, if any, from other parameters.
     */
    boolean isFlatBodyEnabled();

    /**
     * Optionally prefix all anchors for uniqueness.
     */
    String getAnchorPrefix();

    /**
     * Overview document name (without extension).
     */
    String getOverviewDocument();

    /**
     * Paths document name (without extension).
     */
    String getPathsDocument();

    /**
     * Definitions document name (without extension).
     */
    String getDefinitionsDocument();

    /**
     * Security document name (without extension).
     */
    String getSecurityDocument();

    /**
     * Separated operations sub-folder name.
     */
    String getSeparatedOperationsFolder();

    /**
     * Separated definitions sub-folder name.
     */
    String getSeparatedDefinitionsFolder();

    /**
     * Specifies the line separator which should be used.
     */
    LineSeparator getLineSeparator();

    Map<String, String> getExtensionsProperties();
}
