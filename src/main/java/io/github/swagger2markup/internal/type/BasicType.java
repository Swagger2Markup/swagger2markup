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
import org.apache.commons.lang3.Validate;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Base type abstraction (string, integer, ...)
 */
public class BasicType extends Type {

    /**
     * Basic type
     */
    protected String type;
    protected String format;

    public BasicType(String type, String name) {
        this(type, name, null);
    }

    public BasicType(String type, String name, String format) {
        super(name);
        Validate.notBlank(type);
        this.type = type;
        this.format = format;
    }

    @Override
    public String displaySchema(MarkupDocBuilder docBuilder) {
        if (isNotBlank(this.format))
            return String.format("%s(%s)", this.type, this.format);
        else
            return this.type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
