package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public abstract class OverviewContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END
    }

    public static class Context extends ContentContext {
        public Position position;

        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            this.position = position;
        }
    }

    public OverviewContentExtension() {
    }

    public abstract void apply(Context context);
}
