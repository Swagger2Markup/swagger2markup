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

    /**
     * Returns title level offset from 1 to apply to content
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
            case DEF_BEGIN:
            case DEF_END:
                levelOffset = 2;
                break;
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }

        return levelOffset;
    }
}
