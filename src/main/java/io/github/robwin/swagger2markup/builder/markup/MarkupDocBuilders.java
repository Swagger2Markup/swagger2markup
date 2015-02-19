package io.github.robwin.swagger2markup.builder.markup;

import io.github.robwin.swagger2markup.builder.markup.asciidoc.AsciiDocBuilder;
import io.github.robwin.swagger2markup.builder.markup.markdown.MarkdownBuilder;

/**
 * @author Robert Winkler
 */
public final class MarkupDocBuilders {

    private MarkupDocBuilders(){};

    public static MarkupDocBuilder documentBuilder(MarkupLanguage markupLanguage){
        switch(markupLanguage){
            case MARKDOWN: return new MarkdownBuilder();
            case ASCIIDOC: return new AsciiDocBuilder();
            default: return new AsciiDocBuilder();
        }
    }

}
