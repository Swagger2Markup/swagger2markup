/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup.assertions;


import com.sksamuel.diffpatch.DiffMatchPatch;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.AbstractAssert;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffAssert extends AbstractAssert<DiffAssert, Path>{

    public DiffAssert(Path actual) {
        super(actual, DiffAssert.class);
    }

    /**
     * Verifies that the content of the actual File is equal to the given one.
     *
     * @param expected the given value to compare the actual value to.
     * @param reportPath the path to the report which should be generated if the files differ.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public DiffAssert isEqualTo(Path expected, Path reportPath) {
        LinkedList<DiffMatchPatch.Diff> diffs = diff(actual, expected);
        boolean allDiffsAreEqual = assertThatAllDiffsAreEqual(diffs);
        if(!allDiffsAreEqual){
            writeHtmlReport(reportPath, diffs);
        }
        assertThat(allDiffsAreEqual).as("The content of the following files differ. Actual: %s, Expected %s. Check the HTML report for more details: %s", actual.toAbsolutePath(), expected.toAbsolutePath(), reportPath.toAbsolutePath()).isTrue();
        return myself;
    }

    public boolean assertThatAllDiffsAreEqual(LinkedList<DiffMatchPatch.Diff> diffs){
        for(DiffMatchPatch.Diff diff : diffs){
            if(diff.operation == DiffMatchPatch.Operation.DELETE || diff.operation == DiffMatchPatch.Operation.INSERT){
                return false;
            }
        }
        return true;
    }

    private static LinkedList<DiffMatchPatch.Diff> diff(Path actual, Path expected){
        DiffMatchPatch differ = new DiffMatchPatch();
        try {
            return differ.diff_main(IOUtils.toString(expected.toUri()), IOUtils.toString(actual.toUri()), false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to diff files.", e);
        }
    }

    private static void writeHtmlReport(Path reportPath, LinkedList<DiffMatchPatch.Diff> diffs){
        DiffMatchPatch differ = new DiffMatchPatch();
        try {
            Files.createDirectories(reportPath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(reportPath, Charset.forName("UTF-8"))) {
                writer.write(differ.diff_prettyHtml(diffs));
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write report %s", reportPath.toAbsolutePath()), e);
        }
    }
}
