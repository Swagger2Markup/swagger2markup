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
package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class MarkupDocument {

    private MarkupDocBuilder markupDocBuilder;

    public MarkupDocument(MarkupDocBuilder markupDocBuilder) {
        this.markupDocBuilder = markupDocBuilder;
    }

    /**
     * Returns a string representation of the document.
     */
    public String toString() {
        return markupDocBuilder.toString();
    }

    /**
     * Writes the content of the builder to a file.<br>
     * An extension identifying the markup language will be automatically added to file name.
     *
     * @param file    the generated file
     * @param charset the the charset to use for encoding
     * @param options the file open options
     */
    public void writeToFile(Path file, Charset charset, OpenOption... options) {
        markupDocBuilder.writeToFile(file, charset, options);
    }

    /**
     * Writes the content of the builder to a file.
     *
     * @param file    the generated file
     * @param charset the the charset to use for encoding
     * @param options the file open options
     */
    public void writeToFileWithoutExtension(Path file, Charset charset, OpenOption... options) {
        markupDocBuilder.writeToFileWithoutExtension(file, charset, options);
    }
}
