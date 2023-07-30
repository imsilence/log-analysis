package com.silence.log2metric.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "worker", ignoreInvalidFields = true)
public class WorkerConfig {
    private int capacity = 3;
    private Duration stopWaitTime = Duration.ofSeconds(3);
    private Task taskConfig = new Task();

    @Data
    public static class Task {
        private Duration shutdownWaitTime = Duration.ofSeconds(3);
    }
}
