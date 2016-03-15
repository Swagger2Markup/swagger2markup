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

import org.apache.commons.lang3.Validate;

import com.google.common.base.Optional;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.model.PathOperation;

/**
 * A SecurityContentExtension can be used to extend the paths document.
 */
public abstract class PathsDocumentExtension extends AbstractExtension {

    public enum Position {
        DOCUMENT_BEFORE,
        DOCUMENT_BEGIN,
        DOCUMENT_END,
        OPERATION_BEGIN,
        OPERATION_END
    }

    public static class Context extends ContentContext {
        private Position position;
        /**
         * null if position == DOC_*
         */
        private PathOperation operation;

        /**
         * @param position the current position
         * @param docBuilder the MarkupDocBuilder
         */
        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            Validate.isTrue(position != Position.OPERATION_BEGIN && position != Position.OPERATION_END, "You must provide an operation for this position");
            this.position = position;
        }

        /**
         * @param position the current position
         * @param docBuilder the MarkupDocBuilder
         * @param operation the current path operation
         */
        public Context(Position position, MarkupDocBuilder docBuilder, PathOperation operation) {
            super(docBuilder);
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

    public PathsDocumentExtension() {
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
            case DOCUMENT_BEFORE:
            case DOCUMENT_BEGIN:
            case DOCUMENT_END:
                levelOffset = 1;
                break;
            case OPERATION_BEGIN:
            case OPERATION_END:
                levelOffset = 2;
                break;
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }
        if (globalContext.getConfig().getOperationsGroupedBy() == GroupBy.TAGS) {
            levelOffset++;
        }
        return levelOffset;
    }

}
