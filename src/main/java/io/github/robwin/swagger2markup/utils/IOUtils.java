package io.github.robwin.swagger2markup.utils;

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
     * Create a normalized name from an arbitrary string.<br/>
     * Paths separators are replaced, so this function can't be applied on a whole path, but must be called on each path sections.
     *
     * @param name current name of the file
     * @return a normalized filename
     */
    public static String normalizeName(String name) {
        String fileName = NAME_FORBIDDEN_PATTERN.matcher(name).replaceAll("_");
        fileName = fileName.replaceAll(String.format("([%1$s])([%1$s]+)", "-_"), "$1");
        fileName = StringUtils.strip(fileName, "_-");
        fileName = fileName.trim().toLowerCase();
        return fileName;
    }

    /**
     * Create a reader from specified {@code source}.<br/>
     * Returned reader should be explicitly closed after use.
     *
     * @param uri source URI
     * @return reader
     * @throws IOException
     */
    public static Reader uriReader(URI uri) throws IOException {
        return new BufferedReader(new InputStreamReader(uri.toURL().openStream(), StandardCharsets.UTF_8));
    }

    /**
     * Return URI parent
     * @param uri source URI
     * @return URI parent
     */
    public static URI uriParent(URI uri) {
        return uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
    }
}
