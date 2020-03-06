package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.StructuralNode;

import java.util.List;
import java.util.Map;

public class ParagraphBlockImpl extends BlockImpl {

    public static final String CONTEXT = "paragraph";

    public ParagraphBlockImpl(StructuralNode parent) {
        super(parent, CONTEXT);
    }

    public ParagraphBlockImpl(StructuralNode parent, Object content) {
        super(parent, CONTEXT, content);
    }

    public ParagraphBlockImpl(StructuralNode parent, Map<String, Object> attributes) {
        super(parent, CONTEXT, attributes);
    }

    public ParagraphBlockImpl(StructuralNode parent, Map<String, Object> attributes, Object content) {
        super(parent, CONTEXT, attributes, content);
    }

    public ParagraphBlockImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                              Object content, List<StructuralNode> blocks, String contentModel, List<String> subs) {
        super(parent, CONTEXT, attributes, roles, content, blocks, contentModel, subs);
    }

    public ParagraphBlockImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                              Object content, List<StructuralNode> blocks, Integer level, String contentModel,
                              List<String> subs) {
        super(parent, CONTEXT, attributes, roles, content, blocks, level, contentModel, subs);
    }
}
