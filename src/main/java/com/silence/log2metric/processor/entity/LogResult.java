package com.silence.log2metric.processor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.streams.kstream.Windowed;

@Data
@NoArgsConstructor
public class LogResult<T> {
    private String name;
    private Labels labels;
    private Window window;
    private T result;

    public LogResult(String name, Windowed<Labels> windowed, T value) {
        this.name = name;
        this.labels = windowed.key();
        this.window = new Window(windowed.window().start(), windowed.window().end());
        this.result = value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Window {
        private long startMs;
        private long endMs;
    }
}
