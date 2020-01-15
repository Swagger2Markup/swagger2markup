package io.github.swagger2markup.adoc.ast.impl;

import io.github.swagger2markup.adoc.AsciidocConverter;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.PhraseNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhraseNodeImpl extends ContentNodeImpl implements PhraseNode {

    private final String type;
    private final String text;
    private final String target;
    private final AsciidocConverter converter = new AsciidocConverter(AsciidocConverter.NAME, new HashMap<>());

    public PhraseNodeImpl(ContentNode parent, String context, Map<String, Object> attributes, List<String> roles, String type, String text, String target) {
        super(parent, context, attributes, roles);
        this.type = type;
        this.text = text;
        this.target = target;
    }

    @Override
    @Deprecated
    public String render() {
        return convert();
    }

    @Override
    public String convert() {
        return converter.convert(this, null, new HashMap<>());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getTarget() {
        return target;
    }
}
