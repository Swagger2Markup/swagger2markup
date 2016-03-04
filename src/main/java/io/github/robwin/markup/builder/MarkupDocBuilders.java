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

/**
 * @author Robert Winkler
 */
public final class MarkupDocBuilders {

    public static final String LINE_SEPARATOR_UNIX	= "\n";
    public static final String LINE_SEPARATOR_WINDOWS= "\r\n";

    private MarkupDocBuilders(){}

    /**
     * Creates a MarkupDocBuilder which uses the system line separator.
     *
     * @param markupLanguage the markup language which is used to generate the files
     * @return a MarkupDocBuilder
     */
    public static MarkupDocBuilder documentBuilder(MarkupLanguage markupLanguage){
        switch(markupLanguage){
            case MARKDOWN: return new MarkdownBuilder();
            case ASCIIDOC: return new AsciiDocBuilder();
            default: return new AsciiDocBuilder();
        }
    }

    /**
     * Creates a MarkupDocBuilder which uses a custom line separator.
     *
     * @param markupLanguage the markup language which is used to generate the files
     * @param lineSeparator the line separator which should be used
     * @return a MarkupDocBuilder
     */
    public static MarkupDocBuilder documentBuilder(MarkupLanguage markupLanguage, LineSeparator lineSeparator){
        switch(markupLanguage){
            case MARKDOWN: return new MarkdownBuilder(lineSeparator);
            case ASCIIDOC: return new AsciiDocBuilder(lineSeparator);
            default: return new AsciiDocBuilder(lineSeparator);
        }
    }

    /**
     * Instantiate a new builder from {@code docBuilder} with the same configuration.
     * You can use it to build intermediate MarkupDocBuilder for composition purpose.
     */
    public static MarkupDocBuilder documentBuilder(MarkupDocBuilder docBuilder){
       return docBuilder.copy();
    }



}
