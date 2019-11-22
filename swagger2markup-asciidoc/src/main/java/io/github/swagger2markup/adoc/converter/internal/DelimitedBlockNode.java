package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.*;

public class DelimitedBlockNode extends ParagraphAttributes {

    public DelimitedBlockNode(StructuralNode node) {
        super(node);
    }

    @Override
    public void processPositionalAttributes() {
        String source = pop("1", "style");
        StringBuilder options = new StringBuilder();
        attributes.forEach((k, v) -> {
            if (k.endsWith(OPTION_SUFFIX)) {
                attributes.remove(k);
                options.append('%').append(k.replace(OPTION_SUFFIX, ""));
            }
        });
        source += options.toString();

        if (StringUtils.isNotBlank(source)) {
            attrs.add(source);
        }
        super.processPositionalAttributes();
    }

    @Override
    public String processAsciiDocContent() {
        StringBuilder sb = new StringBuilder();
        if (!attrs.isEmpty()) {
            sb.append(ATTRIBUTES_BEGIN).append(String.join(",", attrs)).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }
}
