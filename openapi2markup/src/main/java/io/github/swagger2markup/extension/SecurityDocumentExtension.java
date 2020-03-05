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

import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;

import java.util.Optional;

/**
 * SecurityContentExtension extension point can be used to extend the security document content.
 */
public abstract class SecurityDocumentExtension extends AbstractExtension {


    public abstract void apply(Context context);

    public enum Position {
        DOCUMENT_BEFORE,
        DOCUMENT_BEGIN,
        DOCUMENT_END,
        DOCUMENT_AFTER,
        SECURITY_SCHEME_BEFORE,
        SECURITY_SCHEME_BEGIN,
        SECURITY_SCHEME_END,
        SECURITY_SCHEME_AFTER
    }

    public static class Context extends ContentContext {
        private Position position;
        /**
         * null if position == DOCUMENT_*
         */
        private String securitySchemeName;
        /**
         * null if position == DOCUMENT_*
         */
        private SecuritySchemeDefinition securityScheme;

        /**
         * @param position   the current position
         * @param document the MarkupDocBuilder
         */
        public Context(Position position, Document document) {
            super(document);
            Validate.inclusiveBetween(Position.DOCUMENT_BEFORE, Position.DOCUMENT_AFTER, position);
            this.position = position;
        }

        /**
         * @param position           the current position
         * @param document         the MarkupDocBuilder
         * @param securitySchemeName the name of the current securityScheme
         * @param securityScheme     the current security scheme securityScheme
         */
        public Context(Position position, Document document, String securitySchemeName, SecuritySchemeDefinition securityScheme) {
            super(document);
            Validate.inclusiveBetween(Position.SECURITY_SCHEME_BEFORE, Position.SECURITY_SCHEME_AFTER, position);
            Validate.notNull(securitySchemeName);
            Validate.notNull(securityScheme);
            this.position = position;
            this.securitySchemeName = securitySchemeName;
            this.securityScheme = securityScheme;
        }

        public Position getPosition() {
            return position;
        }

        public Optional<String> getSecuritySchemeName() {
            return Optional.ofNullable(securitySchemeName);
        }

        public Optional<SecuritySchemeDefinition> getSecurityScheme() {
            return Optional.ofNullable(securityScheme);
        }
    }

}
