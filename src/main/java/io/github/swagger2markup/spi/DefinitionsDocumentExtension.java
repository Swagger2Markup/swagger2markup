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

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.Model;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

/**
 * DefinitionsDocumentExtension extension point can be used to extend the definitions document content.
 */
public abstract class DefinitionsDocumentExtension extends AbstractExtension {


    public abstract void apply(Context context);

    /**
     * Returns title level offset from 1 to apply to content
     *
     * @param context context
     * @return title level offset
     */
    protected int levelOffset(Context context) throws RuntimeException {
        //TODO: Unused method, make sure this is never used and then remove it.
        int levelOffset;
        switch (context.position) {
            case DOCUMENT_BEFORE:
            case DOCUMENT_AFTER:
                levelOffset = 0;
                break;
            case DOCUMENT_BEGIN:
            case DOCUMENT_END:
            case DEFINITION_BEFORE:
            case DEFINITION_AFTER:
                levelOffset = 1;
                break;
            case DEFINITION_BEGIN:
            case DEFINITION_END:
                levelOffset = 2;
                break;
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }

        return levelOffset;
    }

    public enum Position {
        DOCUMENT_BEFORE,
        DOCUMENT_BEGIN,
        DOCUMENT_END,
        DOCUMENT_AFTER,
        DEFINITION_BEFORE,
        DEFINITION_BEGIN,
        DEFINITION_END,
        DEFINITION_AFTER
    }

    public static class Context extends ContentContext {
        private Position position;
        /**
         * null if position == DOCUMENT_*
         */
        private String definitionName;

        /**
         * null if position == DOCUMENT_*
         */
        private Model model;

        /**
         * @param position   the current position
         * @param docBuilder the MarkupDocBuilder
         */
        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            Validate.inclusiveBetween(Position.DOCUMENT_BEFORE, Position.DOCUMENT_AFTER, position);
            this.position = position;
        }

        /**
         * @param position       the current position
         * @param docBuilder     the MarkupDocBuilder
         * @param definitionName the name of the current definition
         * @param model          the current Model of the definition
         */
        public Context(Position position, MarkupDocBuilder docBuilder, String definitionName, Model model) {
            super(docBuilder);
            Validate.inclusiveBetween(Position.DEFINITION_BEFORE, Position.DEFINITION_AFTER, position);
            Validate.notNull(definitionName);
            Validate.notNull(model);
            this.position = position;
            this.definitionName = definitionName;
            this.model = model;
        }

        public Position getPosition() {
            return position;
        }

        public Optional<String> getDefinitionName() {
            return Optional.ofNullable(definitionName);
        }

        public Optional<Model> getModel() {
            return Optional.ofNullable(model);
        }
    }
}
