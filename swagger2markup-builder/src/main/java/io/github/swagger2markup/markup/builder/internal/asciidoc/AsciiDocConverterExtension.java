package io.github.swagger2markup.markup.builder.internal.asciidoc;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class AsciiDocConverterExtension {

    private static final Parser parser;
    private static final HtmlRenderer renderer;

    static {
        MutableDataSet options = new MutableDataSet();
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * Converts markdown to asciidoc.
     *
     * @param markdown the markdown source to convert
     * @return asciidoc format
     */
    public static String convertMarkdownToAsciiDoc(String markdown, long timeoutMills) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
