package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

public abstract class SecurityContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END
    }

    public static class Context {
        public Position position;
        public MarkupDocBuilder docBuilder;

        public Context(Position position, MarkupDocBuilder docBuilder) {
            this.position = position;
            this.docBuilder = docBuilder;
        }
    }

    public SecurityContentExtension() {
    }

    public abstract void apply(Swagger2MarkupConverter.Context globalContext, Context context);
}
