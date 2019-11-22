package io.github.swagger2markup.adoc.converter.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static io.github.swagger2markup.adoc.converter.internal.Delimiters.*;

abstract class NodeAttributes {
    public static final String TITLE = "title";

    final Map<String, Object> attributes;
    List<String> attrs = new ArrayList<>();

    NodeAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String pop(String... keys) {
        AtomicReference<String> value = new AtomicReference<>("");
        Stream.of(keys).forEach(key -> {
            try {
                String tmpValue = attributes.remove(key).toString();
                if (null != tmpValue && !tmpValue.isEmpty() && value.get().isEmpty()) {
                    value.set(tmpValue);
                }
            } catch (NullPointerException ignored) {
            }
        });
        return value.get();
    }

    String pop(String key) {
        try {
            String value = attributes.remove(key).toString();
            if (null == value) {
                value = "";
            }
            return value;
        } catch (NullPointerException ignored) {
            return "";
        }
    }

    final public String toAsciiDocContent() {
        processPositionalAttributes();
        processAttributes();
        return processAsciiDocContent();
    }

    abstract void processPositionalAttributes();

    abstract void processAttributes();

    String processAsciiDocContent() {
        StringBuilder sb = new StringBuilder();
        if (!attrs.isEmpty()) {
            sb.append(ATTRIBUTES_BEGIN).append(String.join(",", attrs)).append(ATTRIBUTES_END).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }
}
