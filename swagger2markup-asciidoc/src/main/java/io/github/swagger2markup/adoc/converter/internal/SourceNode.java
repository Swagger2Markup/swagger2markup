package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;

import java.util.ArrayList;
import java.util.List;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.*;

public class SourceNode extends BlockListingNode {

    private List<String> sourceAttrs = new ArrayList<>();
    private final Block node;

    public SourceNode(Block node) {
        super(node);
        this.node = node;
    }

    @Override
    public void processPositionalAttributes() {
        String source = pop("1", "style");
        String language = pop("2", "language");
        StringBuilder options = new StringBuilder();
        attributes.forEach((k, v) -> {
            if (k.endsWith(OPTION_SUFFIX)) {
                attributes.remove(k);
                options.append('%').append(k.replace(OPTION_SUFFIX, ""));
            }
        });
        source += options.toString();

        if (StringUtils.isNotBlank(source)) {
            sourceAttrs.add(source);
        }
        if (StringUtils.isNotBlank(language)) {
            sourceAttrs.add(language);
        }
        super.processPositionalAttributes();
    }

    @Override
    public String processAsciiDocContent() {
        StringBuilder sb = new StringBuilder();
        attrsToString(sb, attrs);
        attrsToString(sb, sourceAttrs);
        sb.append(LINE_SEPARATOR).append(DELIMITER_BLOCK).append(LINE_SEPARATOR).append(node.getSource()).append(LINE_SEPARATOR).append(DELIMITER_BLOCK).append(LINE_SEPARATOR);
        return sb.toString();
    }
}
