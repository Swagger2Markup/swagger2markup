package io.swagger2asciidoc;

import org.junit.Test;

import java.io.File;

/**
 * @author Robert Winkler
 */
public class Swagger2AsciiDocConverterTest {

    @Test
    public void testSwagger2AsciiDocConverter(){
        File file = new File(Swagger2AsciiDocConverterTest.class.getResource("/json/swagger.json").getFile());
        Swagger2AsciiDocConverter.from(file.getAbsolutePath()).convertTo("src/docs/asciidoc/swagger.adoc");
    }
}
