package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.lang3.Validate;

public abstract class Type {

    protected String name;

    public Type() {
    }

    public Type(String name) {
        Validate.notBlank(name);

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String displaySchema(MarkupLanguage language);
}
