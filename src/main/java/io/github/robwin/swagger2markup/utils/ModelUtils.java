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

import com.wordnik.swagger.models.ArrayModel;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.RefModel;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.lang3.Validate;

public final class ModelUtils {

    public static String getType(Model model, MarkupLanguage markupLanguage) {
        Validate.notNull(model, "model must not be null!");
        if (model instanceof ModelImpl) {
            return ((ModelImpl) model).getType();
        } else if (model instanceof RefModel) {
            switch (markupLanguage){
                case ASCIIDOC: return "<<" + ((RefModel) model).getSimpleRef() + ">>";
                default: return ((RefModel) model).getSimpleRef();
            }
        } else if (model instanceof ArrayModel) {
            ArrayModel arrayModel = ((ArrayModel) model);
            return PropertyUtils.getType(arrayModel.getItems(), markupLanguage) + " " + arrayModel.getType();
        }
        return "NOT FOUND";
    }
}
