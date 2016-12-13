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

/**
 * OverviewDocumentExtension extension point can be used to extend the overview document content.
 */
public abstract class OverviewDocumentExtension extends AbstractExtension {


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
                levelOffset = 1;
                break;
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }

        return levelOffset;
    }

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
         * @param docBuilder the MarkupDocBuilder
         */
        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            this.position = position;
        }

        public Position getPosition() {
            return position;
        }
    }

}
