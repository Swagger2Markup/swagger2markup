package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;

public class SecurityDocument extends MarkupComponent<Document, SecurityDocument.Parameters, Document> {
    private final ComponentsDocument componentsDocument;

    public SecurityDocument(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.componentsDocument = new ComponentsDocument(context);
    }

    public static Parameters parameters(OpenAPI schema) {
        return new Parameters(schema);
    }

    @Override
    public Document apply(Document document, SecurityDocument.Parameters parameters) {
        OpenAPI openAPI = parameters.schema;
        componentsDocument.apply(document, ComponentsDocument.parameters(openAPI.getComponents()));
        return document;
    }

    public static class Parameters {
        private final OpenAPI schema;

        public Parameters(OpenAPI schema) {
            this.schema = Validate.notNull(schema, "Schema must not be null");
        }
    }
}
