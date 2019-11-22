package io.github.swagger2markup.adoc.converter.internal;

import org.asciidoctor.ast.Table;

public class TableNode extends DelimitedBlockNode {
    public TableNode(Table table) {
        super(table);
    }

    @Override
    void processAttributes() {
        pop("colcount", "rowcount", "tablepcwidth");
        super.processAttributes();
    }
}
