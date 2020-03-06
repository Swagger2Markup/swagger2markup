package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SectionImpl extends StructuralNodeImpl implements Section {

    private final Integer index;
    private final Integer number;
    private final String numeral;
    private final String sectionName;
    private final boolean special;
    private final boolean numbered;

    public SectionImpl(StructuralNode parent) {
        this(parent, new HashMap<>());
    }

    public SectionImpl(StructuralNode parent, Map<String, Object> attributes) {
        this(parent, "section", null, "");
    }


    public SectionImpl(StructuralNode parent, String context, Object content, String sectionName) {
        this(parent, context, content, null, null, "", sectionName, false, false);
    }

    public SectionImpl(StructuralNode parent, String context, Object content, Integer index, Integer number, String numeral,
                       String sectionName, boolean special, boolean numbered) {
        this(parent, context, new HashMap<>(), new ArrayList<>(), content, new ArrayList<>(),
                "", new ArrayList<>(), index, number, numeral, sectionName, special, numbered);
    }

    public SectionImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                       Object content, List<StructuralNode> blocks, String contentModel, List<String> subs,
                       Integer index, Integer number, String numeral, String sectionName, boolean special, boolean numbered) {
        this(parent, context, attributes, roles, content, blocks, calculateLevel(parent), contentModel, subs, index, number, numeral, sectionName, special, numbered);
    }

    public SectionImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                          Object content, List<StructuralNode> blocks, Integer level, String contentModel, List<String> subs,
                          Integer index, Integer number, String numeral, String sectionName, boolean special, boolean numbered) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
        this.index = index;
        this.number = number;
        this.numeral = numeral;
        this.sectionName = sectionName;
        this.special = special;
        this.numbered = numbered;
    }

    @Override
    @Deprecated
    public int index() {
        return getIndex();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    @Deprecated
    public int number() {
        return getNumber();
    }

    @Override
    @Deprecated
    public int getNumber() {
        return number;
    }

    @Override
    public String getNumeral() {
        return numeral;
    }

    @Override
    @Deprecated
    public String sectname() {
        return getSectionName();
    }

    @Override
    public String getSectionName() {
        return sectionName;
    }

    @Override
    @Deprecated
    public boolean special() {
        return isSpecial();
    }

    @Override
    public boolean isSpecial() {
        return special;
    }

    @Override
    @Deprecated
    public boolean numbered() {
        return isNumbered();
    }

    @Override
    public boolean isNumbered() {
        return numbered;
    }

}
