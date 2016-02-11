package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import org.apache.commons.lang3.Validate;

/**
 * Type abstraction for display purpose
 */
public abstract class Type {

    protected String name;
    protected String uniqueName;

    public Type(String name, String uniqueName) {
        Validate.notBlank(name);

        this.name = name;
        this.uniqueName = uniqueName;
    }

    public Type(String name) {
        this(name, name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public abstract String displaySchema(MarkupDocBuilder docBuilder);
}
