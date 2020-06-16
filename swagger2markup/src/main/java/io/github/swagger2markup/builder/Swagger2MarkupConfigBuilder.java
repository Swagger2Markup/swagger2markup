package io.github.swagger2markup.builder;

import io.github.swagger2markup.Schema2MarkupProperties;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.config.builder.Schema2MarkupConfigBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;

import java.util.Map;
import java.util.Properties;

public class Swagger2MarkupConfigBuilder extends Schema2MarkupConfigBuilder<Swagger2MarkupConfigBuilder, Swagger2MarkupConfig> {

    public Swagger2MarkupConfigBuilder() {
        this(new PropertiesConfiguration());
    }

    public Swagger2MarkupConfigBuilder(Properties properties) {
        this(ConfigurationConverter.getConfiguration(properties));
    }

    public Swagger2MarkupConfigBuilder(Map<String, String> map) {
        this(new MapConfiguration(map));
    }

    public Swagger2MarkupConfigBuilder(Configuration configuration) {
        super(Swagger2MarkupConfigBuilder.class,
                new Swagger2MarkupConfig(),
                new Schema2MarkupProperties(getCompositeConfiguration(configuration)), configuration);
    }

    @Override
    public Swagger2MarkupConfig build() {
        buildNaturalOrdering();
        return config;
    }
}
