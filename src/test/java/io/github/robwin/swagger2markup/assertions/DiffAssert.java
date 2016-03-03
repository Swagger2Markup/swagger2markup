/*
 *
 *  Copyright 2015 Robert Winkler
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
import java.nio.file.Paths;
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
     * @param reportName the name of the report which should be generated if the files differ.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public DiffAssert isEqualTo(Path expected, String reportName) {
        LinkedList<DiffMatchPatch.Diff> diffs = diff(actual, expected);
        boolean allDiffsAreEqual = assertThatAllDiffsAreEqual(diffs);
        if(!allDiffsAreEqual){
            writeHtmlReport(reportName, diffs);
        }
        assertThat(allDiffsAreEqual).as("The content of the files differ. Check the HTML report for more details.").isTrue();
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

    private static void writeHtmlReport(String reportName, LinkedList<DiffMatchPatch.Diff> diffs){
        DiffMatchPatch differ = new DiffMatchPatch();
        String reportFolder = "build/diff-report";
        try {
            Files.createDirectories(Paths.get(reportFolder));
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(reportFolder, reportName), Charset.forName("UTF-8"))) {
                writer.write(differ.diff_prettyHtml(diffs));
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write report into folder %s", reportFolder), e);
        }
    }
}
