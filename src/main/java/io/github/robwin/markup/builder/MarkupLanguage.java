package io.github.robwin.markup.builder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert Winkler
 */
public enum MarkupLanguage {
    ASCIIDOC(".adoc,.asciidoc"),
    MARKDOWN(".md,.markdown");

    private final String fileNameExtensions;

    /**
     * @param fileNameExtensions file name suffix
     */
    private MarkupLanguage(final String fileNameExtensions) {
        this.fileNameExtensions = fileNameExtensions;
    }

    public List<String> getFileNameExtensions() {
        return Arrays.asList(fileNameExtensions.split(","));
    }
}
