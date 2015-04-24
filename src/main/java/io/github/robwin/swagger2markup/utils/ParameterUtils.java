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

import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.parameters.*;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;


public final class ParameterUtils {

    public static String getType(Parameter parameter, MarkupLanguage markupLanguage){
        Validate.notNull(parameter, "property must not be null!");
        String type = "NOT FOUND";
        if(parameter instanceof BodyParameter){
            BodyParameter bodyParameter = (BodyParameter)parameter;
            Model model = bodyParameter.getSchema();
            type = ModelUtils.getType(model, markupLanguage);
        }
        else if(parameter instanceof PathParameter){
            PathParameter pathParameter = (PathParameter)parameter;
            type = getTypeWithFormat(pathParameter.getType(), pathParameter.getFormat());
        }
        else if(parameter instanceof QueryParameter){
            QueryParameter queryParameter = (QueryParameter)parameter;
            List<String> enums = queryParameter.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = "enum" + " (" + StringUtils.join(enums, ", ") + ")";
            }else{
                type = getTypeWithFormat(queryParameter.getType(), queryParameter.getFormat());
            }
            if(type.equals("array")){
                String collectionFormat = queryParameter.getCollectionFormat();
                type = collectionFormat + " " + PropertyUtils.getType(queryParameter.getItems(), markupLanguage) + " " + type;
            }
        }
        else if(parameter instanceof HeaderParameter){
            HeaderParameter headerParameter = (HeaderParameter)parameter;
            List<String> enums = headerParameter.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = "enum" + " (" + StringUtils.join(enums, ", ") + ")";
            }else{
                type = getTypeWithFormat(headerParameter.getType(), headerParameter.getFormat());
            }
            if(type.equals("array")){
                String collectionFormat = headerParameter.getCollectionFormat();
                type = collectionFormat + " " + PropertyUtils.getType(headerParameter.getItems(), markupLanguage) + " " + type;
            }
        }
        else if(parameter instanceof FormParameter){
            FormParameter formParameter = (FormParameter)parameter;
            List<String> enums = formParameter.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = "enum" + " (" + StringUtils.join(enums, ", ") + ")";
            }else{
                type = getTypeWithFormat(formParameter.getType(), formParameter.getFormat());
            }
            if(type.equals("array")){
                String collectionFormat = formParameter.getCollectionFormat();
                type = collectionFormat + " " + PropertyUtils.getType(formParameter.getItems(), markupLanguage) + " " + type;
            }
        }
        else if(parameter instanceof CookieParameter){
            CookieParameter cookieParameter = (CookieParameter)parameter;
            List<String> enums = cookieParameter.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = "enum" + " (" + StringUtils.join(enums, ", ") + ")";
            }else{
                type = getTypeWithFormat(cookieParameter.getType(), cookieParameter.getFormat());
            }
            if(type.equals("array")){
                String collectionFormat = cookieParameter.getCollectionFormat();
                type = collectionFormat + " " + PropertyUtils.getType(cookieParameter.getItems(), markupLanguage) + " " + type;
            }
        }
        else if(parameter instanceof RefParameter){
            RefParameter refParameter = (RefParameter)parameter;
            switch (markupLanguage){
                case ASCIIDOC: return "<<" + refParameter.getSimpleRef() + ">>";
                default: return refParameter.getSimpleRef();
            }
        }
        return type;
    }

    private static String getTypeWithFormat(String typeWithoutFormat, String format) {
        String type;
        if(StringUtils.isNotBlank(format)){
            type = typeWithoutFormat + " (" + format + ")";
        }else{
            type = typeWithoutFormat;
        }
        return type;
    }
}
