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
import io.github.robwin.swagger2markup.type.*;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import org.apache.commons.lang3.Validate;

public final class ModelUtils {

    /**
     * Retrieves the type of a model, or otherwise null
     *
     * @param model the model
     * @return the type of the model, or otherwise null
     */
    public static Type getType(Model model, Function<String, String> definitionDocumentResolver) {
        Validate.notNull(model, "model must not be null!");
        if (model instanceof ModelImpl) {
            return new ObjectType(null, model.getProperties());
        } else if (model instanceof RefModel) {
            String simpleRef = ((RefModel) model).getSimpleRef();
            return new RefType(definitionDocumentResolver.apply(simpleRef), simpleRef);
        } else if (model instanceof ArrayModel) {
            ArrayModel arrayModel = ((ArrayModel) model);
            return new ArrayType(null, PropertyUtils.getType(arrayModel.getItems(), definitionDocumentResolver));
        }
        return null;
    }
}
