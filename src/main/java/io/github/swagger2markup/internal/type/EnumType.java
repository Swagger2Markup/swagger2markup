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

package io.github.swagger2markup.internal.type;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * Enum type abstraction
 */
public class EnumType extends Type {

    protected List<String> values;

    public EnumType(String name, List<String> values) {
        super(name);
        this.values = values;
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        return String.format("enum (%s)", join(values, ", "));
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
