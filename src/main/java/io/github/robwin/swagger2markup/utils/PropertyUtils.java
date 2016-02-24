/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup.utils;

import com.google.common.base.Function;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.type.ArrayType;
import io.github.robwin.swagger2markup.type.BasicType;
import io.github.robwin.swagger2markup.type.EnumType;
import io.github.robwin.swagger2markup.type.ObjectType;
import io.github.robwin.swagger2markup.type.RefType;
import io.github.robwin.swagger2markup.type.Type;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;
import io.swagger.models.refs.RefFormat;
import io.swagger.util.Json;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class PropertyUtils {

    /**
     * Retrieves the type and format of a property.
     *
     * @param property the property
     * @return the type of the property
     */
    public static Type getType(Property property, Function<String, String> definitionDocumentResolver){
        Validate.notNull(property, "property must not be null!");
        Type type;
        if(property instanceof RefProperty){
            RefProperty refProperty = (RefProperty)property;
            if (refProperty.getRefFormat() == RefFormat.RELATIVE)
                type = new ObjectType(null, null); // FIXME : Workaround for https://github.com/swagger-api/swagger-parser/issues/177
            else
                type = new RefType(definitionDocumentResolver.apply(refProperty.getSimpleRef()), refProperty.getSimpleRef());
        }else if(property instanceof ArrayProperty){
            ArrayProperty arrayProperty = (ArrayProperty)property;
            Property items = arrayProperty.getItems();
            type = new ArrayType(null, getType(items, definitionDocumentResolver));
        }else if(property instanceof StringProperty){
            StringProperty stringProperty = (StringProperty)property;
            List<String> enums = stringProperty.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = new EnumType(null, enums);
            }else{
                type = new BasicType(property.getType());
            }
        }else if(property instanceof ObjectProperty) {
          type = new ObjectType(null, ((ObjectProperty) property).getProperties());
        }
        else{
            if(isNotBlank(property.getFormat())){
                type = new BasicType(property.getType(), property.getFormat());
            }else{
                type = new BasicType(property.getType());
            }
        }
        return type;
    }

    /**
     * Retrieves the default value of a property, or otherwise returns an empty String.
     *
     * @param property the property
     * @return the default value of the property, or otherwise an empty String
     */
    public static String getDefaultValue(Property property){
        Validate.notNull(property, "property must not be null!");
        String defaultValue = "";
        if(property instanceof BooleanProperty){
            BooleanProperty booleanProperty = (BooleanProperty)property;
            defaultValue = Objects.toString(booleanProperty.getDefault(), "");
        }else if(property instanceof StringProperty){
            StringProperty stringProperty = (StringProperty)property;
            defaultValue = Objects.toString(stringProperty.getDefault(), "");
        }else if(property instanceof DoubleProperty){
            DoubleProperty doubleProperty = (DoubleProperty)property;
            defaultValue = Objects.toString(doubleProperty.getDefault(), "");
        }else if(property instanceof FloatProperty){
            FloatProperty floatProperty = (FloatProperty)property;
            defaultValue = Objects.toString(floatProperty.getDefault(), "");
        }else if(property instanceof IntegerProperty){
            IntegerProperty integerProperty = (IntegerProperty)property;
            defaultValue = Objects.toString(integerProperty.getDefault(), "");
        }
        else if(property instanceof LongProperty){
            LongProperty longProperty = (LongProperty)property;
            defaultValue = Objects.toString(longProperty.getDefault(), "");
        }
        else if(property instanceof UUIDProperty){
            UUIDProperty uuidProperty = (UUIDProperty)property;
            defaultValue = Objects.toString(uuidProperty.getDefault(), "");
        }
        return defaultValue;
    }

    public static String getExample(Property property, MarkupDocBuilder markupDocBuilder) {
        Validate.notNull(property, "parameter must not be null!");
        Object examplesValue;
        if (property.getExample() != null) {
            examplesValue = property.getExample();
        } else if (property instanceof MapProperty) {
            Map<String, Object> exampleMap = new HashMap<>();
            Property additionalProperty = ((MapProperty) property).getAdditionalProperties();
            if (additionalProperty.getExample() != null) {
                examplesValue = additionalProperty.getExample();
            } else {
                exampleMap.put("string", exampleFromType(additionalProperty.getType(), additionalProperty, markupDocBuilder));
                examplesValue = Json.pretty(exampleMap);
            }
        } else if (property instanceof ArrayProperty) {
            Property itemProperty = ((ArrayProperty) property).getItems();
            examplesValue = "[ " + exampleFromType(itemProperty.getType(), itemProperty, markupDocBuilder) + " ]";
        } else {
            examplesValue = exampleFromType(property.getType(), property, markupDocBuilder);
        }
        return String.valueOf(examplesValue);
    }

    public static Object exampleFromType(String type, Property property, MarkupDocBuilder markupDocBuilder) {
        switch (type) {
            case "integer":
                return 0;
            case "number":
                return 0.0;
            case "boolean":
                return true;
            case "string":
                return  "string";
            case "ref":
                if (property != null && property instanceof RefProperty) {
                    return markupDocBuilder.copy().crossReference(((RefProperty) property).getSimpleRef()).toString();
                }
            default:
                return type;
        }
    }
}
