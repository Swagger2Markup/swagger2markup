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
package io.github.swagger2markup.internal.adapter;

import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.*;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.properties.*;
import io.swagger.models.refs.RefFormat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class PropertyAdapter {

    private final Property property;

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
            else
                type = new ArrayType(arrayProperty.getTitle(), new PropertyAdapter(items).getType(definitionDocumentResolver));
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
    public Optional<Number> getMin() {
        if (property instanceof BaseIntegerProperty) {
            BaseIntegerProperty integerProperty = (BaseIntegerProperty) property;
            return Optional.ofNullable(integerProperty.getMinimum() != null ? integerProperty.getMinimum().longValue() : null);
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
    public Optional<Number> getMax() {
        if (property instanceof BaseIntegerProperty) {
            BaseIntegerProperty integerProperty = (BaseIntegerProperty) property;
            return Optional.ofNullable(integerProperty.getMaximum() != null ? integerProperty.getMaximum().longValue() : null);
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
