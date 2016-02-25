package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public abstract class DefinitionsContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END,
        DEF_BEGIN,
        DEF_END;
    }

    public static class Context extends ContentContext {
        public Position position;
        /**
         * null if position == DOC_*
         */
        public String definitionName;

        public Context(Position position, MarkupDocBuilder docBuilder, String definitionName) {
            super(docBuilder);
            this.position = position;
            this.definitionName = definitionName;
        }
    }

    public DefinitionsContentExtension() {
    }

    public abstract void apply(Context context);
}
