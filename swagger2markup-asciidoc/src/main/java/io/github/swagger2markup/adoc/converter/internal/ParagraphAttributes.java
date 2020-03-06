package io.github.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParagraphAttributes extends NodeAttributes {

    public static final String OPTION_SUFFIX = "-option";
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ParagraphAttributes(StructuralNode node) {
        super(node.getAttributes());
    }

    @Override
    public void processPositionalAttributes() {
        String attr1 = pop("1", "style");
        if (StringUtils.isNotBlank(attr1)) {
            attrs.add(attr1);
        }
    }

    @Override
    void processAttributes() {
        String id = pop("id");
        if (StringUtils.isNotBlank(id)) {
            id = "#" + id;
        }
        String roles = String.join(".", pop("role").split(" "));
        if (StringUtils.isNotBlank(roles)) {
            roles = "." + roles;
        }
        StringBuilder options = new StringBuilder();
        List<String> namedAttributes = new ArrayList<>();

        attributes.forEach((k, v) -> {
            if (k.equals(TITLE)) {
                logger.debug("Skipping attribute: " + TITLE);
            } else if (k.endsWith(OPTION_SUFFIX)) {
                options.append('%').append(k.replace(OPTION_SUFFIX, ""));
            } else if (null != v) {
                if(v.toString().contains(" ") || v.toString().contains(",")) {
                    namedAttributes.add(k + "=\"" + v +"\"");
                } else {
                    namedAttributes.add(k + "=" + v);
                }
            } else {
                logger.warn("Don't know how to handle key: " + k);
            }
        });

        String nonNamedAttributes = id + roles + options.toString();
        if (StringUtils.isNotBlank(nonNamedAttributes)) {
            attrs.add(nonNamedAttributes);
        }
        attrs.addAll(namedAttributes);
    }
}
