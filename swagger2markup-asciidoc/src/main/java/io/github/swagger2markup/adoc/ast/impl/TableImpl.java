package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TableImpl extends StructuralNodeImpl implements Table {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String FRAME_ATTR = "frame";

    private static final String GRID_ATTR = "grid";

    private Rows rows;

    public TableImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                     Object content, List<StructuralNode> blocks, int level, String contentModel, List<String> subs) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
    }

    @Override
    public boolean hasHeaderOption() {
        return isOption("header");
    }

    @Override
    public String getFrame() {
        return (String) getAttribute(FRAME_ATTR, "all");
    }

    @Override
    public void setFrame(String frame) {
        setAttribute(FRAME_ATTR, frame, true);
    }

    @Override
    public String getGrid() {
        return (String) getAttribute(GRID_ATTR, "all");
    }

    @Override
    public void setGrid(String grid) {
        setAttribute(GRID_ATTR, grid, true);
    }

    @Override
    public List<Column> getColumns() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public List<Row> getFooter() {
        return rows.getFooter();
    }

    @Override
    public List<Row> getBody() {
        return rows.getBody();
    }

    @Override
    public List<Row> getHeader() {
        return rows.getHeader();
    }

    private class Rows {

        private final RowList headerRows;
        private final RowList bodyRows;
        private RowList footerRows;

        private Rows(RowList headerRows, RowList bodyRows) {
            this.headerRows = headerRows;
            this.bodyRows = bodyRows;
            this.footerRows = new RowList(new ArrayList<>());
        }

        private Rows(RowList headerRows, RowList bodyRows, RowList footerRows) {
            this.headerRows = headerRows;
            this.bodyRows = bodyRows;
            this.footerRows = footerRows;
        }

        private RowList getHeader() {
            return headerRows;
        }

        private RowList getBody() {
            return bodyRows;
        }

        private RowList getFooter() {
            return footerRows;
        }

        private void setFooterRow(Row row) {
            footerRows.clear();
            footerRows.add(row);
        }

    }


    class RowList extends AbstractList<Row> {

        private final List<Row> rubyArray;

        private RowList(List<Row> rubyArray) {
            this.rubyArray = rubyArray;
        }

        @Override
        public int size() {
            return rubyArray.size();
        }

        @Override
        public boolean isEmpty() {
            return rubyArray.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return rubyArray.contains(o);
        }

        @Override
        public boolean add(Row row) {
            boolean changed = false;
            try {
                changed = rubyArray.add(row);
                setAttribute("rowcount", size(), true);
            } catch (Exception e) {
                logger.debug("Couldn't add row",e);
            }
            return changed;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof RowImpl)) {
                return false;
            }
            try {
                boolean changed = rubyArray.remove(o);
                setAttribute("rowcount", size(), true);
                return changed;
            } catch (Exception e) {
                logger.debug("Couldn't add row",e);
                return false;
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            rubyArray.clear();
            setAttribute("rowcount", size(), true);
        }

        @Override
        public Row get(int index) {
            return rubyArray.get(index);
        }

        @Override
        public Row set(int index, Row element) {
            Row oldRow = get(index);
            rubyArray.set(index, element);
            return oldRow;
        }

        @Override
        public void add(int index, Row element) {
            rubyArray.add(index, element);
            setAttribute("rowcount", size(), true);
        }

        @Override
        public Row remove(int index) {
            Row removed = rubyArray.remove(index);
            setAttribute("rowcount", size(), true);
            return removed;
        }

        @Override
        public int indexOf(Object o) {
            if (!(o instanceof RowImpl)) {
                return -1;
            }
            return rubyArray.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            if (!(o instanceof RowImpl)) {
                return -1;
            }
            return rubyArray.lastIndexOf(o);
        }
    }
}
