package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.DocumentImpl;
import io.swagger.v3.oas.models.OpenAPI;
import org.asciidoctor.ast.Document;

import static io.github.swagger2markup.OpenApiInfoSection.addInfoSection;
import static io.github.swagger2markup.OpenApiPathsSection.appendPathSection;
import static io.github.swagger2markup.OpenApiServerSection.addServersSection;

public class OpenApi2AsciiDoc {

    public String translate(OpenAPI openAPI) {
        Document rootDocument = new DocumentImpl();
        addInfoSection(rootDocument, openAPI);
        addServersSection(rootDocument, openAPI);
        appendPathSection(rootDocument, openAPI);
        return rootDocument.convert();
    }
}
