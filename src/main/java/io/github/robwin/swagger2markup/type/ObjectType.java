package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.swagger.models.properties.Property;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * Complex object abstraction
 */
public class ObjectType extends Type {

    protected Map<String, Property> properties;

    public ObjectType(String name, Map<String, Property> properties) {
        super(name == null ? "object" : name);
        this.properties = properties;
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        if (MapUtils.isEmpty(properties))
            return "empty object";
        else
            return "object";
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }
}
