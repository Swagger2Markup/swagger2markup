package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupLanguage;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;

public class ObjectType extends Type {

    protected Map<String, Property> properties;

    public ObjectType(String name, Map<String, Property> properties) {
        super(name == null ? "object" : name);
        this.properties = properties;
    }

    @Override
    public String displaySchema(MarkupLanguage language) {
        return "object";
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }
}
