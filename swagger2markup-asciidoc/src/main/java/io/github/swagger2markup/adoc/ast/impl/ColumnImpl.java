package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnImpl extends ContentNodeImpl implements Column {

    private String style;
    private Number columnNumber = -1;
    private Number width = 0;

    public ColumnImpl(Table parent) {
        this(parent, "table_column", new HashMap<>(), new ArrayList<>());
    }

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
        return columnNumber.intValue();
    }

    public void setColumnNumber(Integer columnNumber) {
        setAttribute("colnumber", columnNumber, true);
        this.columnNumber = columnNumber;
    }

    @Override
    public int getWidth() {
        return width.intValue();
    }

    @Override
    public void setWidth(int width) {
        setAttribute("width", width, true);
        this.width = width;
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
