package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;

import java.util.List;

public class RowImpl implements Row {

    private final List<Cell> cells;

    public RowImpl(List<Cell> cells) {
        this.cells = cells;
    }

    @Override
    public List<Cell> getCells() {
        return cells;
    }

}
