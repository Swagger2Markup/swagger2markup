package io.github.swagger2markup.markup.builder;

import java.util.HashMap;
import java.util.Map;

public class MarkupTableColumn {
    public String header;
    public boolean headerColumn = false;
    public Integer widthRatio = 0;
    public Map<MarkupLanguage, String> markupSpecifiers = new HashMap<>();

    /**
     * Empty constructor
     */
    public MarkupTableColumn() {
    }

    /**
     * Header constructor.
     *
     * @param header header name
     */
    public MarkupTableColumn(String header) {
        this.header = header;
    }

    /**
     * Header and specifiers constructor.
     *
     * @param header header
     * @param headerColumn set column as an header column.
     * @param widthRatio width ratio
     */
    public MarkupTableColumn(String header, boolean headerColumn, Integer widthRatio) {
        this.header = header;
        this.headerColumn = headerColumn;
        this.widthRatio = widthRatio;
    }

    /**
     * Set header name for this column.
     *
     * @param header header name
     * @return this builder
     */
    public MarkupTableColumn withHeader(String header) {
        this.header = header;
        return this;
    }

    /**
     * Set column as an header column.<br>
     * Limited support : Markdown does not support header column and will ignore it.
     *
     * @param headerColumn configuration value
     * @return this builder
     */
    public MarkupTableColumn withHeaderColumn(boolean headerColumn) {
        this.headerColumn = headerColumn;
        return this;
    }

    /**
     * Set column width ratio for this column.<br>
     * Limited support : Markdown does not support column width specifiers and will ignore {@code widthRatio}.
     *
     * @param widthRatio width ratio integer value [0-100]. Accept relative width specifiers (e.g.: 1, 2, 3, .. with sum of width ratios for all columns != 100).
     * @return this builder
     */
    public MarkupTableColumn withWidthRatio(Integer widthRatio) {
        this.widthRatio = widthRatio;
        return this;
    }

    /**
     * Overrides all other specifiers for the specified {@code language} with this language-dependent {@code specifiers} string.<br>
     * This method should be used as a last resort.
     *
     * @param language apply the {@code specifiers} to this language only
     * @param specifiers RAW language-dependent specifiers string for the column
     * @return this builder
     */
    public MarkupTableColumn withMarkupSpecifiers(MarkupLanguage language, String specifiers) {
        this.markupSpecifiers.put(language, specifiers);
        return this;
    }
}