package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public class RefType extends Type {

    private String document;

    public RefType(String document, String name) {
        super(name);
        this.document = document;
    }

    public RefType(Type type) {
        super(type.name, type.uniqueName);
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        return docBuilder.crossReferenceAsString(getDocument(), getUniqueName(), getName());
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}
