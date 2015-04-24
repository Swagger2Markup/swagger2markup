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

import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.Property;
import com.wordnik.swagger.models.properties.RefProperty;
import com.wordnik.swagger.models.properties.StringProperty;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;

public final class PropertyUtils {

    public static String getType(Property property, MarkupLanguage markupLanguage){
        Validate.notNull(property, "property must not be null!");
        String type;
        if(property instanceof RefProperty){
            RefProperty refProperty = (RefProperty)property;
            switch (markupLanguage){
                case ASCIIDOC: return "<<" + refProperty.getSimpleRef() + ">>";
                default: return refProperty.getSimpleRef();
            }
        }else if(property instanceof ArrayProperty){
            ArrayProperty arrayProperty = (ArrayProperty)property;
            Property items = arrayProperty.getItems();
            type = getType(items, markupLanguage) + " " + arrayProperty.getType();
        }else if(property instanceof StringProperty){
            StringProperty stringProperty = (StringProperty)property;
            List<String> enums = stringProperty.getEnum();
            if(CollectionUtils.isNotEmpty(enums)){
                type = "enum" + " (" + StringUtils.join(enums, ", ") + ")";
            }else{
                type = property.getType();
            }
        }
        else{
            if(StringUtils.isNotBlank(property.getFormat())){
                type = property.getType() + " (" + property.getFormat() + ")";
            }else{
                type = property.getType();
            }
        }
        return type;
    }
}
