/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.swagger2markup.internal.utils;

import ch.netzwerg.paleo.DataFrame;
import ch.netzwerg.paleo.StringColumn;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import io.github.swagger2markup.markup.builder.MarkupTableColumn;
import javaslang.collection.Array;
import javaslang.collection.IndexedSeq;
import javaslang.collection.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Table {

    public static final String WIDTH_RATIO = "widthRatio";
    public static final String HEADER_COLUMN = "headerColumn";

    private static final Logger LOG = LoggerFactory.getLogger(Table.class);

    private final DataFrame dataFrame;
    private final java.util.List<java.util.List<String>> cells;
    private final java.util.List<MarkupTableColumn> columnSpecs;

    private Table(DataFrame dataFrame) {
        this.dataFrame = dataFrame;

        columnSpecs = dataFrame.getColumns().map(column -> {
                    Integer widthRatio = Integer.valueOf(column.getMetaData().get(WIDTH_RATIO).getOrElse("0"));
                    return new MarkupTableColumn(column.getId().getName())
                            .withWidthRatio(widthRatio)
                            .withHeaderColumn(Boolean.parseBoolean(column.getMetaData().get(HEADER_COLUMN).getOrElse("false")))
                            .withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^" + widthRatio);
                }
        ).toJavaList();

        IndexedSeq<IndexedSeq<String>> columnValues = dataFrame.getColumns()
                .map(column -> ((StringColumn) column).getValues());

        cells = Array.range(0, getRowCount())
                .map(rowNumber -> columnValues.map(values -> values.get(rowNumber)).toJavaList()).toJavaList();
    }

    public static Table ofAll(StringColumn... columns) {
        return new Table(DataFrame.ofAll(List.of(columns).filter(Table::isNotBlank)));
    }

    public static boolean isNotBlank(StringColumn column) {
        return !column.getValues().filter(StringUtils::isNotBlank).isEmpty();
    }

    public int getColumnCount() {
        return dataFrame.getColumnCount();
    }

    public int getRowCount() {
        return dataFrame.getRowCount();
    }

    public java.util.List<MarkupTableColumn> getColumnSpecs(){
        return columnSpecs;
    }

    public java.util.List<java.util.List<String>> getCells() {
        return cells;
    }
}
