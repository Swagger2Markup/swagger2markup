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

package io.github.swagger2markup.model;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PathOperation {

    private HttpMethod method;
    private String path;
    private Operation operation;

    public PathOperation(HttpMethod method, String path, Operation operation) {
        this.method = method;
        this.path = path;
        this.operation = operation;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    /**
     * Returns the display title for an operation
     *
     * @return the operation title
     */
    public String getTitle() {
        String operationName = operation.getSummary();
        if (isBlank(operationName)) {
            operationName = getMethod().toString() + " " + getPath();
        }
        return operationName;
    }

    /**
     * Returns an unique id for the operation.<br>
     * Use {@code <operation id>}, then {@code <operation path> lowercase(<operation method>)} if operation id is not set.
     *
     * @return operation unique id
     */
    public String getId() {
        String id = operation.getOperationId();

        if (id == null)
            id = getPath() + " " + getMethod().toString().toLowerCase();

        return id;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return getId();
    }
}
