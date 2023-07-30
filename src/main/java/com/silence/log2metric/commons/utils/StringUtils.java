package com.silence.log2metric.commons.utils;

import java.util.Locale;

public final class StringUtils {
    public static String format(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }
}
