package com.silence.log2metric.processor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Log {
    private long ts;
    private String ip;
    private String name;
    private long used;
    private float elapsed;

    public String get(String key) {
        switch (key) {
            case "ts":
                return String.valueOf(ts);
            case "ip":
                return ip;
            case "name":
                return name;
            case "used":
                return String.valueOf(used);
            case "elapsed":
                return String.valueOf(elapsed);
            default:
                return "";
        }
    }
}
