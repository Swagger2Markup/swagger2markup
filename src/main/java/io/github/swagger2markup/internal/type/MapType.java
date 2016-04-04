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

/**
 * Array type abstraction
 */
public class MapType extends Type {

    protected Type keyType = new BasicType("string");
    protected Type valueType;
    
    public MapType(String name, Type valueType) {
        super(name == null ? "map" : name);
        this.valueType = valueType;
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        return String.format("<%s,%s> map", keyType.displaySchema(docBuilder), valueType.displaySchema(docBuilder));
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }

    public void setValueType(Type valueType) {
        this.valueType = valueType;
    }
}
