package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BasicType extends Type {

    protected String format;

    public BasicType(String name) {
        this(name, null);
    }

    public BasicType(String name, String format) {
        super(name);
        this.format = format;
    }

    @Override
    public String displaySchema(MarkupLanguage language) {
        if (isNotBlank(this.format))
            return this.name + "(" + this.format + ")";
        else
            return this.name;
    }
}
