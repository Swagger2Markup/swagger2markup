package io.github.swagger2markup;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class OpenApiV3Converter {

    public static void main(String[] args) {
        OpenAPI openAPI = new OpenAPIV3Parser().read("/Users/austek/Workspace/swagger2markup/swagger2markup-swagger-v2/src/test/resources/json/swagger_polymorphism.json");
        openAPI.getInfo();
//new DocumentImpl()
        System.out.println(openAPI.toString());
    }
}