package io.github.swagger2markup.config.builder;

import io.github.swagger2markup.OpenAPI2MarkupProperties;
import io.github.swagger2markup.OpenSchema2MarkupConfig;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;

import java.util.Map;
import java.util.Properties;

public class OpenAPI2MarkupConfigBuilder extends Schema2MarkupConfigBuilder<OpenAPI2MarkupConfigBuilder, OpenSchema2MarkupConfig> {

    public OpenAPI2MarkupConfigBuilder() {
        this(new PropertiesConfiguration());
    }

    public OpenAPI2MarkupConfigBuilder(Properties properties) {
        this(ConfigurationConverter.getConfiguration(properties));
    }

    public OpenAPI2MarkupConfigBuilder(Map<String, String> map) {
        this(new MapConfiguration(map));
    }

    private OpenAPI2MarkupConfigBuilder(Configuration configuration) {
        super(OpenAPI2MarkupConfigBuilder.class,
                new OpenSchema2MarkupConfig(),
                new OpenAPI2MarkupProperties(getCompositeConfiguration(configuration)), configuration);
    }

    @Override
    public OpenSchema2MarkupConfig build() {
        buildNaturalOrdering();
        return config;
    }
}
