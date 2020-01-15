package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.swagger.v3.oas.models.OpenAPI;
import org.asciidoctor.ast.Document;


public class OpenApi2AsciiDoc {

    public String translate(OpenAPI openAPI) {
        Document rootDocument = new DocumentImpl();
        OpenApiInfoSection.addInfoSection(rootDocument, openAPI);
        OpenApiTagsSection.appendTagsSection(rootDocument, openAPI.getTags());
        OpenApiServerSection.appendServersSection(rootDocument, openAPI.getServers());
        OpenApiPathsSection.appendPathSection(rootDocument, openAPI.getPaths());
        OpenApiComponentsSection.appendComponentsSection(rootDocument, openAPI.getComponents());
        return rootDocument.convert();
    }
}
