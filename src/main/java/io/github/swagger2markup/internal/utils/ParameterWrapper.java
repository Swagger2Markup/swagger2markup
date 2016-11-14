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

import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;

public class ParameterWrapper {

    private final Parameter parameter;

    public ParameterWrapper(Parameter parameter){
        Validate.notNull(parameter, "parameter must not be null");
        this.parameter = parameter;
    }

    public String getType(){
        return WordUtils.capitalize(parameter.getIn());
    }


}
