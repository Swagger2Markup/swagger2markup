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
package io.github.swagger2markup;

public enum SwaggerPageBreakLocations implements io.github.swagger2markup.config.PageBreakLocations {
    BEFORE_OPERATION,
    BEFORE_OPERATION_DESCRIPTION,
    BEFORE_OPERATION_PARAMETERS,
    BEFORE_OPERATION_RESPONSES,
    BEFORE_OPERATION_CONSUMES,
    BEFORE_OPERATION_PRODUCES,
    BEFORE_OPERATION_EXAMPLE_REQUEST,
    BEFORE_OPERATION_EXAMPLE_RESPONSE,
    BEFORE_DEFINITION,
    AFTER_OPERATION,
    AFTER_OPERATION_DESCRIPTION,
    AFTER_OPERATION_PARAMETERS,
    AFTER_OPERATION_RESPONSES,
    AFTER_OPERATION_CONSUMES,
    AFTER_OPERATION_PRODUCES,
    AFTER_OPERATION_EXAMPLE_REQUEST,
    AFTER_OPERATION_EXAMPLE_RESPONSE,
    AFTER_DEFINITION
}