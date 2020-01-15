package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Cursor;

public class CursorImpl implements Cursor {

    private int lineno;

    public CursorImpl() {
    }

    @Override
    public int getLineNumber() {
        return lineno;
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String getDir() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String getFile() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }
}
