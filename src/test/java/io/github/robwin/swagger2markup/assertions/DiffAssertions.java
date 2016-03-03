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

import org.apache.commons.lang3.Validate;

import java.nio.file.Path;

/**
 * Entry point for assertion methods for diffing files.
 *
 * @author Robert Winkler
 */
public class DiffAssertions {

    /**
     * Creates a new instance of <code>{@link DiffAssert}</code>.
     *
     * @param actual the the actual File path.
     * @return the created assertion object.
     */

    public static DiffAssert assertThat(Path actual) {
        Validate.notNull(actual, "actual must not be null!");
        return new DiffAssert(actual);
    }
}
