package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.PathOperation;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

public abstract class OperationsContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END,
        OP_BEGIN,
        OP_END
    }

    public static class Context {
        public Position position;
        public MarkupDocBuilder docBuilder;
        /**
         * null if position == DOC_*
         */
        public PathOperation operation;

        public Context(Position position, PathOperation operation, MarkupDocBuilder docBuilder) {
            this.position = position;
            this.operation = operation;
            this.docBuilder = docBuilder;
        }
    }

    public OperationsContentExtension() {
    }

    public abstract void apply(Swagger2MarkupConverter.Context globalContext, Context context);
}
