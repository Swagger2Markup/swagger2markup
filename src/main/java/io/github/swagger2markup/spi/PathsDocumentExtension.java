/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.swagger2markup.spi;

import com.google.common.base.Optional;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.model.PathOperation;
import org.apache.commons.lang3.Validate;

/**
 * PathsDocumentExtension extension point can be used to extend the paths document content.
 */
public abstract class PathsDocumentExtension extends AbstractExtension {


    public abstract void apply(Context context);

    /**
     * Returns title level offset from 1 to apply to content
     *
     * @param context context
     * @return title level offset
     */
    protected int levelOffset(Context context) {
        //TODO: Unused method, make sure this is never used and then remove it.
        int levelOffset;
        switch (context.position) {
            case DOCUMENT_BEFORE:
            case DOCUMENT_AFTER:
                levelOffset = 0;
                break;
            case DOCUMENT_BEGIN:
            case DOCUMENT_END:
            case OPERATION_BEFORE:
            case OPERATION_AFTER:
                levelOffset = 1;
                break;
            case OPERATION_BEGIN:
            case OPERATION_END:
                levelOffset = increaseLevelOffset(2);
                break;
            case OPERATION_DESCRIPTION_BEFORE:
            case OPERATION_DESCRIPTION_AFTER:
            case OPERATION_PARAMETERS_BEFORE:
            case OPERATION_PARAMETERS_AFTER:
            case OPERATION_RESPONSES_BEFORE:
            case OPERATION_RESPONSES_AFTER:
            case OPERATION_SECURITY_BEFORE:
            case OPERATION_SECURITY_AFTER:
                levelOffset = increaseLevelOffset(2);
                break;
            case OPERATION_DESCRIPTION_BEGIN:
            case OPERATION_DESCRIPTION_END:
            case OPERATION_PARAMETERS_BEGIN:
            case OPERATION_PARAMETERS_END:
            case OPERATION_RESPONSES_BEGIN:
            case OPERATION_RESPONSES_END:
            case OPERATION_SECURITY_BEGIN:
            case OPERATION_SECURITY_END:
                levelOffset = 3;
                break;
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }
        return levelOffset;
    }

    private int increaseLevelOffset(int levelOffset) {
        //TODO: This method always receives levelOffset=2. Perhaps the parameter could be removed
        if (globalContext.getConfig().getPathsGroupedBy() == GroupBy.TAGS) {
            return ++levelOffset;
        } else {
            return levelOffset;
        }
    }

    public enum Position {
        DOCUMENT_BEFORE,
        DOCUMENT_BEGIN,
        DOCUMENT_END,
        DOCUMENT_AFTER,
        OPERATION_BEFORE,
        OPERATION_BEGIN,
        OPERATION_END,
        OPERATION_AFTER,
        OPERATION_DESCRIPTION_BEFORE,
        OPERATION_DESCRIPTION_BEGIN,
        OPERATION_DESCRIPTION_END,
        OPERATION_DESCRIPTION_AFTER,
        OPERATION_PARAMETERS_BEFORE,
        OPERATION_PARAMETERS_BEGIN,
        OPERATION_PARAMETERS_END,
        OPERATION_PARAMETERS_AFTER,
        OPERATION_RESPONSES_BEFORE,
        OPERATION_RESPONSES_BEGIN,
        OPERATION_RESPONSES_END,
        OPERATION_RESPONSES_AFTER,
        OPERATION_SECURITY_BEFORE,
        OPERATION_SECURITY_BEGIN,
        OPERATION_SECURITY_END,
        OPERATION_SECURITY_AFTER
    }

    public static class Context extends ContentContext {
        private Position position;
        /**
         * null if position == DOCUMENT_*
         */
        private PathOperation operation;

        /**
         * Context for positions DOCUMENT_*
         *
         * @param position   the current position
         * @param docBuilder the MarkupDocBuilder
         */
        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            Validate.inclusiveBetween(Position.DOCUMENT_BEFORE, Position.DOCUMENT_AFTER, position);
            this.position = position;
        }

        /**
         * Context for all other positions
         *
         * @param position   the current position
         * @param docBuilder the MarkupDocBuilder
         * @param operation  the current path operation
         */
        public Context(Position position, MarkupDocBuilder docBuilder, PathOperation operation) {
            super(docBuilder);
            Validate.inclusiveBetween(Position.OPERATION_BEFORE, Position.OPERATION_SECURITY_AFTER, position);
            Validate.notNull(operation);
            this.position = position;
            this.operation = operation;
        }

        public Position getPosition() {
            return position;
        }

        public Optional<PathOperation> getOperation() {
            return Optional.fromNullable(operation);
        }
    }
}
