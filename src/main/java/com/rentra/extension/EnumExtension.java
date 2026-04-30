package com.rentra.extension;

public final class EnumExtension {
    private EnumExtension() {}

    public static String toName(Enum<?> value) {
        return value != null ? value.name() : null;
    }
}
