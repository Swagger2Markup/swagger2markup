package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Title;

public class TitleImpl implements Title {

    private final String main;
    private final String subtitle;

    public TitleImpl(String main, String subtitle) {
        this.main = main;
        this.subtitle = subtitle;
    }

    @Override
    public String getMain() {
        return main;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    public String getCombined() {
        return main + ": " + subtitle;
    }

    @Override
    public boolean isSanitized() {
        return false;
    }
}
