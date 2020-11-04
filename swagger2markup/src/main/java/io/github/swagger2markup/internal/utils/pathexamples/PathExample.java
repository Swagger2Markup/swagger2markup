package io.github.swagger2markup.internal.utils.pathexamples;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.model.SwaggerPathOperation;
import io.swagger.models.parameters.Parameter;

/**
 * Date: 05/06/2020
 * Time: 01:43
 *
 * @author Klaus Schwartz &lt;mailto:klaus@eraga.net&gt;
 */
public interface PathExample {
    String getRequestString();
    String getAsciidocCodeLanguage();
    void updateHeaderParameterValue(Parameter parameter, Object example);
    void updatePathParameterValue(Parameter parameter, Object example);
    void updateQueryParameterValue(Parameter parameter, Object example);
    void updateBodyParameterValue(Parameter parameter, Object example);

    Swagger2MarkupConverter.SwaggerContext getContext();
    DocumentResolver getDefinitionDocumentResolver();
    SwaggerPathOperation getOperation();
}
