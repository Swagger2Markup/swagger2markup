package io.github.swagger2markup.markup.builder.internal.asciidoc;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import nl.jworks.markdown_to_asciidoc.Converter;
import nl.jworks.markdown_to_asciidoc.ToAsciiDocSerializer;

public class AsciiDocConverterExtension extends Converter {

    /**
     * Converts markdown to asciidoc.
     *
     * @param markdown the markdown source to convert
     * @param timeoutMills parsing timeout
     * @return asciidoc format
     */
    public static String convertMarkdownToAsciiDoc(String markdown, long timeoutMills) {
        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL, timeoutMills);
        // insert blank line before fenced code block if necessary
        if (markdown.contains("```")) {
            markdown = markdown.replaceAll("(?m)(?<!\n\n)(\\s*)```(\\w*\n)((?:\\1[^\n]*\n)+)\\1```", "\n$1```$2$3$1```");
        }
        char[] markDown = markdown.toCharArray();
        RootNode rootNode = processor.parseMarkdown(markDown);
        return new ToAsciiDocSerializer(rootNode, markdown).toAsciiDoc();
    }
}
