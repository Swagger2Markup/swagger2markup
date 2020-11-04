package io.github.swagger2markup.internal.utils.pathexamples;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.model.SwaggerPathOperation;

import java.util.Map;

/**
 * Produces Invoke-WebRequest -uri "blahblahwhatever.com/api/uri" -Method POST -Body "\"JSON\":\"string\"" example
 * request
 *
 * Date: 05/06/2020
 * Time: 01:44
 *
 * @author Klaus Schwartz &lt;mailto:klaus@eraga.net&gt;
 */
public class InvokeWebRequestPathExample extends UtilityPathExample {
    /**
     * @param context                    to get configurations and to use in ParameterAdapter
     * @param definitionDocumentResolver to use in ParameterAdapter
     * @param operation                  the Swagger Operation
     */
    public InvokeWebRequestPathExample(Swagger2MarkupConverter.SwaggerContext context,
                                       DocumentResolver definitionDocumentResolver,
                                       SwaggerPathOperation operation) {
        super(context, definitionDocumentResolver, operation);
        if(asciidocCodeLanguage == null)
            asciidocCodeLanguage = "powershell";
    }

    @Override
    protected void generateRequest(String data, Map<String, String> headers) {
        prefix = "Invoke-WebRequest -Method " + operation.getHttpMethod();

        requestString = " -uri \"" + path + query + "\" ";

        if (headers != null && !headers.isEmpty()) {
            StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append(" -Headers ");
            headerBuilder.append("@{ ");
            headers.keySet().forEach(s -> headerBuilder.append("'").append(s).append("' = '").append(headers.get(s)).append("'; "));
            headerBuilder.append("} ");

            header = headerBuilder.toString();
        }

        if (data != null) {
            body = "-Body " +
                    "\"" + data + "\"";
        }
    }
}
