package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BlockImpl extends StructuralNodeImpl implements Block {

    private List<String> lines;

    public BlockImpl(StructuralNode parent, String context) {
        super(parent, context);
        this.lines = new ArrayList<>();
    }

    public BlockImpl(StructuralNode parent, String context, Object content) {
        super(parent, context, content);
        this.lines = new ArrayList<>();
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes) {
        super(parent, context, attributes);
        this.lines = new ArrayList<>();
    }

    public BlockImpl(StructuralNode parent, String context, Object content, Map<String, Object> attributes) {
        super(parent, context, content, attributes);
        this.lines = new ArrayList<>();
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                     Object content, List<StructuralNode> blocks, String contentModel, List<String> subs) {
        super(parent, context, attributes, roles, content, blocks, contentModel, subs);
        this.lines = new ArrayList<>();
    }

    public BlockImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        Object content, List<StructuralNode> blocks, int level, String contentModel, List<String> subs) {
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
