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
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.lang3.Validate;

/**
 * A SecurityContentExtension can be used to extend the security document.
 */
public abstract class SecurityDocumentExtension extends AbstractExtension {

    public enum Position {
        DOCUMENT_BEFORE,
        DOCUMENT_BEGIN,
        DOCUMENT_END,
        DEFINITION_BEGIN,
        DEFINITION_END

    }

    public static class Context extends ContentContext {
        private Position position;
        /**
         * null if position == DOC_*
         */
        private String definitionName;
        /**
         * null if position == DOC_*
         */
        private SecuritySchemeDefinition definition;

        /**
         * @param position the current position
         * @param docBuilder the MarkupDocBuilder
         */
        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            this.position = position;
        }

        /**
         * @param position the current position
         * @param docBuilder the MarkupDocBuilder
         * @param definitionName the name of the current definition
         * @param definition the current security scheme definition
         */
        public Context(Position position, MarkupDocBuilder docBuilder, String definitionName, SecuritySchemeDefinition definition) {
            super(docBuilder);
            Validate.notNull(definitionName);
            Validate.notNull(definition);
            this.position = position;
            this.definitionName = definitionName;
            this.definition = definition;
        }

        public Position getPosition() {
            return position;
        }

        public Optional<String> getDefinitionName() {
            return Optional.fromNullable(definitionName);
        }

        public Optional<SecuritySchemeDefinition> getDefinition() {
            return Optional.fromNullable(definition);
        }
    }

    public SecurityDocumentExtension() {
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
            case DOCUMENT_BEFORE:
            case DOCUMENT_BEGIN:
            case DOCUMENT_END:
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

}
