package com.silence.log2metric.processor.task;

import com.silence.log2metric.manager.TaskResult;
import com.silence.log2metric.processor.AbstractTask;
import com.silence.log2metric.processor.ITaskRunner;
import com.silence.log2metric.processor.runner.AggregationTaskRunner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class AggregationTask extends AbstractTask {
    private MetricConfig metric = new MetricConfig();
    private WindowConfig window = new WindowConfig();
    private KafkaConfig kafka = new KafkaConfig();

    @Override
    protected ITaskRunner createTaskRunner() {
        log.info("create task runner");
        return new AggregationTaskRunner(this);
    }

    @Override
    public TaskResult run() {
        return getTaskRunner().run();
    }

    @Override
    public void stop() {
        getTaskRunner().stop();
    }

    @Data
    public static class MetricConfig {
        private String name = "default";
        private List<String> labels = new ArrayList<>();
    }

    @Data
    public static class WindowConfig {
        private Duration windowSize = Duration.ofSeconds(60);
        private Duration gracePeriod = Duration.ofSeconds(10);
    }

    @Data
    public static class KafkaConfig {
        private String topic = "log";
        private String getBootstrapServers = "silence:9092";
    }

}
