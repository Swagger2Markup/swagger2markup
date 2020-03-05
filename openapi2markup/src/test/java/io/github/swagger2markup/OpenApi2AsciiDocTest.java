package io.github.swagger2markup;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class OpenApi2AsciiDocTest {

    final private String openApiFile;
    final private String expectedAsciiDoc;

    public OpenApi2AsciiDocTest(String openApiFile, String expectedAsciiDoc) throws IOException {
        this.openApiFile = "./src/test/resources/open_api/" + openApiFile;
        this.expectedAsciiDoc = IOUtils.toString(getClass().getResourceAsStream("/asciidoc/" + expectedAsciiDoc), StandardCharsets.UTF_8);
    }

    @Parameterized.Parameters(name = "Run {index}: open api={0}, asciidoc={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"simple.yaml", "simple.adoc"},
                {"petstore.yaml", "petstore.adoc"}
        });
    }

    @Test
    public void converts_open_api_v3_to_asciidoc() {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(openApiFile, null, options);
        OpenAPI swagger = result.getOpenAPI();
        assertNotNull(swagger);

        OpenAPI2MarkupConverter converter = OpenAPI2MarkupConverter.from(swagger).build();
        converter.toFolder(Paths.get("build/test/asciidoc"));
    }
}
