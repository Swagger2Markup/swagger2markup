package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

public abstract class DefinitionsContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END,
        DEF_BEGIN,
        DEF_END
    }

    public static class Context {
        public Position position;
        public MarkupDocBuilder docBuilder;
        /**
         * null if position == DOC_*
         */
        public String definitionName;

        public Context(Position position, String definitionName, MarkupDocBuilder docBuilder) {
            this.position = position;
            this.definitionName = definitionName;
            this.docBuilder = docBuilder;
        }
    }

    public DefinitionsContentExtension() {
    }

    public abstract void apply(Swagger2MarkupConverter.Context globalContext, Context context);
}
