package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;

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
    public String displaySchema(MarkupLanguage language) {
        String collectionFormat = "";
        if (this.collectionFormat != null)
            collectionFormat = this.collectionFormat + " ";
        return collectionFormat + ofType.displaySchema(language) + " array";
    }
}
