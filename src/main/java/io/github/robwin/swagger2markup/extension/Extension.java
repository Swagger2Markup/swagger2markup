package io.github.robwin.swagger2markup.extension;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

public interface Extension {

    /**
     * Global context lazy initialization
     *
     * @param globalContext Global context
     */
    void setGlobalContext(Swagger2MarkupConverter.Context globalContext);

}
