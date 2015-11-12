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

import io.github.robwin.markup.builder.MarkupLanguage;
import io.swagger.models.Model;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;


public final class ParameterUtils {

    /**
     * Retrieves the type of a parameter, or otherwise an empty String
     *
     * @param parameter the parameter
     * @param markupLanguage the markup language which is used to generate the files
     * @return the type of the parameter, or otherwise an empty String
     */
    public static String getType(Parameter parameter, MarkupLanguage markupLanguage){
        Validate.notNull(parameter, "property must not be null!");
        String type = "NOT FOUND";
        if(parameter instanceof BodyParameter){
            BodyParameter bodyParameter = (BodyParameter)parameter;
            Model model = bodyParameter.getSchema();
            if(model != null){
                type = ModelUtils.getType(model, markupLanguage);
            }else{
                type = "string";
            }

        }
        else if(parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter)parameter;
            List enums = serializableParameter.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = "enum" + " (" + join(enums, ", ") + ")";
            }else{
                type = getTypeWithFormat(serializableParameter.getType(), serializableParameter.getFormat());
            }
            if(type.equals("array")){
                String collectionFormat = serializableParameter.getCollectionFormat();
                type = collectionFormat + " " + PropertyUtils.getType(serializableParameter.getItems(), markupLanguage) + " " + type;
            }
        }
        else if(parameter instanceof RefParameter){
            RefParameter refParameter = (RefParameter)parameter;
            switch (markupLanguage){
                case ASCIIDOC: return "<<" + refParameter.getSimpleRef() + ">>";
                default: return refParameter.getSimpleRef();
            }
        }
        return defaultString(type);
    }

    /**
     * Adds the format to the type, if a format is available
     *
     * @param typeWithoutFormat the type
     * @param format the format
     * @return returns the type and format, if a format is available
     */
    private static String getTypeWithFormat(String typeWithoutFormat, String format) {
        String type;
        if(isNotBlank(format)){
            type = defaultString(typeWithoutFormat) + " (" + format + ")";
        }else{
            type = defaultString(typeWithoutFormat);
        }
        return type;
    }

    /**
     * Retrieves the default value of a parameter, or otherwise an empty String
     *
     * @param parameter the parameter
     * @return the default value of the parameter, or otherwise an empty String
     */
    public static String getDefaultValue(Parameter parameter){
        Validate.notNull(parameter, "property must not be null!");
        String defaultValue = "";
        if(parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter serializableParameter = (AbstractSerializableParameter)parameter;
            defaultValue = serializableParameter.getDefaultValue();
        }
        return defaultString(defaultValue);
    }

}
