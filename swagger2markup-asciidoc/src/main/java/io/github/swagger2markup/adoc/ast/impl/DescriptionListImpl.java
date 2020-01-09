package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionListImpl extends StructuralNodeImpl implements DescriptionList {

    public static final String CONTEXT = "dlist";
    private List<DescriptionListEntry> items;

    public DescriptionListImpl(StructuralNode parent) {
        this(parent, new ArrayList<>());
    }

    public DescriptionListImpl(StructuralNode parent, List<DescriptionListEntry> items) {
        this(parent, null, items);
    }

    public DescriptionListImpl(StructuralNode parent, Object content, List<DescriptionListEntry> items) {
        this(parent, new HashMap<>(), new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>(), items);
    }

    public DescriptionListImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                               Object content, List<StructuralNode> blocks, String contentModel,
                               List<String> subs, List<DescriptionListEntry> items) {
        this(parent, attributes, roles, content, blocks, calculateLevel(parent), contentModel, subs, items);
    }

    public DescriptionListImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                               Object content, List<StructuralNode> blocks, Integer level,
                               String contentModel, List<String> subs, List<DescriptionListEntry> items) {
        super(parent, CONTEXT, attributes, roles, content, blocks, level, contentModel, subs);
        this.items = items;
    }

    @Override
    public List<DescriptionListEntry> getItems() {
        return items;
    }

    public void setItems(List<DescriptionListEntry> items) {
        this.items = items;
    }

    public void addEntry(DescriptionListEntry entry) {
        this.items.add(entry);
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

    protected static Integer calculateLevel(StructuralNode parent) {
        int level = 1;
        if (parent instanceof DescriptionList)
            level = parent.getLevel() + 1;
        return level;
    }
}
