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


import java.net.URI;
import java.nio.file.Paths;

public class URIUtils {
    /**
     * Return URI parent
     *
     * @param uri source URI
     * @return URI parent
     */
    public static URI uriParent(URI uri) {
        return uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
    }

    /**
     * Convert an URI without a scheme to a file scheme.
     *
     * @param uri the source URI
     * @return the converted URI
     */
    public static URI convertUriWithoutSchemeToFileScheme(URI uri) {
        if (uri.getScheme() == null) {
            return Paths.get(uri.getPath()).toUri();
        }
        return uri;
    }

    /**
     * Creates a URI from a String representation of a URI or a Path.
     *
     * @param input String representation of a URI or a Path.
     * @return the URI
     */
    public static URI create(String input) {
        if (isFile(input) || isURL(input)) {
            return URI.create(input);
        } else {
            return Paths.get(input).toUri();
        }
    }

    /**
     * Check if the input is a String representation of a file URI.
     *
     * @param input the String
     * @return true if the input is a String representation of a file URI.
     */
    private static boolean isFile(String input) {
        return input.toLowerCase().startsWith("file");
    }

    /**
     * Check if the input is a String representation of a URL.
     *
     * @param input the String
     * @return true if the input is a String representation of a URL.
     */
    private static boolean isURL(String input) {
        return input.toLowerCase().startsWith("http");
    }
}
