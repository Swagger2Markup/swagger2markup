package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

public class EnumType extends Type {

    protected List<String> values;

    public EnumType(String name, List<String> values) {
        super(name == null ? "enum" : name);
        this.values = values;
    }

    @Override
    public String displaySchema(MarkupLanguage language) {
        return "enum" + " (" + join(values, ", ") + ")";
    }
}
