package com.silence.log2metric.time;

import java.time.Duration;

public class DurationTest {
    public static void main(String[] args) {
        Duration duration = Duration.parse("P2DT3H4M");
        System.out.println(duration.getSeconds());
        System.out.println(duration.toSeconds());
        System.out.println(duration.toMinutes());
        System.out.println(duration.toMillis());
    }
}
