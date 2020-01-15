package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;

import java.util.*;

public class BlockImpl extends StructuralNodeImpl implements Block {

    private List<String> lines;

    public BlockImpl(StructuralNode parent, String context) {
        this(parent, context, "");
    }

    public BlockImpl(StructuralNode parent, String context, Object content) {
        this(parent, context, new HashMap<>(), content);
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes) {
        this(parent, context, attributes, "");
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes, Object content) {
        this(parent, context, attributes, new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>());
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                     Object content, List<StructuralNode> blocks, String contentModel, List<String> subs) {
        this(parent, context, attributes, roles, content, blocks, calculateLevel(parent), contentModel, subs);
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        Object content, List<StructuralNode> blocks, Integer level, String contentModel, List<String> subs) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.lines = new ArrayList<>();
    }

    @Override
    @Deprecated
    public List<String> lines() {
        return getLines();
    }

    @Override
    public List<String> getLines() {
        return lines;
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    @Override
    @Deprecated
    public String source() {
        return getSource();
    }

    @Override
    public String getSource() {
        return String.join("\n", lines);
    }

    @Override
    public void setSource(String source) {
        setLines(Arrays.asList(source.split("\n")));
    }
}
