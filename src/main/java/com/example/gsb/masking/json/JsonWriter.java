package com.example.gsb.masking.json;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/** 轻量 JSON 序列化器，支持 Map / Collection / 数组 / 字符串 / 数字 / 布尔 / null。 */
public final class JsonWriter {

    private JsonWriter() {
    }

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        append(value, out);
        return out.toString();
    }

    private static void append(Object value, StringBuilder out) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String || value instanceof Character || value instanceof Enum<?>) {
            appendQuoted(String.valueOf(value), out);
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                appendQuoted(String.valueOf(entry.getKey()), out);
                out.append(':');
                append(entry.getValue(), out);
            }
            out.append('}');
        } else if (value instanceof Collection<?> collection) {
            out.append('[');
            boolean first = true;
            for (Object element : collection) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                append(element, out);
            }
            out.append(']');
        } else if (value.getClass().isArray()) {
            out.append('[');
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    out.append(',');
                }
                append(Array.get(value, i), out);
            }
            out.append(']');
        } else {
            appendQuoted(String.valueOf(value), out);
        }
    }

    private static void appendQuoted(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
