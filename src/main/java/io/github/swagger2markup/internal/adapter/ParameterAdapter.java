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

import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.internal.resolver.DocumentResolver;
import io.github.swagger2markup.internal.type.*;
import io.github.swagger2markup.internal.utils.InlineSchemaUtils;
import io.github.swagger2markup.internal.utils.ModelUtils;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.Model;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParameterAdapter {

    private final Parameter parameter;
    private Type type;
    private final List<ObjectType> inlineDefinitions = new ArrayList<>();

    public ParameterAdapter(Swagger2MarkupConfig config,
                            PathOperation operation,
                            Parameter parameter,
                            Map<String, Model> definitions,
                            DocumentResolver definitionDocumentResolver){
        Validate.notNull(parameter, "parameter must not be null");
        this.parameter = parameter;
        type = getType(definitions, definitionDocumentResolver);
        if (config.isInlineSchemaEnabled()){
            if(config.isFlatBodyEnabled()) {
                if (!(type instanceof ObjectType)){
                    type = InlineSchemaUtils.createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), inlineDefinitions);
                }
            }else{
                type = InlineSchemaUtils.createInlineType(type, parameter.getName(), operation.getId() + " " + parameter.getName(), inlineDefinitions);
            }
        }
    }

    public String getIn(){
        return WordUtils.capitalize(parameter.getIn());
    }

    public Type getType(){
        return type;
    }

    public List<ObjectType> getInlineDefinitions(){
        return inlineDefinitions;
    }

    /**
     * Retrieves the type of a parameter, or otherwise null
     *
     * @param definitionDocumentResolver the defintion document resolver
     * @return the type of the parameter, or otherwise null
     */
    private Type getType(Map<String, Model> definitions, DocumentResolver definitionDocumentResolver){
        Validate.notNull(parameter, "parameter must not be null!");
        Type type = null;

        if(parameter instanceof BodyParameter){
            BodyParameter bodyParameter = (BodyParameter)parameter;
            Model model = bodyParameter.getSchema();

            if(model != null){
                type = ModelUtils.getType(model, definitions, definitionDocumentResolver);
            }else{
                type = new BasicType("string", bodyParameter.getName());
            }

        }
        else if(parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter)parameter;
            @SuppressWarnings("unchecked")
            List<String> enums = serializableParameter.getEnum();

            if(CollectionUtils.isNotEmpty(enums)){
                type = new EnumType(serializableParameter.getName(), enums);
            }else{
                type = new BasicType(serializableParameter.getType(), serializableParameter.getName(), serializableParameter.getFormat());
            }
            if(serializableParameter.getType().equals("array")){
                String collectionFormat = serializableParameter.getCollectionFormat();

                type = new ArrayType(serializableParameter.getName(), new PropertyAdapter(serializableParameter.getItems()).getType(definitionDocumentResolver), collectionFormat);
            }
        }
        else if(parameter instanceof RefParameter){
            String refName = ((RefParameter)parameter).getSimpleRef();

            type = new RefType(definitionDocumentResolver.apply(refName), new ObjectType(refName, null /* FIXME, not used for now */));
        }
        return type;
    }

    /**
     * Retrieves the default value of a parameter
     *
     * @return the default value of the parameter
     */
    public Optional<String> getDefaultValue(){
        Validate.notNull(parameter, "parameter must not be null!");
        if(parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter)parameter;
            return Optional.ofNullable(serializableParameter.getDefaultValue());
        }
        return Optional.empty();
    }

    /**
     * Generate a default example value for parameter.
     *
     * @param parameter parameter
     * @return a generated example for the parameter
     */
    public static Object generateExample(AbstractSerializableParameter parameter) {
        switch (parameter.getType()) {
            case "integer":
                return 0;
            case "number":
                return 0.0;
            case "boolean":
                return true;
            case "string":
                return "string";
            default:
                return parameter.getType();
        }
    }

}
