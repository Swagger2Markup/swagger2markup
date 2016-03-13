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

package io.github.robwin.markup.builder;

import io.github.robwin.markup.builder.asciidoc.AsciiDocBuilder;
import io.github.robwin.markup.builder.markdown.MarkdownBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert Winkler
 */
public final class MarkupLanguage {
    public static final MarkupLanguage ASCIIDOC = new MarkupLanguage(".adoc,.asciidoc", AsciiDocBuilder.class);
    public static final MarkupLanguage MARKDOWN = new MarkupLanguage(".md,.markdown", MarkdownBuilder.class);

    private final String fileNameExtensions;
    private final Class<? extends MarkupDocBuilder> markupDocBuilderClass;

    /**
     * @param fileNameExtensions    file name suffix
     * @param markupDocBuilderClass the builder class
     */
    public MarkupLanguage(final String fileNameExtensions, Class<? extends MarkupDocBuilder> markupDocBuilderClass) {
        this.fileNameExtensions = fileNameExtensions;
        this.markupDocBuilderClass = markupDocBuilderClass;
    }

    public List<String> getFileNameExtensions() {
        return Arrays.asList(fileNameExtensions.split(","));
    }

    public Class<? extends MarkupDocBuilder> getMarkupDocBuilderClass() {
        return markupDocBuilderClass;
    }
}
