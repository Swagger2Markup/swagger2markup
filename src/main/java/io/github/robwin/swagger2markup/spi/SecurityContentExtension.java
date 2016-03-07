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

package io.github.robwin.swagger2markup.spi;

import io.github.robwin.markup.builder.MarkupDocBuilder;

public abstract class SecurityContentExtension extends AbstractExtension {

    public enum Position {
        DOC_BEFORE,
        DOC_AFTER,
        DOC_BEGIN,
        DOC_END
    }

    public static class Context extends ContentContext {
        public Position position;

        public Context(Position position, MarkupDocBuilder docBuilder) {
            super(docBuilder);
            this.position = position;
        }
    }

    public SecurityContentExtension() {
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
            default:
                throw new RuntimeException(String.format("Unknown position '%s'", context.position));
        }

        return levelOffset;
    }

}
