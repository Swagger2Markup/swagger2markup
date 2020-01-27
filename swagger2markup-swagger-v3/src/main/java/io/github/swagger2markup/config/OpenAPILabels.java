package io.github.swagger2markup.config;

import io.github.swagger2markup.config.builder.OpenAPI2MarkupConfigBuilder;

import java.util.ResourceBundle;

public class OpenAPILabels extends Labels {
    public OpenAPILabels(OpenAPI2MarkupConfigBuilder.OpenSchema2MarkupConfig config) {
        super(ResourceBundle.getBundle("io/github/swagger2markup/lang/labels", config.getLanguage().toLocale()));
    }
}
