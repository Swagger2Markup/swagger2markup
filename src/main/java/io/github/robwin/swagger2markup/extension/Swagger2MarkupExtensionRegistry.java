package io.github.robwin.swagger2markup.extension;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.github.robwin.swagger2markup.extension.repository.DynamicDefinitionsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOperationsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOverviewContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicSecurityContentExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Swagger2MarkupExtensionRegistry {

    protected static final List<Class<? extends Extension>> EXTENSION_POINTS = Arrays.<Class<? extends Extension>>asList(
            SwaggerExtension.class,
            OverviewContentExtension.class,
            SecurityContentExtension.class,
            DefinitionsContentExtension.class,
            OperationsContentExtension.class
    );

    protected final Multimap<Class<? extends Extension>, Extension> extensions;

    public Swagger2MarkupExtensionRegistry() {
        extensions = MultimapBuilder.hashKeys().arrayListValues().build();
    }

    public static Builder ofEmpty() {
        return new Builder(false);
    }

    public static Builder ofDefaults() {
        return new Builder(true);
    }

    public static class Builder {

        Swagger2MarkupExtensionRegistry registry = new Swagger2MarkupExtensionRegistry();

        public Builder(boolean useDefaults) {
            if (useDefaults) {
                withExtension(new DynamicOverviewContentExtension());
                withExtension(new DynamicSecurityContentExtension());
                withExtension(new DynamicOperationsContentExtension());
                withExtension(new DynamicDefinitionsContentExtension());
            }
        }

        public Swagger2MarkupExtensionRegistry build() {
            return registry;
        }

        public Builder withExtension(Extension extension) {
            registry.registerExtension(extension);
            return this;
        }
    }

    public void registerExtension(Extension extension) {
        for (Class<? extends Extension> extensionPoint : EXTENSION_POINTS) {
            if (extensionPoint.isInstance(extension)) {
                extensions.put(extensionPoint, extension);
                return;
            }
        }

        throw new IllegalArgumentException("Provided extension class does not extend any of the supported extension points");
    }

    @SuppressWarnings(value = "unchecked")
    public <T extends Extension> List<T> getExtensions(Class<T> extensionClass) {
        List<T> ret = new ArrayList<>();

        for (Map.Entry<Class<? extends Extension>, Extension> entry : extensions.entries()) {
            if (extensionClass.isAssignableFrom(entry.getKey())) {
                if (extensionClass.isInstance(entry.getValue()))
                    ret.add((T) entry.getValue());
            }
        }

        return ret;
    }

    /**
     * Get all extensions
     * @return all extensions
     */
    public List<Extension> getExtensions() {
        return getExtensions(Extension.class);
    }
}
