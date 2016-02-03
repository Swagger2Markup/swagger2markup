package io.github.robwin.markup.builder;

public class TableColumnSpec {
    public String header;
    public Integer widthRatio = 0;

    public TableColumnSpec() {
    }

    public TableColumnSpec(String header, Integer widthRatio) {
        this.header = header;
        this.widthRatio = widthRatio;
    }

    public TableColumnSpec withHeader(String header) {
        this.header = header;
        return this;
    }

    public TableColumnSpec withWidthRatio(Integer widthRatio) {
        this.widthRatio = widthRatio;
        return this;
    }
}