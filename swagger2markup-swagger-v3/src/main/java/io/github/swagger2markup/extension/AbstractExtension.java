/*
 * Copyright 2017 Robert Winkler
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

package io.github.swagger2markup.extension;

import io.github.swagger2markup.OpenAPI2MarkupConverter.OpenAPIContext;

/**
 * An abstract OpenAPI extension which must be extended by an other OpenAPI extensions
 */
abstract class AbstractExtension implements Extension {

    protected OpenAPIContext globalContext;

    /**
     * Global context lazy initialization
     *
     * @param globalContext Global context
     */
    public void setGlobalContext(OpenAPIContext globalContext) {
        this.globalContext = globalContext;
    }
}
