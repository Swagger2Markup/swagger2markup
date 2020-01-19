package io.github.swagger2markup.config;

import java.util.ResourceBundle;

import static io.github.swagger2markup.config.builder.OpenAPI2MarkupConfigBuilder.OpenSchema2MarkupConfig;

public class OpenAPILabels extends Labels {
    public OpenAPILabels(OpenSchema2MarkupConfig config) {
        super(ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getLanguage().toLocale()));
    }
}
