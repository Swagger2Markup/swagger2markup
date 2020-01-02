package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Table;

import java.util.List;
import java.util.Map;

public class ColumnImpl extends ContentNodeImpl implements Column {

    private String style;

    public ColumnImpl(Table parent, String context, Map<String, Object> attributes, List<String> roles) {
        super(parent, context, attributes, roles);
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
    public Table getTable() {
        return (Table) getParent();
    }

    @Override
    public int getColumnNumber() {
        Number columnNumber = (Number) getAttribute("colnumber");
        return columnNumber == null ? -1 : columnNumber.intValue();
    }

    @Override
    public int getWidth() {
        Number width = (Number) getAttribute("width");
        return width == null ? 0 : width.intValue();
    }

    @Override
    public void setWidth(int width) {
        setAttribute("width", width, true);
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

}
