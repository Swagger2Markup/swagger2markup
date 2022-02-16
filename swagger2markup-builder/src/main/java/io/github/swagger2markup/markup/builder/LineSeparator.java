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
package io.github.swagger2markup.markup.builder;

public enum LineSeparator {
    /**
     * Line separator for Unix systems (<tt>\n</tt>).
     */
    UNIX("\n"),
    /**
     * Line separator for Windows systems (<tt>\r\n</tt>).
     */
    WINDOWS("\r\n"),
    /**
     * Line separator for Macintosh systems (<tt>\r</tt>).
     */
    MAC("\r");

    private String lineSeparator;

    LineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    @Override
    public String toString() {
        return lineSeparator;
    }
}
