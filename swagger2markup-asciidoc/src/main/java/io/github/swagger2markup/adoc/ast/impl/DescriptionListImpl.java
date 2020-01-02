package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.StructuralNode;

import java.util.List;
import java.util.Map;

public class DescriptionListImpl extends StructuralNodeImpl implements DescriptionList {

    private List<DescriptionListEntry> items;

    public DescriptionListImpl(StructuralNode parent, String context, Object content, List<DescriptionListEntry> items) {
        super(parent, context, content);
        this.items = items;
    }

    public DescriptionListImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                               List<String> options, Object content, List<StructuralNode> blocks, String contentModel,
                               List<String> subs, List<DescriptionListEntry> items) {
        super(parent, context, attributes, roles, content, blocks, contentModel, subs);
        this.items = items;
    }

    public DescriptionListImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                               List<String> options, Object content, List<StructuralNode> blocks, Integer level,
                               String contentModel, List<String> subs, List<DescriptionListEntry> items) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.items = items;
    }

    public DescriptionListImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                               List<String> options, Object content, List<StructuralNode> blocks,
                               int level, String contentModel, List<String> subs) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
    }

    @Override
    public List<DescriptionListEntry> getItems() {
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
