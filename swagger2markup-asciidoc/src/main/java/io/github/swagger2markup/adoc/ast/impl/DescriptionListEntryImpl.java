package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionListEntryImpl extends StructuralNodeImpl implements DescriptionListEntry {

    private final List<ListItem> terms;
    private ListItem description;

    public DescriptionListEntryImpl(StructuralNode parent) {
        this(parent, new ArrayList<>());
    }

    public DescriptionListEntryImpl(StructuralNode parent, List<ListItem> terms) {
        this(parent, terms, null);
    }

    public DescriptionListEntryImpl(StructuralNode parent, List<ListItem> terms, ListItem description) {
        this(parent, null, terms, description);
    }

    public DescriptionListEntryImpl(StructuralNode parent, Object content, List<ListItem> terms, ListItem description) {
        this(parent, new HashMap<>(), new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>(), terms, description);
    }

    public DescriptionListEntryImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                                    Object content, List<StructuralNode> blocks, String contentModel, List<String> subs,
                                    List<ListItem> terms, ListItem description) {
        this(parent, attributes, roles, content, blocks, null != parent ? parent.getLevel() : 1, contentModel, subs, terms, description);
    }

    public DescriptionListEntryImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                                    Object content, List<StructuralNode> blocks, Integer level, String contentModel,
                                    List<String> subs, List<ListItem> terms, ListItem description) {
        super(parent, "dlist_item", attributes, roles, content, blocks, level, contentModel, subs);
        this.terms = terms;
        this.description = description;
    }

    @Override
    public List<ListItem> getTerms() {
        return terms;
    }

    public boolean addTerm(ListItem term) {
        return terms.add(term);
    }

    @Override
    public ListItem getDescription() {
        return description;
    }

    public void setDescription(final ListItem description) {
        this.description = description;
    }


}
