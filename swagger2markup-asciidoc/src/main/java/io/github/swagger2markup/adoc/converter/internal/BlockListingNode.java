package io.github.swagger2markup.adoc.converter.internal;

import org.asciidoctor.ast.Block;

import java.util.List;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.*;

public class BlockListingNode extends ParagraphAttributes {

    final private Block node;

    public BlockListingNode(Block node) {
        super(node);
        this.node = node;
    }

    @Override
    public String processAsciiDocContent() {
        StringBuilder sb = new StringBuilder();
        attrsToString(sb, attrs);
        sb.append(LINE_SEPARATOR).append(DELIMITER_BLOCK).append(LINE_SEPARATOR).append(node.getSource()).append(LINE_SEPARATOR).append(DELIMITER_BLOCK).append(LINE_SEPARATOR);
        return sb.toString();
    }

    void attrsToString(StringBuilder sb, List<String> list) {
        if (!list.isEmpty()) {
            sb.append(ATTRIBUTES_BEGIN).append(String.join(",", list)).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        }
    }
}
