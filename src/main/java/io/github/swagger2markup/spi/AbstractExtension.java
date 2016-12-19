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

package io.github.swagger2markup.spi;

import io.github.swagger2markup.Swagger2MarkupConverter;

/**
 * An abstract extension which must be extended by an extension
 */
abstract class AbstractExtension implements Extension {

    protected Swagger2MarkupConverter.Context globalContext;

    /**
     * Global context lazy initialization
     *
     * @param globalContext Global context
     */
    public void setGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        this.globalContext = globalContext;
        init(globalContext);
    }

    /**
     * Overridable init event listener.
     *
     * @param globalContext Global context
     */
    public void init(Swagger2MarkupConverter.Context globalContext) {
        /* must be left empty */
    }

}
