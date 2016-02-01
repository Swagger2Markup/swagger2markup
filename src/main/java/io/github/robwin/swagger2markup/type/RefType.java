package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;

public class RefType extends Type {

    public RefType(String name) {
        super(name);
    }

    @Override
    public String displaySchema(MarkupLanguage language) {
        switch (language) {
            case ASCIIDOC:
                return "<<" + getName() + ">>";
            default:
                return getName();
        }
    }
}
