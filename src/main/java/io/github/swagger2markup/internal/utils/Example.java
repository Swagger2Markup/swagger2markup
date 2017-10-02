package io.github.swagger2markup.internal.utils;

import io.github.swagger2markup.ExampleType;

/**
 * swagger2markup (c) Duco Hosting
 * Created by cas on 02-Oct-17.
 */
public class Example {
    private ExampleType type;
    private Object example;

    public Example(ExampleType type, Object example) {
        this.type = type;
        this.example = example;
    }

    public ExampleType getType() {
        return type;
    }

    public Object getExample() {
        return example;
    }
}
