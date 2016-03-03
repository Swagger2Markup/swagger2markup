/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public Swagger2MarkupExtensionRegistry(Multimap<Class<? extends Extension>, Extension> extensions) {
        this.extensions = extensions;
    }

    public static Builder ofEmpty() {
        return new Builder(false);
    }

    public static Builder ofDefaults() {
        return new Builder(true);
    }

    public static class Builder {

        private final Multimap<Class<? extends Extension>, Extension> extensions;

        Builder(boolean useDefaults) {
            extensions = MultimapBuilder.hashKeys().arrayListValues().build();
            if (useDefaults) {
                withExtension(new DynamicOverviewContentExtension());
                withExtension(new DynamicSecurityContentExtension());
                withExtension(new DynamicOperationsContentExtension());
                withExtension(new DynamicDefinitionsContentExtension());
            }
        }

        public Swagger2MarkupExtensionRegistry build() {
            return new Swagger2MarkupExtensionRegistry(extensions);
        }

        public Builder withExtension(Extension extension) {
            registerExtension(extension);
            return this;
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
