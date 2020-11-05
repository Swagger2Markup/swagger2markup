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

import org.asciidoctor.ast.Document;

/**
 * OverviewDocumentExtension extension point can be used to extend the overview document content.
 */
public abstract class OverviewDocumentExtension extends AbstractExtension {

    public abstract void apply(Context context);

    public enum Position {
        DOCUMENT_BEFORE,
        DOCUMENT_AFTER,
        DOCUMENT_BEGIN,
        DOCUMENT_END
    }

    public static class Context extends ContentContext {
        private Position position;

        /**
         * @param position   the current position
         * @param document   document object
         */
        public Context(Position position, Document document) {
            super(document);
            this.position = position;
        }

        public Position getPosition() {
            return position;
        }
    }

}
