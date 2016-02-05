package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public class ArrayType extends Type {

    protected String collectionFormat;
    protected Type ofType;

    public ArrayType(String name, Type ofType) {
        this(name, ofType, null);
    }

    public ArrayType(String name, Type ofType, String collectionFormat) {
        super(name == null ? "array" : name);
        this.collectionFormat = collectionFormat;
        this.ofType = ofType;
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        String collectionFormat = "";
        if (this.collectionFormat != null)
            collectionFormat = this.collectionFormat + " ";
        return collectionFormat + ofType.displaySchema(docBuilder) + " array";
    }
}
