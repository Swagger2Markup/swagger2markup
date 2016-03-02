package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.PathOperation;
import org.apache.commons.lang3.Validate;

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

        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            Validate.isTrue(position != Position.OP_BEGIN && position != Position.OP_END, "You must provide an operation for this position");
            this.position = position;
        }

        public Context(Position position, MarkupDocBuilder docBuilder, PathOperation operation) {
            super(docBuilder);
            Validate.notNull(operation);
            this.position = position;
            this.operation = operation;
        }
    }

    public OperationsContentExtension() {
    }

    public abstract void apply(Context context);

    /**
     * Returns title level offset from 1 to apply to content
     *
     * @param context context
     * @return title level offset
     */
    protected int levelOffset(Context context) {
        int levelOffset;
        switch (context.position) {
            case DOC_BEFORE:
            case DOC_AFTER:
                levelOffset = 0;
                break;
            case DOC_BEGIN:
            case DOC_END:
                levelOffset = 1;
                break;
            case OP_BEGIN:
            case OP_END:
                levelOffset = 2;
                break;
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }
        if (globalContext.config.getOperationsGroupedBy() == GroupBy.TAGS) {
            levelOffset++;
        }
        return levelOffset;
    }

}
