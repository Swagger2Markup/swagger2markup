package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.ContentNode;

public class BlockImageNode extends NodeAttributes {

    final private String target;

    public BlockImageNode(ContentNode node) {
        super(node.getAttributes());
        target = pop("target").replaceAll("\\s", "{sp}");
    }

    public String getTarget() {
        return target;
    }

    @Override
    public void processPositionalAttributes() {
        String attr1 = pop("1", "alt");
        if (StringUtils.isNotBlank(attr1)) {
            attrs.add(attr1);
        }

        String attr2 = pop("2", "width");
        if (StringUtils.isNotBlank(attr2)) {
            attrs.add(attr2);
        }

        String attr3 = pop("3", "height");
        if (StringUtils.isNotBlank(attr3)) {
            attrs.add(attr3);
        }
    }

    @Override
    void processAttributes() {
        attributes.forEach((k, v) -> {
            if (!k.equals("role") && null != v) {
                attrs.add(k + "=" + v);
            }
        });
    }

    @Override
    public String processAsciiDocContent() {
        return "image::" + target + '[' + String.join(",", attrs) + ']';
    }
}
