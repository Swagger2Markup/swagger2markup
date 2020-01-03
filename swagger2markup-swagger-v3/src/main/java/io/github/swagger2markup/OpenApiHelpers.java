package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.BlockImpl;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;

public class OpenApiHelpers {

    public static void appendDescription(StructuralNode node, String description) {
        if (StringUtils.isNotBlank(description)) {
            Block paragraph = new BlockImpl(node, "paragraph");
            paragraph.setSource(description);
            node.append(paragraph);
        }
    }
}
