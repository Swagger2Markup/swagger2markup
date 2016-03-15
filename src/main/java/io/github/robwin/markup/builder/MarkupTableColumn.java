package io.github.robwin.markup.builder;

import java.util.HashMap;
import java.util.Map;

public class MarkupTableColumn {
    public String header;
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
     * @param header header name
     * @param widthRatio width ratio
     */
    public MarkupTableColumn(String header, Integer widthRatio) {
        this.header = header;
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
     * Set column width ratio for this column.<br/>
     * Limited support : Markdown does not support column width specifiers and will ignore {@code widthRatio}.
     *
     * @param widthRatio width ratio integer value [0-100]. Accept relative width specifiers (e.g.: 1, 2, 3, .. with Sum{i=0->nbCols}(widthRatio(i)) != 100).
     * @return this builder
     */
    public MarkupTableColumn withWidthRatio(Integer widthRatio) {
        this.widthRatio = widthRatio;
        return this;
    }

    /**
     * Overrides all other specifiers (for the specified language) with this language-dependent {@code specifiers} string.
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