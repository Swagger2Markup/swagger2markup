package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.NodeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DescriptionListEntryImpl extends StructuralNodeImpl implements DescriptionListEntry {

    private final List<ListItem> terms;
    private ListItem description;

    public DescriptionListEntryImpl(StructuralNode parent) {
        this(parent, null, new ArrayList<>());
    }

    public DescriptionListEntryImpl(StructuralNode parent, List<ListItem> terms) {
        this(parent, null, terms);
    }

    public DescriptionListEntryImpl(StructuralNode parent, Object content, List<ListItem> terms) {
        super(parent, "dlist_item", content);
        this.terms = terms;
    }

    public DescriptionListEntryImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                                    Object content, List<StructuralNode> blocks, String contentModel, List<String> subs, List<ListItem> terms) {
        super(parent, "dlist_item", attributes, roles, content, blocks, contentModel, subs);
        this.terms = terms;
    }

    public DescriptionListEntryImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                                    Object content, List<StructuralNode> blocks, Integer level, String contentModel, List<String> subs, List<ListItem> terms) {
        super(parent, "dlist_item", attributes, roles, content, blocks, level, contentModel, subs);
        this.terms = terms;
    }

    @Override
    public List<ListItem> getTerms() {
        return terms;
    }

    @Override
    public ListItem getDescription() {
        return description;
    }

    public void setDescription(final ListItem description) {
        this.description = description;
    }


}
