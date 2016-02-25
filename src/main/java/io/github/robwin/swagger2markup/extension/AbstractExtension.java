package io.github.robwin.swagger2markup.extension;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

public abstract class AbstractExtension implements Extension {

    protected Swagger2MarkupConverter.Context globalContext;

    /**
     * Global context lazy initialization
     *
     * @param globalContext Global context
     */
    public void setGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        this.globalContext = globalContext;
        onUpdateGlobalContext(globalContext);
    }

    /**
     * Overridable onUpdateGlobalContext event listener.
     *
     * @param globalContext Global context
     */
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        /* must be left empty */
    }

}
