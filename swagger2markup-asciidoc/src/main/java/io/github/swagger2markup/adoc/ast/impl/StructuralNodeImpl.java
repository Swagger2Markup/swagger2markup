package io.github.swagger2markup.adoc.ast.impl;

import io.github.swagger2markup.adoc.AsciidocConverter;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;

import java.util.*;

public class StructuralNodeImpl extends ContentNodeImpl implements StructuralNode {

    private String title;
    private String caption;
    private String style;
    private final Object content;
    private final List<StructuralNode> blocks;
    private Integer level;
    private final String contentModel;
    private List<String> subs;
    private final AsciidocConverter converter = new AsciidocConverter(AsciidocConverter.NAME, new HashMap<>());

    public StructuralNodeImpl(StructuralNode parent, String context) {
        this(parent, context, new HashMap<>());
    }

    public StructuralNodeImpl(StructuralNode parent, String context, Map<String, Object> attributes) {
        this(parent, context, attributes, null);
    }

    public StructuralNodeImpl(StructuralNode parent, String context, Object content) {
        this(parent, context, new HashMap<>(), content);
    }

    public StructuralNodeImpl(StructuralNode parent, String context, Map<String, Object> attributes, Object content) {
        this(parent, context, attributes, new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>());
    }

    public StructuralNodeImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                              Object content, List<StructuralNode> blocks, String contentModel, List<String> subs) {
        this(parent, context, attributes, roles, content, blocks, calculateLevel(parent), contentModel, subs);
    }

    public StructuralNodeImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                              Object content, List<StructuralNode> blocks, Integer level, String contentModel, List<String> subs) {
        super(parent, context, attributes, roles);
        this.content = content;
        this.blocks = blocks;
        this.level = level;
        this.contentModel = contentModel;
        this.subs = subs;
    }

    @Override
    @Deprecated
    public String title() {
        return getTitle();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    @Deprecated
    public String style() {
        return getStyle();
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    @Deprecated
    public List<StructuralNode> blocks() {
        return getBlocks();
    }

    @Override
    public List<StructuralNode> getBlocks() {
        return blocks;
    }

    @Override
    public void append(StructuralNode block) {
        blocks.add(block);
    }

    @Override
    @Deprecated
    public Object content() {
        return getContent();
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public String convert() {
        return converter.convert(this, null, new HashMap<>());
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Cursor getSourceLocation() {
        return new CursorImpl();
    }

    @Override
    public String getContentModel() {
        return contentModel;
    }

    @Override
    public List<String> getSubstitutions() {
        return subs;
    }

    @Override
    public boolean isSubstitutionEnabled(String substitution) {
        return subs.contains(substitution);
    }

    @Override
    public void removeSubstitution(String substitution) {
        subs.remove(substitution);
    }

    @Override
    public void addSubstitution(String substitution) {
        subs.add(substitution);
    }

    @Override
    public void prependSubstitution(String substitution) {
    }

    @Override
    public void setSubstitutions(String... substitutions) {
        subs = Arrays.asList(substitutions);
    }

    @Override
    public List<StructuralNode> findBy(Map<Object, Object> selector) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    protected static Integer calculateLevel(StructuralNode parent) {
        return null != parent ? parent.getLevel() + 1 : 0;
    }

}
