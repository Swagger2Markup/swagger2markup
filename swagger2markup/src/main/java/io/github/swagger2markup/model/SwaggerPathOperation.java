package io.github.swagger2markup.model;

import io.swagger.models.Operation;

public class SwaggerPathOperation extends PathOperation {
    private Operation operation;

    public SwaggerPathOperation(String httpMethod, String path, String id, String title, Operation operation) {
        super(httpMethod, path, id, title);
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return getId();
    }
}
