package io.github.swagger2markup.internal.utils.pathexamples;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.model.SwaggerPathOperation;

import java.util.Map;

/**
 * Date: 05/06/2020
 * Time: 01:44
 *
 * @author Klaus Schwartz <mailto:klaus@eraga.net>
 */
public class CurlPathExample extends UtilityPathExample {
    /**
     * @param context                    to get configurations and to use in ParameterAdapter
     * @param definitionDocumentResolver to use in ParameterAdapter
     * @param operation                  the Swagger Operation
     */
    public CurlPathExample(Swagger2MarkupConverter.SwaggerContext context,
                           DocumentResolver definitionDocumentResolver,
                           SwaggerPathOperation operation) {
        super(context, definitionDocumentResolver, operation);
        if(asciidocCodeLanguage == null)
            asciidocCodeLanguage = "bash";
    }

    @Override
    protected void generateRequest(String data, Map<String, String> headers) {
        prefix = "curl -s -S ";

        requestString =  "-X " +
                operation.getHttpMethod() +
                " \"" + path + query + "\" ";

        if (headers != null && !headers.isEmpty()) {
            StringBuilder headerBuilder = new StringBuilder();

            headers.keySet().forEach(s -> {
                headerBuilder.append("-H ");
                headerBuilder.append("\"");
                headerBuilder.append(s).append(": ").append(headers.get(s));
                headerBuilder.append("\" ");
            });


            header = headerBuilder.toString();
        }

        if (data != null) {
            body = "-d " +
                    "\"" + data + "\"";
        }
    }
}
