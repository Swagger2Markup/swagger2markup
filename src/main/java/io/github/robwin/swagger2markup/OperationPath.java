package io.github.robwin.swagger2markup;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

public class OperationPath {

    protected HttpMethod method;
    protected String path;
    protected Operation operation;

    public OperationPath(HttpMethod method, String path, Operation operation) {
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

    public String getId() {
        String id = operation.getOperationId();

        if (id == null)
            id = getMethod() + " " + getPath();

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
