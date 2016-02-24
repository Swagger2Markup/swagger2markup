package io.github.robwin.swagger2markup.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class FileUtils {
    private static final Pattern FILENAME_FORBIDDEN_PATTERN = Pattern.compile("[^0-9A-Za-z-_]+");

    /**
     * Create a normalized filename from an arbitrary string.<br/>
     * Paths separators are replaced, so this function can't be applied on a whole path, but must be called on each path sections.
     *
     * @param name current name of the file
     * @return a normalized filename
     */
    public static String normalizeFileName(String name) {
        String fileName = FILENAME_FORBIDDEN_PATTERN.matcher(name).replaceAll("_");
        fileName = fileName.replaceAll(String.format("([%1$s])([%1$s]+)", "-_"), "$1");
        fileName = StringUtils.strip(fileName, "_-");
        fileName = fileName.trim().toLowerCase();
        return fileName;
    }
}
