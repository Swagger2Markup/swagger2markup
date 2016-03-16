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

package io.github.swagger2markup.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class IOUtils {
    private static final Pattern NAME_FORBIDDEN_PATTERN = Pattern.compile("[^0-9A-Za-z-_]+");

    /**
     * Create a normalized name from an arbitrary string.<br>
     * Paths separators are replaced, so this function can't be applied on a whole path, but must be called on each path sections.
     *
     * @param name current name of the file
     * @return a normalized filename
     */
    public static String normalizeName(String name) {
        String fileName = NAME_FORBIDDEN_PATTERN.matcher(name).replaceAll("_");
        fileName = fileName.replaceAll(String.format("([%1$s])([%1$s]+)", "-_"), "$1");
        fileName = StringUtils.strip(fileName, "_-");
        fileName = fileName.trim();
        return fileName;
    }

    /**
     * Create a reader from specified {@code source}.<br>
     * Returned reader should be explicitly closed after use.
     *
     * @param uri source URI
     * @return reader
     * @throws IOException if the connection cannot be opened
     */
    public static Reader uriReader(URI uri) throws IOException {
        return new BufferedReader(new InputStreamReader(uri.toURL().openStream(), StandardCharsets.UTF_8));
    }

}
