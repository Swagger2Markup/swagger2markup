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

package io.github.swagger2markup.internal.utils;

import io.github.swagger2markup.internal.adapter.ParameterAdapter;
import io.github.swagger2markup.internal.adapter.PropertyAdapter;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.models.utils.PropertyModelConverter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamplesUtil {

    private static final Integer MAX_RECURSION_TO_DISPLAY = 2;

    /**
     * Generates a Map of response examples
     *
     * @param generateMissingExamples specifies the missing examples should be generated
     * @param operation               the Swagger Operation
     * @param definitions             the map of definitions
     * @param markupDocBuilder        the markup builder
     * @return map containing response examples.
     */
    public static Map<String, Object> generateResponseExampleMap(boolean generateMissingExamples, PathOperation operation, Map<String, Model> definitions, DocumentResolver definitionDocumentResolver, MarkupDocBuilder markupDocBuilder) {
        Map<String, Object> examples = new LinkedHashMap<>();
        Map<String, Response> responses = operation.getOperation().getResponses();
        if (responses != null)
            for (Map.Entry<String, Response> responseEntry : responses.entrySet()) {
                Response response = responseEntry.getValue();
                Object example = response.getExamples();
                if (example == null) {
                    Model model = response.getResponseSchema();
                    if (model != null) {
                        Property schema = new PropertyModelConverter().modelToProperty(model);
                        if (schema != null) {
                            example = schema.getExample();

                            if (example == null && schema instanceof RefProperty) {
                                String simpleRef = ((RefProperty) schema).getSimpleRef();
                                example = generateExampleForRefModel(generateMissingExamples, simpleRef, definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                            }
                            if (example == null && schema instanceof ArrayProperty && generateMissingExamples) {
                                example = generateExampleForArrayProperty((ArrayProperty) schema, definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                            }
                            if (example == null && schema instanceof ObjectProperty && generateMissingExamples) {
                                example = exampleMapForProperties(((ObjectProperty) schema).getProperties(), definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                            }
                            if (example == null && generateMissingExamples) {
                                example = PropertyAdapter.generateExample(schema, markupDocBuilder);
                            }
                        }
                    }
                }

                if (example != null)
                    examples.put(responseEntry.getKey(), example);

            }

        return examples;
    }

    /**
     * Generates examples for request
     *
     * @param generateMissingExamples specifies the missing examples should be generated
     * @param pathOperation           the Swagger Operation
     * @param definitions             the map of definitions
     * @param markupDocBuilder        the markup builder
     * @return an Optional with the example content
     */
    public static Map<String, Object> generateRequestExampleMap(boolean generateMissingExamples, PathOperation pathOperation, Map<String, Model> definitions, DocumentResolver definitionDocumentResolver, MarkupDocBuilder markupDocBuilder) {
        Operation operation = pathOperation.getOperation();
        List<Parameter> parameters = operation.getParameters();
        Map<String, Object> examples = new LinkedHashMap<>();

        // Path example should always be included (if generateMissingExamples):
        if (generateMissingExamples)
            examples.put("path", pathOperation.getPath());
        for (Parameter parameter : parameters) {
            Object example = null;
            if (parameter instanceof BodyParameter) {
                example = getExamplesFromBodyParameter(parameter);
                if (example == null) {
                    Model schema = ((BodyParameter) parameter).getSchema();
                    if (schema instanceof RefModel) {
                        String simpleRef = ((RefModel) schema).getSimpleRef();
                        example = generateExampleForRefModel(generateMissingExamples, simpleRef, definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                    } else if (generateMissingExamples) {
                        if (schema instanceof ComposedModel) {
                            //FIXME: getProperties() may throw NullPointerException
                            example = exampleMapForProperties(((ObjectType) ModelUtils.getType(schema, definitions, definitionDocumentResolver)).getProperties(), definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                        } else if (schema instanceof ArrayModel) {
                            example = generateExampleForArrayModel((ArrayModel) schema, definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                        } else {
                            example = schema.getExample();
                            if (example == null) {
                                example = exampleMapForProperties(schema.getProperties(), definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                            }
                        }
                    }
                }
            } else if (parameter instanceof AbstractSerializableParameter) {
                if (generateMissingExamples) {
                    Object abstractSerializableParameterExample;
                    abstractSerializableParameterExample = ((AbstractSerializableParameter) parameter).getExample();
                    if (abstractSerializableParameterExample == null) {
                        abstractSerializableParameterExample = parameter.getVendorExtensions().get("x-example");
                    }
                    if (abstractSerializableParameterExample == null) {
                        Property item = ((AbstractSerializableParameter) parameter).getItems();
                        if (item != null) {
                            abstractSerializableParameterExample = item.getExample();
                            if (abstractSerializableParameterExample == null) {
                                abstractSerializableParameterExample = PropertyAdapter.generateExample(item, markupDocBuilder);
                            }
                        }
                        if (abstractSerializableParameterExample == null) {
                            abstractSerializableParameterExample = ParameterAdapter.generateExample((AbstractSerializableParameter) parameter);
                        }
                    }
                    if (parameter instanceof HeaderParameter){
                        example = parameter.getName() +":\"" +((HeaderParameter) parameter).getType()+ "\"";
                    } else if (parameter instanceof PathParameter) {
                        String pathExample = (String) examples.get("path");
                        pathExample = pathExample.replace('{' + parameter.getName() + '}', encodeExampleForUrl(abstractSerializableParameterExample));
                        example = pathExample;
                    } else if (parameter instanceof QueryParameter) {
                        if (parameter.getRequired())
                        {
                            String path = (String) examples.get("path");
                            String separator = path.contains("?") ? "&" : "?";
                            String pathExample = path + separator + parameter.getName() + "=" + encodeExampleForUrl(abstractSerializableParameterExample);
                            examples.put("path", pathExample);
                        }
                    } else {
                        example = abstractSerializableParameterExample;
                    }
                }
            } else if (parameter instanceof RefParameter) {
                String simpleRef = ((RefParameter) parameter).getSimpleRef();
                example = generateExampleForRefModel(generateMissingExamples, simpleRef, definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
            }

            if (example != null)
                examples.put(parameter.getIn(), example);
        }

        return examples;
    }

    /**
     * Retrieves example payloads for body parameter either from examples or from vendor extensions.
     * @param parameter parameter to get the examples for 
     * @return examples if found otherwise null
     */
    private static Object getExamplesFromBodyParameter(Parameter parameter) {
        Object examples = ((BodyParameter) parameter).getExamples();
        if (examples == null) {
            examples = parameter.getVendorExtensions().get("x-examples");
        }
        return examples;
    }

    /**
     * Encodes an example value for use in an URL
     *
     * @param example the example value
     * @return encoded example value
     */
    private static String encodeExampleForUrl(Object example) {
        try {
            return URLEncoder.encode(String.valueOf(example), "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Generates an example object from a simple reference
     *
     * @param generateMissingExamples specifies the missing examples should be generated
     * @param simpleRef               the simple reference string
     * @param definitions             the map of definitions
     * @param markupDocBuilder        the markup builder
     * @param refStack                map to detect cyclic references
     * @return returns an Object or Map of examples
     */
    private static Object generateExampleForRefModel(boolean generateMissingExamples, String simpleRef, Map<String, Model> definitions, DocumentResolver definitionDocumentResolver, MarkupDocBuilder markupDocBuilder, Map<String, Integer> refStack) {
        Model model = definitions.get(simpleRef);
        Object example = null;
        if (model != null) {
            example = model.getExample();
            if (example == null && generateMissingExamples) {
                if (!refStack.containsKey(simpleRef)) {
                    refStack.put(simpleRef, 1);
                } else {
                    refStack.put(simpleRef, refStack.get(simpleRef) + 1);
                }
                if (refStack.get(simpleRef) <= MAX_RECURSION_TO_DISPLAY) {
                    if (model instanceof ComposedModel) {
                        //FIXME: getProperties() may throw NullPointerException
                        example = exampleMapForProperties(((ObjectType) ModelUtils.getType(model, definitions, definitionDocumentResolver)).getProperties(), definitions, definitionDocumentResolver, markupDocBuilder, new HashMap<>());
                    } else {
                        example = exampleMapForProperties(model.getProperties(), definitions, definitionDocumentResolver, markupDocBuilder, refStack);
                    }
                } else {
                    return "...";
                }
                refStack.put(simpleRef, refStack.get(simpleRef) - 1);
            }
        }
        return example;
    }

    private static Map<String, Property> getPropertiesForComposedModel(ComposedModel model, Map<String, Model> definitions) {
        //TODO: Unused method, make sure this is never used and then remove it.
        Map<String, Property> combinedProperties;
        if (model.getParent() instanceof RefModel) {
            Map<String, Property> parentProperties = definitions.get(((RefModel) model.getParent()).getSimpleRef()).getProperties();
            if (parentProperties == null) {
                return null;
            } else {
                combinedProperties = new LinkedHashMap<>(parentProperties);
            }

        } else {
            combinedProperties = new LinkedHashMap<>(model.getParent().getProperties());
        }
        Map<String, Property> childProperties;
        if (model.getChild() instanceof RefModel) {
            childProperties = definitions.get(((RefModel) model.getChild()).getSimpleRef()).getProperties();
        } else {
            childProperties = model.getChild().getProperties();
        }
        if (childProperties != null) {
            combinedProperties.putAll(childProperties);
        }
        return combinedProperties;
    }

    /**
     * Generates a map of examples from a map of properties. If defined examples are found, those are used. Otherwise,
     * examples are generated from the type.
     *
     * @param properties       the map of properties
     * @param definitions      the map of definitions
     * @param markupDocBuilder the markup builder
     * @param refStack         map to detect cyclic references
     * @return a Map of examples
     */
    private static Map<String, Object> exampleMapForProperties(Map<String, Property> properties, Map<String, Model> definitions, DocumentResolver definitionDocumentResolver, MarkupDocBuilder markupDocBuilder, Map<String, Integer> refStack) {
        Map<String, Object> exampleMap = new LinkedHashMap<>();
        if (properties != null) {
            for (Map.Entry<String, Property> property : properties.entrySet()) {
                Object exampleObject = property.getValue().getExample();
                if (exampleObject == null) {
                    if (property.getValue() instanceof RefProperty) {
                        exampleObject = generateExampleForRefModel(true, ((RefProperty) property.getValue()).getSimpleRef(), definitions, definitionDocumentResolver, markupDocBuilder, refStack);
                    } else if (property.getValue() instanceof ArrayProperty) {
                        exampleObject = generateExampleForArrayProperty((ArrayProperty) property.getValue(), definitions, definitionDocumentResolver, markupDocBuilder, refStack);
                    } else if (property.getValue() instanceof MapProperty) {
                        exampleObject = generateExampleForMapProperty((MapProperty) property.getValue(), markupDocBuilder);
                    }
                    if (exampleObject == null) {
                        Property valueProperty = property.getValue();
                        exampleObject = PropertyAdapter.generateExample(valueProperty, markupDocBuilder);
                    }
                }
                exampleMap.put(property.getKey(), exampleObject);
            }
        }
        return exampleMap;
    }

    private static Object generateExampleForMapProperty(MapProperty property, MarkupDocBuilder markupDocBuilder) {
        if (property.getExample() != null) {
            return property.getExample();
        }
        Map<String, Object> exampleMap = new LinkedHashMap<>();
        Property valueProperty = property.getAdditionalProperties();
        if (valueProperty.getExample() != null) {
            return valueProperty.getExample();
        }
        exampleMap.put("string", PropertyAdapter.generateExample(valueProperty, markupDocBuilder));
        return exampleMap;
    }

    private static Object generateExampleForArrayModel(ArrayModel model, Map<String, Model> definitions, DocumentResolver definitionDocumentResolver, MarkupDocBuilder markupDocBuilder, Map<String, Integer> refStack) {
        if (model.getExample() != null) {
            return model.getExample();
        } else if (model.getProperties() != null) {
            return new Object[]{exampleMapForProperties(model.getProperties(), definitions, definitionDocumentResolver, markupDocBuilder, refStack)};
        } else {
            Property itemProperty = model.getItems();
            return getExample(itemProperty, definitions, definitionDocumentResolver, markupDocBuilder, refStack);
        }
    }

    /**
     * Generates examples from an ArrayProperty
     *
     * @param value            ArrayProperty
     * @param definitions      map of definitions
     * @param markupDocBuilder the markup builder
     * @return array of Object
     */
    private static Object[] generateExampleForArrayProperty(ArrayProperty value, Map<String, Model> definitions, DocumentResolver definitionDocumentResolver, MarkupDocBuilder markupDocBuilder, Map<String, Integer> refStack) {
        Property property = value.getItems();
        return getExample(property, definitions, definitionDocumentResolver, markupDocBuilder, refStack);
    }

    /**
     * Get example from a property
     *
     * @param property                   Property
     * @param definitions                map of definitions
     * @param definitionDocumentResolver DocumentResolver
     * @param markupDocBuilder           the markup builder
     * @param refStack                   reference stack
     * @return array of Object
     */
    private static Object[] getExample(
            Property property,
            Map<String, Model> definitions,
            DocumentResolver definitionDocumentResolver,
            MarkupDocBuilder markupDocBuilder,
            Map<String, Integer> refStack) {
        if (property.getExample() != null) {
            return new Object[]{property.getExample()};
        } else if (property instanceof ArrayProperty) {
            return new Object[]{generateExampleForArrayProperty((ArrayProperty) property, definitions, definitionDocumentResolver, markupDocBuilder, refStack)};
        } else if (property instanceof RefProperty) {
            return new Object[]{generateExampleForRefModel(true, ((RefProperty) property).getSimpleRef(), definitions, definitionDocumentResolver, markupDocBuilder, refStack)};
        } else {
            return new Object[]{PropertyAdapter.generateExample(property, markupDocBuilder)};
        }
    }

    /**
     * Generates examples for string properties or parameters with given format
     *
     * @param format     the format of the string property
     * @param enumValues the enum values
     * @return example
     */
    public static String generateStringExample(String format, List<String> enumValues) {
        if (enumValues == null || enumValues.isEmpty()) {
            if (format == null) {
                return "string";
            } else {
                switch (format) {
                    case "byte":
                        return "Ynl0ZQ==";
                    case "date":
                        return "1970-01-01";
                    case "date-time":
                        return "1970-01-01T00:00:00Z";
                    case "email":
                        return "email@example.com";
                    case "password":
                        return "secret";
                    case "uuid":
                        return "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
                    default:
                        return "string";
                }
            }
        } else {
            return enumValues.get(0);
        }
    }

    /**
     * Generates examples for integer properties - if there are enums, it uses first enum value, returns 0 otherwise.
     *
     * @param enumValues the enum values
     * @return example
     */
    public static Integer generateIntegerExample(List<Integer> enumValues) {
        if (enumValues == null || enumValues.isEmpty()) {
            return 0;
        } else {
            return enumValues.get(0);
        }
    }

    //TODO: Unused method, make sure this is never used and then remove it.
    //FIXME: getProperties() may throw NullPointerException

}
