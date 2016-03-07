package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public class ContentContext {
    public MarkupDocBuilder docBuilder;

    public ContentContext(MarkupDocBuilder docBuilder) {
        this.docBuilder = docBuilder;
    }
}
