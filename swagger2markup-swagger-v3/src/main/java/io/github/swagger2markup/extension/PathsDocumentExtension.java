/*
 * Copyright 2017 Robert Winkler
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

package io.github.swagger2markup.extension;

import io.github.swagger2markup.model.PathOperation;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;

import java.util.Optional;

/**
 * PathsDocumentExtension extension point can be used to extend the paths document content.
 */
public abstract class PathsDocumentExtension extends AbstractExtension {

    public abstract void apply(Context context);

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
         * @param document   document object
         */
        public Context(Position position, Document document) {
            super(document);
            Validate.inclusiveBetween(Position.DOCUMENT_BEFORE, Position.DOCUMENT_AFTER, position);
            this.position = position;
        }

        /**
         * Context for all other positions
         *
         * @param position   the current position
         * @param document   document object
         * @param operation  the current path operation
         */
        public Context(Position position, Document document, PathOperation operation) {
            super(document);
            Validate.inclusiveBetween(Position.OPERATION_BEFORE, Position.OPERATION_SECURITY_AFTER, position);
            Validate.notNull(operation);
            this.position = position;
            this.operation = operation;
        }

        public Position getPosition() {
            return position;
        }

        public Optional<PathOperation> getOperation() {
            return Optional.ofNullable(operation);
        }
    }
}
