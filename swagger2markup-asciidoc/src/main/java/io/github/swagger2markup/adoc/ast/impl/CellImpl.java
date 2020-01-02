package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Table;

import java.util.List;
import java.util.Map;

public class CellImpl extends ContentNodeImpl implements Cell {

    private final int colspan;
    private final int rowspan;
    private String text;
    private String style;
    private Document innerDocument;

    public CellImpl(Column parent, String context, Map<String, Object> attributes, List<String> roles,
                    int colspan, int rowspan) {
        super(parent, context, attributes, roles);
        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    @Override
    public Column getColumn() {
        return (Column) getParent();
    }

    @Override
    public int getColspan() {
        return colspan;
    }

    @Override
    public int getRowspan() {
        return rowspan;
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
    public Object getContent() {
        return text;
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
    public Table.HorizontalAlignment getHorizontalAlignment() {
        return Table.HorizontalAlignment.valueOf(((String) getAttribute("halign", "left")).toUpperCase());
    }

    @Override
    public void setHorizontalAlignment(Table.HorizontalAlignment halign) {
        setAttribute("halign", halign.name().toLowerCase(), true);
    }

    @Override
    public Table.VerticalAlignment getVerticalAlignment() {
        return Table.VerticalAlignment.valueOf(((String) getAttribute("valign", "top")).toUpperCase());
    }

    @Override
    public void setVerticalAlignment(Table.VerticalAlignment valign) {
        setAttribute("valign", valign.name().toLowerCase(), true);
    }

    @Override
    public Document getInnerDocument() {
        return innerDocument;
    }

    @Override
    public void setInnerDocument(Document document) {
        this.innerDocument = document;
    }

}
