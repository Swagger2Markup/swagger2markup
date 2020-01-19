package io.github.swagger2markup.config.builder;

import io.github.swagger2markup.OpenAPI2MarkupProperties;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;

import java.util.Map;
import java.util.Properties;

public class OpenAPI2MarkupConfigBuilder extends Schema2MarkupConfigBuilder {
    private OpenSchema2MarkupConfig openApi2MarkupConfig;

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
        super(new OpenAPI2MarkupProperties(getCompositeConfiguration(configuration)), configuration);
    }

    @Override
    public DefaultSchema2MarkupConfig createConfigInstance() {
        if(openApi2MarkupConfig == null) {
            openApi2MarkupConfig = new OpenSchema2MarkupConfig();
        }
        return openApi2MarkupConfig;
    }

    @Override
    public OpenSchema2MarkupConfig build() {
        buildNaturalOrdering();
        return openApi2MarkupConfig;
    }

    public static class OpenSchema2MarkupConfig extends DefaultSchema2MarkupConfig {

    }
}
