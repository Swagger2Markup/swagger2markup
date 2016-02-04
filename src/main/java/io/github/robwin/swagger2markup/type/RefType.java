package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public class RefType extends Type {

    public RefType(String name) {
        super(name);
    }

    public RefType(Type type) {
        super(type.name, type.uniqueName);
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        return docBuilder.crossReferenceAsString(getUniqueName(), getName());
    }
}
