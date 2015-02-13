package io.swagger2markup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

/**
 * Project:   spring-swagger2asciidoc
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringBootSwaggerConfig.class)
@IntegrationTest
@WebAppConfiguration
public class Swagger2MarkupTest {

    @Test
    public void convertSwaggerToAsciiDoc() throws IOException {
        Swagger2MarkupConverter.from("http://localhost:8080/api-docs").toAsciiDoc("src/docs/asciidoc/example.adoc");
        Swagger2MarkupConverter.from("http://localhost:8080/api-docs").toMarkdown("src/docs/markdown/example.md");
    }

}
