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

package io.github.robwin.swagger2markup.extension;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import org.apache.commons.lang3.Validate;

public abstract class DefinitionsContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END,
        DEF_BEGIN,
        DEF_END
    }

    public static class Context extends ContentContext {
        public Position position;
        /**
         * null if position == DOC_*
         */
        public String definitionName;

        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            Validate.isTrue(position != Position.DEF_BEGIN && position != Position.DEF_END, "You must provide a definitionName for this position");
            this.position = position;
        }

        public Context(Position position, MarkupDocBuilder docBuilder, String definitionName) {
            super(docBuilder);
            Validate.notNull(definitionName);
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
