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

package io.github.swagger2markup.internal.component;

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupExtensionRegistry;
import io.github.swagger2markup.internal.type.*;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;
import java.util.ResourceBundle;

public abstract class MarkupComponent {

    Logger logger = LoggerFactory.getLogger(getClass());

    static final String COLON = " : ";

    Context context;
    ResourceBundle labels;
    MarkupDocBuilder markupDocBuilder;
    Swagger2MarkupConfig config;
    Swagger2MarkupExtensionRegistry extensionRegistry;

    MarkupComponent(Context context){
        this.context = context;
        this.config = context.getConfig();
        this.markupDocBuilder = context.getMarkupDocBuilder();
        this.labels = ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getOutputLanguage().toLocale());
        this.extensionRegistry = context.getExtensionRegistry();
    }

    public static class Context {
        private final Swagger2MarkupConfig config;
        private final MarkupDocBuilder markupDocBuilder;
        private final Swagger2MarkupExtensionRegistry extensionRegistry;

        public Context(Swagger2MarkupConfig config, MarkupDocBuilder markupDocBuilder, Swagger2MarkupExtensionRegistry extensionRegistry){
            this.config = config;
            this.markupDocBuilder = markupDocBuilder;
            this.extensionRegistry = extensionRegistry;
        }

        public Swagger2MarkupConfig getConfig() {
            return config;
        }

        public MarkupDocBuilder getMarkupDocBuilder() {
            return markupDocBuilder;
        }

        public Swagger2MarkupExtensionRegistry getExtensionRegistry() {
            return extensionRegistry;
        }
    }

    public abstract MarkupDocBuilder render();

    MarkupDocBuilder copyMarkupDocBuilder() {
        return markupDocBuilder.copy(false);
    }

    String literalText(String text) {
        return copyMarkupDocBuilder().literalText(text).toString();
    }

    String boldText(String text) {
        return copyMarkupDocBuilder().boldText(text).toString();
    }

    String italicText(String text) {
        return copyMarkupDocBuilder().italicText(text).toString();
    }

    /**
     * Returns converted markup text from Swagger.
     *
     * @param markupText text to convert, or empty string
     * @return converted markup text, or an empty string if {@code markupText} == null
     */
    String markupDescription(String markupText) {
        if (StringUtils.isBlank(markupText)) {
            return StringUtils.EMPTY;
        }
        return copyMarkupDocBuilder().importMarkup(new StringReader(markupText), config.getSwaggerMarkupLanguage()).toString().trim();
    }

    /**
     * Returns a RefType to a new inlined type named with {@code name} and {@code uniqueName}.<br>
     * The returned RefType point to the new inlined type which is added to the {@code inlineDefinitions} collection.<br>
     * The function is recursive and support collections (ArrayType and MapType).<br>
     * The function is transparent : {@code type} is returned as-is if type is not inlinable or if !config.isInlineSchemaEnabled().<br>
     *
     * @param type type to inline
     * @param name name of the created inline ObjectType
     * @param uniqueName unique name of the created inline ObjectType
     * @param inlineDefinitions a non null collection of inline ObjectType
     * @return the type referencing the newly created inline ObjectType. Can be a RefType, an ArrayType or a MapType
     */
    Type createInlineType(Type type, String name, String uniqueName, List<ObjectType> inlineDefinitions) {
        if (!config.isInlineSchemaEnabled())
            return type;

        if (type instanceof ObjectType) {
            return createInlineObjectType(type, name, uniqueName, inlineDefinitions);
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            arrayType.setOfType(createInlineType(arrayType.getOfType(), name, uniqueName, inlineDefinitions));

            return arrayType;
        } else if (type instanceof MapType) {
            MapType mapType = (MapType)type;
            if (mapType.getValueType() instanceof ObjectType)
                mapType.setValueType(createInlineType(mapType.getValueType(), name, uniqueName, inlineDefinitions));

            return mapType;
        } else {
            return type;
        }
    }

    private Type createInlineObjectType(Type type, String name, String uniqueName, List<ObjectType> inlineDefinitions) {
        if (type instanceof ObjectType) {
            ObjectType objectType = (ObjectType)type;
            if (MapUtils.isNotEmpty(objectType.getProperties())) {
                if (objectType.getName() == null) {
                    objectType.setName(name);
                    objectType.setUniqueName(uniqueName);
                }
                inlineDefinitions.add(objectType);
                return new RefType(objectType);
            } else
                return type;
        } else
            return type;
    }
}
