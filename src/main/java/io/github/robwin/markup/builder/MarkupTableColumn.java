package io.github.robwin.markup.builder;

public class MarkupTableColumn {
    public String header;
    public Integer widthRatio = 0;

    public MarkupTableColumn() {
    }

    public MarkupTableColumn(String header, Integer widthRatio) {
        this.header = header;
        this.widthRatio = widthRatio;
    }

    public MarkupTableColumn withHeader(String header) {
        this.header = header;
        return this;
    }

    public MarkupTableColumn withWidthRatio(Integer widthRatio) {
        this.widthRatio = widthRatio;
        return this;
    }
}