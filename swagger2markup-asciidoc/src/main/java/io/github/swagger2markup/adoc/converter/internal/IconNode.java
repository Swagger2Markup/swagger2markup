package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.ContentNode;

public class IconNode extends NodeAttributes {

    final private String alt;

    public IconNode(ContentNode node) {
        super(node.getAttributes());
        alt = pop("alt", "default-alt");
    }

    public String getAlt() {
        return alt;
    }

    @Override
    void processPositionalAttributes() {
        String attr1 = pop("1", "size");
        if (StringUtils.isNotBlank(attr1)) {
            attrs.add(attr1);
        }
    }

    @Override
    void processAttributes() {
        attributes.forEach((k, v) -> {
            attrs.add(k + "=" + v);
        });
    }

    @Override
    String processAsciiDocContent() {
        return "icon:" + alt + '[' + String.join(",", attrs) + ']';
    }
}
