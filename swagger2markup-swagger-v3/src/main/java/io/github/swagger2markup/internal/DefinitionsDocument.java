package io.github.swagger2markup.internal;

import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;

public class DefinitionsDocument extends MarkupComponent<DefinitionsDocument.Parameters> {
    public DefinitionsDocument(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static Parameters parameters(OpenAPI schema) {
        return new Parameters(schema);
    }

    @Override
    public Document apply(Document document, DefinitionsDocument.Parameters parameters) {
        return document;
    }

    public static class Parameters {
        private final OpenAPI schema;

        public Parameters(OpenAPI schema) {
            this.schema = Validate.notNull(schema, "Schema must not be null");
        }
    }
}


