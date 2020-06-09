package io.github.swagger2markup.internal.utils.pathexamples;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.adapter.ParameterAdapter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.ArrayType;
import io.github.swagger2markup.model.SwaggerPathOperation;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.swagger2markup.internal.utils.ExamplesUtil.encodeExampleForUrl;

/**
 * Date: 05/06/2020
 * Time: 01:43
 *
 * @author Klaus Schwartz <mailto:klaus@eraga.net>
 */
public class BasicPathExample implements PathExample {
    protected final Swagger2MarkupConverter.SwaggerContext context;
    protected final DocumentResolver definitionDocumentResolver;
    protected final SwaggerPathOperation operation;

    protected List<Parameter> queryParameters;

    protected String prefix = "";
    protected String header = "";
    protected String path;
    protected String query;
    protected String body = "";

    protected String asciidocCodeLanguage;


    /**
     * @param context                    to get configurations and to use in ParameterAdapter
     * @param definitionDocumentResolver to use in ParameterAdapter
     * @param operation                  the Swagger Operation
     */
    public BasicPathExample(Swagger2MarkupConverter.SwaggerContext context,
                            DocumentResolver definitionDocumentResolver,
                            SwaggerPathOperation operation) {
        this.context = context;
        this.definitionDocumentResolver = definitionDocumentResolver;
        this.operation = operation;

        asciidocCodeLanguage = context.getConfig().getRequestExamplesSourceFormat();
        if (asciidocCodeLanguage.equalsIgnoreCase("default")) {
            asciidocCodeLanguage = null;
        }
        boolean includeAllQueryParameters = context.getConfig().getRequestExamplesIncludeAllQueryParams();

        queryParameters = operation
                .getOperation()
                .getParameters()
                .stream()
                .filter(it -> it.getIn().equals("query") && (it.getRequired() || includeAllQueryParameters))
                .collect(Collectors.toList());


        StringBuilder pathBuilder = new StringBuilder();

        String schemaOverride = context.getConfig().getRequestExamplesSchema();
        if (!schemaOverride.equals("hide")) {
            if (schemaOverride.equals("inherit")) {
                if (context.getSchema().getSchemes() != null && !context.getSchema().getSchemes().isEmpty()) {
                    pathBuilder.append(context.getSchema().getSchemes().get(0).toString().toLowerCase());
                    pathBuilder.append("://");
                }
            } else {
                pathBuilder.append(schemaOverride.toLowerCase());
                pathBuilder.append("://");
            }
        }

        String hostOverride = context.getConfig().getRequestExamplesHost();
        if (!hostOverride.equals("hide")) {
            if (hostOverride.equals("inherit")) {
                if (context.getSchema().getHost() != null) {
                    pathBuilder.append(context.getSchema().getHost());
                }
            } else {
                pathBuilder.append(hostOverride);
            }
        }
        if (!context.getConfig().getRequestExamplesHideBasePath()) {
            if (context.getSchema().getBasePath() != null) {
                pathBuilder.append(context.getSchema().getBasePath());
            }
        }

        pathBuilder.append(operation.getPath());

        path = pathBuilder.toString().trim();


        StringBuilder queryBuilder = new StringBuilder();
        if (!queryParameters.isEmpty()) {
            if (path.contains("?"))
                queryBuilder.append("&");
            else
                queryBuilder.append("?");

            queryParameters.forEach(it -> {
                ParameterAdapter adapter = new ParameterAdapter(
                        context,
                        operation,
                        it,
                        definitionDocumentResolver
                );

                String arrayParamSuffix = "";
                String arrayValueDuplicate = "";

                if (adapter.getType() instanceof ArrayType) {

                    switch (context.getConfig().getRequestExamplesQueryArrayStyle()) {
                        case "commaSeparated":
                            arrayValueDuplicate = "1,{" + it.getName() + "}2";
                            break;
                        case "multiple":
                            queryBuilder
                                    .append(it.getName())
                                    .append(arrayParamSuffix)
                                    .append("=").append("{").append(it.getName()).append("}1").append("&");
                            arrayValueDuplicate = "2";
                            break;
                        case "multiple[]":
                            arrayParamSuffix = "[]";
                            queryBuilder
                                    .append(it.getName())
                                    .append(arrayParamSuffix)
                                    .append("=").append("{").append(it.getName()).append("}1").append("&");
                            arrayValueDuplicate = "2";
                            break;
                        case "single":
                        default:
                            arrayValueDuplicate = "";
                            arrayParamSuffix = "";
                    }
                }

                queryBuilder
                        .append(it.getName())
                        .append(arrayParamSuffix)
                        .append("=").append("{").append(it.getName()).append("}").append(arrayValueDuplicate).append("&");
            });

            queryBuilder.deleteCharAt(queryBuilder.lastIndexOf("&"));
        }

        query = queryBuilder.toString();
    }


    @Override
    public void updateHeaderParameterValue(Parameter parameter, Object example) {
        //do nothing here: basic example has no header in request path
    }

    @Override
    public void updatePathParameterValue(Parameter parameter, Object example) {
        if (parameter.getName().contains("id")) {
            System.getenv();
        }

        if (example == null)
            return;

        if (path.contains(parameter.getName()))
            path = StringUtils.replace(
                    path,
                    '{' + parameter.getName() + '}',
                    encodeExampleForUrl(example)
            );
    }

    @Override
    public void updateQueryParameterValue(Parameter parameter, Object example) {
        if (example == null)
            return;

        if (query.contains(parameter.getName()))
            query = StringUtils.replace(
                    query,
                    '{' + parameter.getName() + '}',
                    encodeExampleForUrl(example)
            );
    }

    @Override
    public void updateBodyParameterValue(Parameter parameter, Object example) {
        //do nothing here: basic example has no body in request path
    }

    @Override
    public String getRequestString() {
        return prefix + header + path + query + body;
    }

    @Override
    public String getAsciidocCodeLanguage() {
        return asciidocCodeLanguage;
    }

    @Override
    public Swagger2MarkupConverter.SwaggerContext getContext() {
        return context;
    }

    @Override
    public DocumentResolver getDefinitionDocumentResolver() {
        return definitionDocumentResolver;
    }

    @Override
    public SwaggerPathOperation getOperation() {
        return operation;
    }

}
