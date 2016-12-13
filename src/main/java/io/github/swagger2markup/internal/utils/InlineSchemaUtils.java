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
package io.github.swagger2markup.internal.utils;


import io.github.swagger2markup.internal.type.*;

import java.util.List;

import static org.apache.commons.collections4.MapUtils.isNotEmpty;

public class InlineSchemaUtils {
    /**
     * Returns a RefType to a new inlined type named with {@code name} and {@code uniqueName}.<br>
     * The returned RefType point to the new inlined type which is added to the {@code inlineDefinitions} collection.<br>
     * The function is recursive and support collections (ArrayType and MapType).<br>
     * The function is transparent : {@code type} is returned as-is if type is not inlinable or if !config.isInlineSchemaEnabled().<br>
     *
     * @param type              type to inline
     * @param name              name of the created inline ObjectType
     * @param uniqueName        unique name of the created inline ObjectType
     * @param inlineDefinitions a non null collection of inline ObjectType
     * @return the type referencing the newly created inline ObjectType. Can be a RefType, an ArrayType or a MapType
     */
    public static Type createInlineType(Type type, String name, String uniqueName, List<ObjectType> inlineDefinitions) {
        if (type instanceof ObjectType) {
            return createInlineObjectType(type, name, uniqueName, inlineDefinitions);
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            arrayType.setOfType(createInlineType(arrayType.getOfType(), name, uniqueName, inlineDefinitions));

            return arrayType;
        } else if (type instanceof MapType) {
            MapType mapType = (MapType) type;
            if (mapType.getValueType() instanceof ObjectType)
                mapType.setValueType(createInlineType(mapType.getValueType(), name, uniqueName, inlineDefinitions));

            return mapType;
        } else {
            return type;
        }
    }

    private static Type createInlineObjectType(Type type, String name, String uniqueName, List<ObjectType> inlineDefinitions) {
        if (type instanceof ObjectType) {
            ObjectType objectType = (ObjectType) type;
            if (isNotEmpty(objectType.getProperties())) {
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
