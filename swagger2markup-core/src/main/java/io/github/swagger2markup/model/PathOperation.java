package io.github.swagger2markup.model;

/**
 * PathOperation wrapper for Schema operation model
 */
public class PathOperation {
    private String httpMethod;
    private String path;
    private String id;
    private String title;

    public PathOperation(String httpMethod, String path, String id, String title) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.id = id;
        this.title = title;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
