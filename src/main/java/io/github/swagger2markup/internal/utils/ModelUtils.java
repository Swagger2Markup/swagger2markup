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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import io.github.swagger2markup.internal.type.*;
import io.swagger.models.*;
import io.swagger.models.properties.Property;
import io.swagger.models.refs.RefFormat;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

public final class ModelUtils {

    /**
     * Retrieves the specified {@code type} properties if any, and recursively follows RefType
     * @param type retrieve properties from type
     * @return dereferenced type properties, or null if type has no properties
     */
    public static Map<String, Property> getTypeProperties(Type type) {
        Map<String, Property> properties = null;
        
        if (type instanceof ObjectType) {
            properties = ((ObjectType) type).getProperties();
        } else if (type instanceof RefType) {
            properties = getTypeProperties(((RefType) type).getRefType());
        }
        
        if (properties != null && !org.apache.commons.collections4.MapUtils.isEmpty(properties))
            return ImmutableMap.copyOf(properties);
        else
            return null;
    }

    /**
     * Retrieves the type of a model, or otherwise null
     *
     * @param model                      the model
     * @param definitionDocumentResolver the definition document resolver
     * @return the type of the model, or otherwise null
     */
    public static Type getType(Model model, Map<String, Model> definitions, Function<String, String> definitionDocumentResolver) {
        Validate.notNull(model, "model must not be null!");
        if (model instanceof ModelImpl) {
            ModelImpl modelImpl = (ModelImpl) model;

            if (modelImpl.getAdditionalProperties() != null)
                return new MapType(null, PropertyUtils.getType(modelImpl.getAdditionalProperties(), definitionDocumentResolver));
            else if (modelImpl.getEnum() != null)
                return new EnumType(null, modelImpl.getEnum());
            else if (modelImpl.getProperties() != null)
                return new ObjectType(null, model.getProperties());
            else
                return new BasicType(((ModelImpl) model).getType());
        } else if (model instanceof ComposedModel) {
            ComposedModel composedModel = (ComposedModel) model;
            Map<String, Property> allProperties = new HashMap<>();
            if (composedModel.getAllOf() != null) {
                for (Model innerModel : composedModel.getAllOf()) {
                    Type innerModelType = getType(innerModel, definitions, definitionDocumentResolver);
                    Map<String, Property> innerModelProperties = getTypeProperties(innerModelType);

                    if (innerModelProperties != null)
                        allProperties.putAll(innerModelProperties);
                }
            }
            return new ObjectType(null, allProperties);
        } else if (model instanceof RefModel) {
            RefModel refModel = (RefModel) model;
            String refName = refModel.getRefFormat().equals(RefFormat.INTERNAL) ? refModel.getSimpleRef() : refModel.getReference();
            
            Type refType = new ObjectType(refName, null);
            if (definitions.containsKey(refName))
                refType = getType(definitions.get(refName), definitions, definitionDocumentResolver);
            
            return new RefType(definitionDocumentResolver.apply(refName), refName, refName, refType);
        } else if (model instanceof ArrayModel) {
            ArrayModel arrayModel = ((ArrayModel) model);

            return new ArrayType(null, PropertyUtils.getType(arrayModel.getItems(), definitionDocumentResolver));
        }

        return null;
    }
}
