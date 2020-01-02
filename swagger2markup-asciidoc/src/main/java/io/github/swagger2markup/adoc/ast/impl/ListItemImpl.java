package io.github.swagger2markup.adoc.ast.impl;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListItemImpl extends StructuralNodeImpl implements ListItem {

    private final String marker;
    private String text;

    public ListItemImpl(StructuralNode parent, String text) {
        super(parent, "list_item", new HashMap<>());
        this.marker = "*";
        this.text = text;
    }

    public ListItemImpl(StructuralNode parent, String context, Object content, String marker, String text) {
        super(parent, context, content);
        this.marker = marker;
        this.text = text;
    }

    public ListItemImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        List<String> options, Object content, List<StructuralNode> blocks, String contentModel,
                        List<String> subs, String marker, String text) {
        super(parent, context, attributes, roles, content, blocks, contentModel, subs);
        this.marker = marker;
        this.text = text;
    }

    public ListItemImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        List<String> options, Object content, List<StructuralNode> blocks, int level,
                        String contentModel, List<String> subs, String marker, String text) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.marker = marker;
        this.text = text;
    }

    public ListItemImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                           List<String> options, Object content, List<StructuralNode> blocks, int level,
                           String contentModel, List<String> subs, String marker) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.marker = marker;
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
