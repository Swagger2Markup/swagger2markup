package io.github.robwin.swagger2markup;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PathOperation {

    protected HttpMethod method;
    protected String path;
    protected Operation operation;

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
     * @return the operation title
     */
    public String getTitle() {
        String operationName = operation.getSummary();
        if (isBlank(operationName)) {
            operationName = getMethod() + " " + getPath();
        }
        return operationName;
    }

    public String getId() {
        String id = operation.getOperationId();

        if (id == null)
            id = getPath() + " " + getMethod();

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
