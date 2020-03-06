package io.github.swagger2markup.adoc.ast.impl;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListItemImpl extends StructuralNodeImpl implements ListItem {

    private final String marker;
    private String text;

    public ListItemImpl(StructuralNode parent, String text) {
        this(parent, "list_item", null, "*", text);
    }

    public ListItemImpl(StructuralNode parent, String context, Object content, String marker, String text) {
        this(parent, context, new HashMap<>(), new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>(), marker, text);
    }

    public ListItemImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        Object content, List<StructuralNode> blocks, String contentModel,
                        List<String> subs, String marker, String text) {
        this(parent, context, attributes, roles, content, blocks, null != parent ? parent.getLevel() : 1, contentModel, subs, marker, text);
    }

    public ListItemImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        Object content, List<StructuralNode> blocks, Integer level,
                        String contentModel, List<String> subs, String marker, String text) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.marker = marker;
        this.text = text;
    }

    @Override
    public String getMarker() {
        return marker;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getSource() {
        return text;
    }

    @Override
    public void setSource(String source) {
        this.text = source;
    }

    @Override
    public boolean hasText() {
        return StringUtils.isNotBlank(text);
    }
}
