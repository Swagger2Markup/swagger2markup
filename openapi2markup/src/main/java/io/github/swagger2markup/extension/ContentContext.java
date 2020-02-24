package io.github.swagger2markup.extension;

import org.asciidoctor.ast.Document;

public class ContentContext {
    private Document document;

    public ContentContext(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
