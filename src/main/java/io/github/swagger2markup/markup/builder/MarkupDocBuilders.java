/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.swagger2markup.markup.builder;

import io.github.swagger2markup.markup.builder.internal.asciidoc.AsciiDocBuilder;
import io.github.swagger2markup.markup.builder.internal.confluenceMarkup.ConfluenceMarkupBuilder;
import io.github.swagger2markup.markup.builder.internal.markdown.MarkdownBuilder;

/**
 * @author Robert Winkler
 */
public final class MarkupDocBuilders {

    private MarkupDocBuilders() {
    }

    /**
     * Creates a MarkupDocBuilder which uses the system line separator.
     *
     * @param markupLanguage the markup language which is used to generate the files
     * @return a MarkupDocBuilder
     */
    public static MarkupDocBuilder documentBuilder(MarkupLanguage markupLanguage) {
        switch (markupLanguage) {
            case MARKDOWN:
                return new MarkdownBuilder();
            case ASCIIDOC:
                return new AsciiDocBuilder();
            case CONFLUENCE_MARKUP:
                return new ConfluenceMarkupBuilder();
            default:
                throw new IllegalArgumentException(String.format("Unsupported markup language %s", markupLanguage));
        }
    }

    /**
     * Creates a MarkupDocBuilder which uses a custom line separator.
     * If the custom line separator is null, it uses the system line separator.
     *
     * @param markupLanguage the markup language which is used to generate the files
     * @param lineSeparator  the line separator which should be used
     * @return a MarkupDocBuilder
     */
    public static MarkupDocBuilder documentBuilder(MarkupLanguage markupLanguage, LineSeparator lineSeparator) {
        switch (markupLanguage) {
            case MARKDOWN:
                if (lineSeparator == null)
                    return new MarkdownBuilder();
                else
                    return new MarkdownBuilder(lineSeparator.toString());
            case ASCIIDOC:
                if (lineSeparator == null)
                    return new AsciiDocBuilder();
                else
                    return new AsciiDocBuilder(lineSeparator.toString());
            case CONFLUENCE_MARKUP:
                if (lineSeparator == null)
                    return new ConfluenceMarkupBuilder();
                else
                    return new ConfluenceMarkupBuilder(lineSeparator.toString());
            default:
                throw new IllegalArgumentException(String.format("Unsupported markup language %s", markupLanguage));
        }
    }
}
