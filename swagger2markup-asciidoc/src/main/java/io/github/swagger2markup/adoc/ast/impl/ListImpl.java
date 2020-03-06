package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.List;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListImpl extends StructuralNodeImpl implements List {

    private final java.util.List<StructuralNode> items;

    public ListImpl(StructuralNode parent, String context) {
        this(parent, context, new ArrayList<>());
    }


    public ListImpl(StructuralNode parent, String context, java.util.List<StructuralNode> items) {
        this(parent, context, null, items);
    }

    public ListImpl(StructuralNode parent, String context, Object content, java.util.List<StructuralNode> items) {
        this(parent, context, new HashMap<>(), new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>(), items);
    }

    public ListImpl(StructuralNode parent, String context, Map<String, Object> attributes, java.util.List<String> roles,
                    Object content, java.util.List<StructuralNode> blocks,
                    String contentModel, java.util.List<String> subs, java.util.List<StructuralNode> items) {
        this(parent, context, attributes, roles, content, blocks, null != parent ? parent.getLevel() + 1 : 0, contentModel, subs, items);
    }

    public ListImpl(StructuralNode parent, String context, Map<String, Object> attributes, java.util.List<String> roles,
                       Object content, java.util.List<StructuralNode> blocks,
                       Integer level, String contentModel, java.util.List<String> subs, java.util.List<StructuralNode> items) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.items = items;
    }

    @Override
    public java.util.List<StructuralNode> getItems() {
        return items;
    }

    @Override
    public boolean hasItems() {
        return !items.isEmpty();
    }

    @Override
    @Deprecated
    public String render() {
        return convert();
    }

}
