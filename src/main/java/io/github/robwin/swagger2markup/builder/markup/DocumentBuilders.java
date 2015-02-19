package io.github.robwin.swagger2markup.builder.markup;

import io.github.robwin.swagger2markup.builder.markup.asciidoc.AsciiDocBuilder;
import io.github.robwin.swagger2markup.builder.markup.markdown.MarkdownBuilder;

/**
 * Project:   swagger2markup
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
public final class DocumentBuilders {

    private DocumentBuilders(){};

    public static DocumentBuilder documentBuilder(MarkupLanguage markupLanguage){
        switch(markupLanguage){
            case MARKDOWN: return new MarkdownBuilder();
            case ASCIIDOC: return new AsciiDocBuilder();
            default: return new AsciiDocBuilder();
        }
    }

}
