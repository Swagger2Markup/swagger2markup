package io.github.swagger2markup.internal.utils.pathexamples;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.adapter.ParameterAdapter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.model.SwaggerPathOperation;
import io.swagger.models.parameters.Parameter;
import io.swagger.util.Json;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Date: 05/06/2020
 * Time: 03:34
 *
 * @author Klaus Schwartz <mailto:klaus@eraga.net>
 */
abstract public class UtilityPathExample extends BasicPathExample {

    protected String requestString;
    protected String data = null;
    protected Map<String, String> headerParameters;

    /**
     * @param context                    to get configurations and to use in ParameterAdapter
     * @param definitionDocumentResolver to use in ParameterAdapter
     * @param operation                  the Swagger Operation
     */
    public UtilityPathExample(Swagger2MarkupConverter.SwaggerContext context,
                              DocumentResolver definitionDocumentResolver,
                              SwaggerPathOperation operation) {
        super(context, definitionDocumentResolver, operation);


        List<Parameter> bodyParameters = operation
                .getOperation()
                .getParameters()
                .stream()
                .filter(it -> it.getIn().equals("body"))
                .collect(Collectors.toList());

        if (!bodyParameters.isEmpty()) {
            Parameter bodyParameter = bodyParameters.get(0);
            data = "{" + bodyParameter.getName() + "}";
        }


//        operation.getOperation().getSecurity().forEach({ it->
//
//        });

        headerParameters = operation
                .getOperation()
                .getParameters()
                .stream()
                .filter(it -> it.getIn().equals("header"))
                .map(it ->
                        new ParameterAdapter(
                                context,
                                operation,
                                it,
                                definitionDocumentResolver
                        ))
                .collect(Collectors.toMap(ParameterAdapter::getName, it -> "{" + it.getName() + "}"));

        List<String> consumes = operation.getOperation().getConsumes();
        if (consumes != null && consumes.contains("application/json")) {
            headerParameters.put("ContentType", "application/json");
        }
    }

    abstract protected void generateRequest(
            String data,
            Map<String, String> headerParameters);

    @Override
    public String getRequestString() {
        generateRequest(
                data,
                headerParameters
        );

        return prefix + requestString + body;
    }

    @Override
    public void updateBodyParameterValue(Parameter parameter, Object example) {
        if (example == null)
            return;

        try {
            data = StringUtils.replace(
                    data,
                    '{' + parameter.getName() + '}',
                    Json.mapper().writeValueAsString(example).replace("\"", "\\\"").trim()
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHeaderParameterValue(Parameter parameter, Object example) {
        if (example == null)
            return;

        header = StringUtils.replace(
                header,
                '{' + parameter.getName() + '}',
                String.valueOf(example).replace("\"", "\\\"").trim()
        );
    }
}
