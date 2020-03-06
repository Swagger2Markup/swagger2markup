package io.github.swagger2markup.adoc.converter.internal;

public class TableCellStyle {
    public final TableCellHorizontalAlignment horizontalAlignment;
    public final TableCellVerticalAlignment verticalAlignment;
    public final Style style;
    public final int width;

    public TableCellStyle(TableCellHorizontalAlignment horizontalAlignment, TableCellVerticalAlignment verticalAlignment, Style style, int width) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.style = style;
        this.width = width;
    }
}
