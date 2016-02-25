package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.PathOperation;

public abstract class OperationsContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END,
        OP_BEGIN,
        OP_END
    }

    public static class Context extends ContentContext {
        public Position position;
        /**
         * null if position == DOC_*
         */
        public PathOperation operation;

        public Context(Position position, MarkupDocBuilder docBuilder, PathOperation operation) {
            super(docBuilder);
            this.position = position;
            this.operation = operation;
        }
    }

    public OperationsContentExtension() {
    }

    public abstract void apply(Context context);
}
