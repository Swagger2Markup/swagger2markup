package io.github.swagger2markup.adoc.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.converter.spi.ConverterRegistry;

public class AsciiDocConverterRegistry implements ConverterRegistry {
    @Override
    public void register(Asciidoctor asciidoctor) {
        asciidoctor.javaConverterRegistry().register(AsciidocConverter.class);
    }
}
