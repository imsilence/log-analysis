package com.silence.log2metric.commons.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public final class ThreadUtils {
    public static ThreadFactory makeThreadFactory(String nameFormat) {
        return makeThreadFactory(nameFormat, -1);
    }

    public static ThreadFactory makeThreadFactory(String nameFormat, int priority) {
        final ThreadFactoryBuilder builder = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(nameFormat);
        if (priority > 0) {
            builder.setPriority(priority);
        }

        return builder.build();
    }
}
