package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.utils.MarkupDocBuilderUtils;

public class RefType extends Type {

    public RefType(String name) {
        super(name);
    }

    public RefType(Type type) {
        super(type.name, type.uniqueName);
    }

    @Override
    public String displaySchema(MarkupLanguage language) {
        return MarkupDocBuilderUtils.crossReference(getName(), getUniqueName(), language);
    }
}
