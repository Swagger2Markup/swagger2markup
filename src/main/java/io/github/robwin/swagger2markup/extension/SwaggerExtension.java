package io.github.robwin.swagger2markup.extension;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

public abstract class SwaggerExtension extends AbstractExtension {

    public SwaggerExtension() {
    }

    public abstract void apply(Swagger2MarkupConverter.Context globalContext);

}
