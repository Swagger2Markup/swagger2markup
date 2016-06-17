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
import io.github.swagger2markup.internal.type.*;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.properties.*;
import io.swagger.models.refs.RefFormat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class PropertyUtils {

    /**
     * Retrieves the type and format of a property.
     *
     * @param property the property
     * @param definitionDocumentResolver the definition document resolver
     * @return the type of the property
     */
    public static Type getType(Property property, Function<String, String> definitionDocumentResolver) {
        Validate.notNull(property, "property must not be null");
        Type type;
        if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            if (refProperty.getRefFormat() == RefFormat.RELATIVE)
                type = new ObjectType(refProperty.getTitle(), null); // FIXME : Workaround for https://github.com/swagger-api/swagger-parser/issues/177
            else
                type = new RefType(definitionDocumentResolver.apply(refProperty.getSimpleRef()), new ObjectType(refProperty.getSimpleRef(), null /* FIXME, not used for now */));
        } else if (property instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) property;
            Property items = arrayProperty.getItems();
            if (items == null)
                type = new ArrayType(arrayProperty.getTitle(), new ObjectType(null, null)); // FIXME : Workaround for Swagger parser issue with composed models (https://github.com/Swagger2Markup/swagger2markup/issues/150)
            else
                type = new ArrayType(arrayProperty.getTitle(), getType(items, definitionDocumentResolver));
        } else if (property instanceof MapProperty) {
            MapProperty mapProperty = (MapProperty) property;
            Property additionalProperties = mapProperty.getAdditionalProperties();
            if (additionalProperties == null)
                type = new MapType(mapProperty.getTitle(), new ObjectType(null, null)); // FIXME : Workaround for Swagger parser issue with composed models (https://github.com/Swagger2Markup/swagger2markup/issues/150)
            else
                type = new MapType(mapProperty.getTitle(), getType(additionalProperties, definitionDocumentResolver));
        } else if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            List<String> enums = stringProperty.getEnum();
            if (CollectionUtils.isNotEmpty(enums)) {
                type = new EnumType(stringProperty.getTitle(), enums);
            } else {
                type = new BasicType(stringProperty.getType(), stringProperty.getTitle());
            }
        } else if (property instanceof ObjectProperty) {
            type = new ObjectType(property.getTitle(), ((ObjectProperty) property).getProperties());
        } else {
            if (isNotBlank(property.getFormat())) {
                type = new BasicType(property.getType(), property.getTitle(), property.getFormat());
            } else {
                type = new BasicType(property.getType(), property.getTitle());
            }
        }
        return type;
    }

    /**
     * Retrieves the default value of a property, or otherwise returns null.
     *
     * @param property the property
     * @return the default value of the property, or otherwise null
     */
    public static Object getDefaultValue(Property property) {
        Validate.notNull(property, "property must not be null");
        Object defaultValue = null;
        
        if (property instanceof BooleanProperty) {
            BooleanProperty booleanProperty = (BooleanProperty) property;
            defaultValue = booleanProperty.getDefault();
        } else if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            defaultValue = stringProperty.getDefault();
        } else if (property instanceof DoubleProperty) {
            DoubleProperty doubleProperty = (DoubleProperty) property;
            defaultValue = doubleProperty.getDefault();
        } else if (property instanceof FloatProperty) {
            FloatProperty floatProperty = (FloatProperty) property;
            defaultValue = floatProperty.getDefault();
        } else if (property instanceof IntegerProperty) {
            IntegerProperty integerProperty = (IntegerProperty) property;
            defaultValue = integerProperty.getDefault();
        } else if (property instanceof LongProperty) {
            LongProperty longProperty = (LongProperty) property;
            defaultValue = longProperty.getDefault();
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            defaultValue = uuidProperty.getDefault();
        }
        return defaultValue;
    }
    

    /**
     * Retrieves the minLength of a property, or otherwise returns null.
     *
     * @param property the property
     * @return the minLength of the property, or otherwise null
     */
    public static Integer getMinlength(Property property) {
        Validate.notNull(property, "property must not be null");
        Integer minLength = null;
        
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            minLength = stringProperty.getMinLength();
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            minLength = uuidProperty.getMinLength();
        }
        return minLength;
    }
    

    /**
     * Retrieves the maxLength of a property, or otherwise returns null.
     *
     * @param property the property
     * @return the maxLength of the property, or otherwise null
     */
    public static Integer getMaxlength(Property property) {
        Validate.notNull(property, "property must not be null");
        Integer maxLength = null;
        
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            maxLength = stringProperty.getMaxLength();
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            maxLength = uuidProperty.getMaxLength();
        }
        return maxLength;
    }
    
    /**
     * Retrieves the pattern of a property, or otherwise returns null.
     *
     * @param property the property
     * @return the pattern of the property, or otherwise null
     */
    public static String getPattern(Property property) {
        Validate.notNull(property, "property must not be null");
        String pattern = null;
        
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            pattern = stringProperty.getPattern();
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            pattern = uuidProperty.getPattern();
        }
        return pattern;
    }
    
    /**
     * Retrieves the minimum value of a property, or otherwise returns null.
     *
     * @param property the property
     * @return the minimum value of the property, or otherwise null
     */
    public static Double getMin(Property property) {
        Validate.notNull(property, "property must not be null");
        Double min = null;
        
        if (property instanceof DoubleProperty) {
            DoubleProperty doubleProperty = (DoubleProperty) property;
            min = doubleProperty.getMinimum();
        } else if (property instanceof FloatProperty) {
            FloatProperty floatProperty = (FloatProperty) property;
            min = floatProperty.getMinimum();
        } else if (property instanceof IntegerProperty) {
            IntegerProperty integerProperty = (IntegerProperty) property;
            min = integerProperty.getMinimum();
        } else if (property instanceof LongProperty) {
            LongProperty longProperty = (LongProperty) property;
            min = longProperty.getMinimum();
        }
        return min;
    }
    
    /**
     * Retrieves the minimum value of a property, or otherwise returns null.
     *
     * @param property the property
     * @return the minimum value of the property, or otherwise null
     */
    public static Double getMax(Property property) {
        Validate.notNull(property, "property must not be null");
        Double max = null;
        
        if (property instanceof DoubleProperty) {
            DoubleProperty doubleProperty = (DoubleProperty) property;
            max = doubleProperty.getMaximum();
        } else if (property instanceof FloatProperty) {
            FloatProperty floatProperty = (FloatProperty) property;
            max = floatProperty.getMaximum();
        } else if (property instanceof IntegerProperty) {
            IntegerProperty integerProperty = (IntegerProperty) property;
            max = integerProperty.getMaximum();
        } else if (property instanceof LongProperty) {
            LongProperty longProperty = (LongProperty) property;
            max = longProperty.getMaximum();
        }
        return max;
    }
    
    /**
     * Return example display string for the given {@code property}.
     *
     * @param generateMissingExamples specifies if missing examples should be generated
     * @param property         property
     * @param markupDocBuilder doc builder
     * @return property example display string
     */
    public static Object getExample(boolean generateMissingExamples, Property property, MarkupDocBuilder markupDocBuilder) {
        Validate.notNull(property, "property must not be null");
        Object examplesValue = null;
        if (property.getExample() != null) {
            examplesValue = property.getExample();
        } else if (property instanceof MapProperty) {
            Property additionalProperty = ((MapProperty) property).getAdditionalProperties();
            if (additionalProperty.getExample() != null) {
                examplesValue = additionalProperty.getExample();
            } else if (generateMissingExamples) {
                Map<String, Object> exampleMap = new HashMap<>();
                exampleMap.put("string", generateExample(additionalProperty, markupDocBuilder));
                examplesValue = exampleMap;
            }
        } else if (property instanceof ArrayProperty) {
            if (generateMissingExamples) {
                Property itemProperty = ((ArrayProperty) property).getItems();
                List<Object> exampleArray = new ArrayList<>();
                exampleArray.add(generateExample(itemProperty, markupDocBuilder));
                examplesValue = exampleArray;
            }
        } else if (generateMissingExamples) {
            examplesValue = generateExample(property, markupDocBuilder);
        }

        return examplesValue;
    }

    /**
     * Generate a default example value for property.
     *
     * @param property         property
     * @param markupDocBuilder doc builder
     * @return a generated example for the property
     */
    public static Object generateExample(Property property, MarkupDocBuilder markupDocBuilder) {
        switch (property.getType()) {
            case "integer":
                return 0;
            case "number":
                return 0.0;
            case "boolean":
                return true;
            case "string":
                return "string";
            case "ref":
                if (property instanceof RefProperty) {
                    return markupDocBuilder.copy(false).crossReference(((RefProperty) property).getSimpleRef()).toString();
                }
            default:
                return property.getType();
        }
    }

    /**
     * Convert a string {@code value} to specified {@code type}.
     *
     * @param value value to convert
     * @param type  target conversion type
     * @return converted value as object
     */
    public static Object convertExample(String value, String type) {
        if (value == null) {
            return null;
        }

        try {
            switch (type) {
                case "integer":
                    return Integer.valueOf(value);
                case "number":
                    return Float.valueOf(value);
                case "boolean":
                    return Boolean.valueOf(value);
                case "string":
                    return value;
                default:
                    return value;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Value '%s' cannot be converted to '%s'", value, type), e);
        }
    }
}
