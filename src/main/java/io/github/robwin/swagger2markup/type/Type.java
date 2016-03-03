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

package io.github.robwin.swagger2markup.type;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import org.apache.commons.lang3.Validate;

/**
 * Type abstraction for display purpose
 */
public abstract class Type {

    protected String name;
    protected String uniqueName;

    public Type(String name, String uniqueName) {
        Validate.notBlank(name);

        this.name = name;
        this.uniqueName = uniqueName;
    }

    public Type(String name) {
        this(name, name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public abstract String displaySchema(MarkupDocBuilder docBuilder);
}
