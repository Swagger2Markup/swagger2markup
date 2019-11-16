package io.github.swagger2markup;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Test;

import static org.junit.Assert.*;

public class OpenApiV3ConverterTest {

    @Test
    public void name() {
        OpenAPI openAPI = new OpenAPIV3Parser().read("/Users/austek/Workspace/swagger2markup/swagger2markup-swagger-v2/src/test/resources/json/swagger_polymorphism.json");
        openAPI.getInfo();
    }
}
