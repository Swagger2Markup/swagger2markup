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

import ch.netzwerg.paleo.StringColumn;
import org.junit.Test;

import java.util.List;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static org.assertj.core.api.Assertions.assertThat;

public class TableTest {

    @Test
    public void testTable(){
        StringColumn.Builder typeColumnBuilder = StringColumn.builder(StringColumnId.of("type"));
        typeColumnBuilder.add("type1").add("type2").add("type3");

        StringColumn.Builder nameColumnBuilder = StringColumn.builder(StringColumnId.of("name"));
        nameColumnBuilder.add("name1").add("").add("name3");

        StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of("description"));
        descriptionColumnBuilder.add("").add("").add("");


        Table table = Table.ofAll(typeColumnBuilder.build(), nameColumnBuilder.build(), descriptionColumnBuilder.build());
        assertThat(table.getColumnCount()).isEqualTo(2);
        assertThat(table.getRowCount()).isEqualTo(3);

        List<List<String>> cells = table.getCells();
        assertThat(cells).hasSize(3);
        assertThat(cells.get(0)).hasSize(2);
        assertThat(cells.get(1)).hasSize(2);

        assertThat(table.getColumnSpecs()).hasSize(2);
    }


}
