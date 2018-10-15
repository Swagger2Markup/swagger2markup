/*
 * Copyright 2017 Robert Winkler
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
package io.github.swagger2markup.internal.adapter;

import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.ArrayType;
import io.github.swagger2markup.internal.type.BasicType;
import io.github.swagger2markup.internal.type.EnumType;
import io.github.swagger2markup.internal.type.MapType;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.RefType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.internal.utils.ExamplesUtil;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.properties.AbstractNumericProperty;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class PropertyAdapter {

    private final Property property;
    private static Logger logger = LoggerFactory.getLogger(PropertyAdapter.class);

    public PropertyAdapter(Property property) {
        Validate.notNull(property, "property must not be null");
        this.property = property;
    }

    /**
     * Generate a default example value for property.
     *
     * @param property         property
     * @param markupDocBuilder doc builder
     * @return a generated example for the property
     */
    public static Object generateExample(Property property, MarkupDocBuilder markupDocBuilder) {

        if (property.getType() == null) {
            return "untyped";
        }

        switch (property.getType()) {
            case "integer":
                return ExamplesUtil.generateIntegerExample(property instanceof IntegerProperty ? ((IntegerProperty) property).getEnum() : null);
            case "number":
                return 0.0;
            case "boolean":
                return true;
            case "string":
                return ExamplesUtil.generateStringExample(property.getFormat(), property instanceof StringProperty ? ((StringProperty) property).getEnum() : null);
            case "ref":
                if (property instanceof RefProperty) {
                    if (logger.isDebugEnabled()) logger.debug("generateExample RefProperty for " + property.getName());
                    return markupDocBuilder.copy(false).crossReference(((RefProperty) property).getSimpleRef()).toString();
                } else {
                    if (logger.isDebugEnabled()) logger.debug("generateExample for ref not RefProperty");
                }
            case "array":
                if (property instanceof ArrayProperty) {
                    return generateArrayExample((ArrayProperty) property, markupDocBuilder);
                }
            default:
                return property.getType();
        }
    }

    /**
     * Generate example for an ArrayProperty
     *
     * @param property ArrayProperty to generate example for
     * @param markupDocBuilder MarkupDocBuilder containing all associated settings
     * @return String example
     */
    private static Object generateArrayExample(ArrayProperty property, MarkupDocBuilder markupDocBuilder) {
        Property itemProperty = property.getItems();
        List<Object> exampleArray = new ArrayList<>();

        exampleArray.add(generateExample(itemProperty, markupDocBuilder));
        return exampleArray;
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

    /**
     * Retrieves the type and format of a property.
     *
     * @param definitionDocumentResolver the definition document resolver
     * @return the type of the property
     */
    public Type getType(DocumentResolver definitionDocumentResolver) {
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
            else {
                Type arrayType = new PropertyAdapter(items).getType(definitionDocumentResolver);
                if (arrayType == null)
                    type = new ArrayType(arrayProperty.getTitle(), new ObjectType(null, null)); // FIXME : Workaround for Swagger parser issue with composed models (https://github.com/Swagger2Markup/swagger2markup/issues/150)
                else
                    type = new ArrayType(arrayProperty.getTitle(), new PropertyAdapter(items).getType(definitionDocumentResolver));
            }
        } else if (property instanceof MapProperty) {
            MapProperty mapProperty = (MapProperty) property;
            Property additionalProperties = mapProperty.getAdditionalProperties();
            if (additionalProperties == null)
                type = new MapType(mapProperty.getTitle(), new ObjectType(null, null)); // FIXME : Workaround for Swagger parser issue with composed models (https://github.com/Swagger2Markup/swagger2markup/issues/150)
            else
                type = new MapType(mapProperty.getTitle(), new PropertyAdapter(additionalProperties).getType(definitionDocumentResolver));
        } else if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            List<String> enums = stringProperty.getEnum();
            if (CollectionUtils.isNotEmpty(enums)) {
                type = new EnumType(stringProperty.getTitle(), enums);
            } else if (isNotBlank(stringProperty.getFormat())) {
                type = new BasicType(stringProperty.getType(), stringProperty.getTitle(), stringProperty.getFormat());
            } else {
                type = new BasicType(stringProperty.getType(), stringProperty.getTitle());
            }
        } else if (property instanceof ObjectProperty) {
            type = new ObjectType(property.getTitle(), ((ObjectProperty) property).getProperties());
        } else if (property instanceof IntegerProperty) {
            IntegerProperty integerProperty = (IntegerProperty) property;
            List<Integer> enums = integerProperty.getEnum();
            if (CollectionUtils.isNotEmpty(enums)) {
                // first, convert integer enum values to strings
                List<String> enumValuesAsString = enums.stream().map(String::valueOf).collect(Collectors.toList());
                type = new EnumType(integerProperty.getTitle(), enumValuesAsString);
            } else if (isNotBlank(integerProperty.getFormat())) {
                type = new BasicType(integerProperty.getType(), integerProperty.getTitle(), integerProperty.getFormat());
            } else {
                type = new BasicType(property.getType(), property.getTitle());
            }
        } else {
            if (property.getType() == null) {
                return null;
            } else if (isNotBlank(property.getFormat())) {
                type = new BasicType(property.getType(), property.getTitle(), property.getFormat());
            } else {
                type = new BasicType(property.getType(), property.getTitle());
            }
        }
        return type;
    }

    /**
     * Retrieves the default value of a property
     *
     * @return the default value of the property
     */
    public Optional<Object> getDefaultValue() {
        if (property instanceof BooleanProperty) {
            BooleanProperty booleanProperty = (BooleanProperty) property;
            return Optional.ofNullable(booleanProperty.getDefault());
        } else if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            return Optional.ofNullable(stringProperty.getDefault());
        } else if (property instanceof DoubleProperty) {
            DoubleProperty doubleProperty = (DoubleProperty) property;
            return Optional.ofNullable(doubleProperty.getDefault());
        } else if (property instanceof FloatProperty) {
            FloatProperty floatProperty = (FloatProperty) property;
            return Optional.ofNullable(floatProperty.getDefault());
        } else if (property instanceof IntegerProperty) {
            IntegerProperty integerProperty = (IntegerProperty) property;
            return Optional.ofNullable(integerProperty.getDefault());
        } else if (property instanceof LongProperty) {
            LongProperty longProperty = (LongProperty) property;
            return Optional.ofNullable(longProperty.getDefault());
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            return Optional.ofNullable(uuidProperty.getDefault());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the minLength of a property
     *
     * @return the minLength of the property
     */
    public Optional<Integer> getMinlength() {
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            return Optional.ofNullable(stringProperty.getMinLength());
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            return Optional.ofNullable(uuidProperty.getMinLength());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the maxLength of a property
     *
     * @return the maxLength of the property
     */
    public Optional<Integer> getMaxlength() {
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            return Optional.ofNullable(stringProperty.getMaxLength());
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            return Optional.ofNullable(uuidProperty.getMaxLength());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the pattern of a property
     *
     * @return the pattern of the property
     */
    public Optional<String> getPattern() {
        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            return Optional.ofNullable(stringProperty.getPattern());
        } else if (property instanceof UUIDProperty) {
            UUIDProperty uuidProperty = (UUIDProperty) property;
            return Optional.ofNullable(uuidProperty.getPattern());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the minimum value of a property
     *
     * @return the minimum value of the property
     */
    public Optional<BigDecimal> getMin() {
        if (property instanceof BaseIntegerProperty) {
            BaseIntegerProperty integerProperty = (BaseIntegerProperty) property;
            return Optional.ofNullable(integerProperty.getMinimum() != null ? integerProperty.getMinimum() : null);
        } else if (property instanceof AbstractNumericProperty) {
            AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
            return Optional.ofNullable(numericProperty.getMinimum());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the exclusiveMinimum value of a property
     *
     * @return the exclusiveMinimum value of the property
     */
    public boolean getExclusiveMin() {
        if (property instanceof AbstractNumericProperty) {
            AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
            return BooleanUtils.isTrue(numericProperty.getExclusiveMinimum());
        }
        return false;
    }

    /**
     * Retrieves the minimum value of a property
     *
     * @return the minimum value of the property
     */
    public Optional<BigDecimal> getMax() {
        if (property instanceof BaseIntegerProperty) {
            BaseIntegerProperty integerProperty = (BaseIntegerProperty) property;
            return Optional.ofNullable(integerProperty.getMaximum() != null ? integerProperty.getMaximum() : null);
        } else if (property instanceof AbstractNumericProperty) {
            AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
            return Optional.ofNullable(numericProperty.getMaximum());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the exclusiveMaximum value of a property
     *
     * @return the exclusiveMaximum value of the property
     */
    public boolean getExclusiveMax() {
        if (property instanceof AbstractNumericProperty) {
            AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
            return BooleanUtils.isTrue((numericProperty.getExclusiveMaximum()));
        }
        return false;
    }

    /**
     * Return example display string for the given {@code property}.
     *
     * @param generateMissingExamples specifies if missing examples should be generated
     * @param markupDocBuilder        doc builder
     * @return property example display string
     */
    public Optional<Object> getExample(boolean generateMissingExamples, MarkupDocBuilder markupDocBuilder) {
        if (property.getExample() != null) {
            return Optional.ofNullable(property.getExample());
        } else if (property instanceof MapProperty) {
            Property additionalProperty = ((MapProperty) property).getAdditionalProperties();
            if (additionalProperty.getExample() != null) {
                return Optional.ofNullable(additionalProperty.getExample());
            } else if (generateMissingExamples) {
                Map<String, Object> exampleMap = new HashMap<>();
                exampleMap.put("string", generateExample(additionalProperty, markupDocBuilder));
                return Optional.of(exampleMap);
            }
        } else if (property instanceof ArrayProperty) {
            if (generateMissingExamples) {
                Property itemProperty = ((ArrayProperty) property).getItems();
                List<Object> exampleArray = new ArrayList<>();
                exampleArray.add(generateExample(itemProperty, markupDocBuilder));
                return Optional.of(exampleArray);
            }
        } else if (generateMissingExamples) {
            return Optional.of(generateExample(property, markupDocBuilder));
        }

        return Optional.empty();
    }

    /**
     * Checks if a property is read-only.
     *
     * @return true if the property is read-only
     */
    public boolean getReadOnly() {
        return BooleanUtils.isTrue(property.getReadOnly());
    }
}
