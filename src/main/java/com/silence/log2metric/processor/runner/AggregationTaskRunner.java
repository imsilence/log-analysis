package com.silence.log2metric.processor.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.silence.log2metric.commons.utils.StringUtils;
import com.silence.log2metric.manager.TaskResult;
import com.silence.log2metric.processor.AbstractTaskRunner;
import com.silence.log2metric.processor.entity.Labels;
import com.silence.log2metric.processor.entity.Log;
import com.silence.log2metric.processor.entity.LogResult;
import com.silence.log2metric.processor.extractor.LogTimestampExtractor;
import com.silence.log2metric.processor.serialization.JsonDeserializer;
import com.silence.log2metric.processor.serialization.JsonSerializer;
import com.silence.log2metric.processor.task.AggregationTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AggregationTaskRunner extends AbstractTaskRunner<AggregationTask> {
    protected final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final Lock runningLock = new ReentrantLock();
    private final Condition stopSignal = runningLock.newCondition();

    public AggregationTaskRunner(AggregationTask task) {
        super(task);
    }

    @Override
    public TaskResult run() {
        log.info("task[{}] running...", getTask().getId());
        log.info("task[{}] spec: {}", getTask().getId(), getTask());
        try {
            return runInternal();
        } catch (Exception e) {
            log.error(StringUtils.format("an interrupt exception occurred while run task[%s]", getTask().getId()), e);
            return TaskResult.failure(getTask().getId(), e.getMessage());
        }
    }

    private Properties newKafkaStreamProperties() {
        String appId = StringUtils.format("streams-app-%s", getTask().getId());
        String clientId = StringUtils.format("streams-client-%s", getTask().getId());
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getTask().getKafka().getGetBootstrapServers());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId);
        Serde<String> stringSerde = Serdes.String();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, LogTimestampExtractor.class);
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000L);
        return props;
    }

    private KafkaStreams newKafkaStream() {
        String appId = getTask().getId();
        String topic = getTask().getKafka().getTopic();
        String name = getTask().getMetric().getName();
        List<String> labelNames = ImmutableList.<String>builder().addAll(getTask().getMetric().getLabels()).build();
        Duration windowSize = getTask().getWindow().getWindowSize();
        Duration gracePeriod = getTask().getWindow().getGracePeriod();

        Serde<String> stringSerde = Serdes.String();
        Serde<Long> longSerde = Serdes.Long();
        Serde<Log> logSerde = Serdes.serdeFrom(new JsonSerializer<>(Log.class), new JsonDeserializer<>(Log.class));
        Serde<Labels> keysSerde = Serdes.serdeFrom(new JsonSerializer<>(Labels.class), new JsonDeserializer<>(Labels.class));
        Serde<LogResult<Long>> logResultSerde = Serdes.serdeFrom(new JsonSerializer<>(new TypeReference<LogResult<Long>>() {
        }), new JsonDeserializer<>(new TypeReference<LogResult<Long>>() {
        }));
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Log> source = builder.stream(topic, Consumed.with(stringSerde, logSerde));
        source.map(new KeyValueMapper<String, Log, KeyValue<Labels, Long>>() {
                    @Override
                    public KeyValue<Labels, Long> apply(String key, Log value) {
                        return new KeyValue<>(new Labels(labelNames, value), 1L);
                    }
                }).groupByKey(Grouped.with(keysSerde, longSerde))
                .windowedBy(TimeWindows.ofSizeAndGrace(windowSize, gracePeriod).advanceBy(windowSize))
                .count(Materialized.with(keysSerde, longSerde))
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                .toStream().map(new KeyValueMapper<Windowed<Labels>, Long, KeyValue<String, LogResult<Long>>>() {
                    @Override
                    public KeyValue<String, LogResult<Long>> apply(Windowed<Labels> windowed, Long value) {
                        return new KeyValue<>("", new LogResult<>(name, windowed, value));
                    }
                })
                .peek(new ForeachAction<String, LogResult<Long>>() {
                    @Override
                    public void apply(String key, LogResult<Long> value) {
                        log.debug("result: {}", value);
                    }
                })
                .to(StringUtils.format("%s-result-%s", topic, appId), Produced.with(stringSerde, logResultSerde));
        Topology topology = builder.build();
        log.info("task[{}] build topology: {}", getTask().getId(), topology.describe());
        return new KafkaStreams(topology, newKafkaStreamProperties());
    }

    private TaskResult runInternal() {
        try (KafkaStreams streams = newKafkaStream()) {
            streams.start();
            // 等待结束
            while (isRunning.get()) {
                try {
                    runningLock.lockInterruptibly();
                    log.info("waiting stop signal");
                    stopSignal.await();
                    log.info("waited stop signal");
                } catch (InterruptedException e) {
                    log.error(StringUtils.format("an interrupt exception occurred waiting for running task[%s] lock or signal", getTask().getId()), e);
                } finally {
                    runningLock.unlock();
                }
            }
        }
        return TaskResult.success(getTask().getId());
    }

    @Override
    public void stop() {
        log.info("task[{}] stopping...", getTask().getId());
        isRunning.set(false);
        try {
            runningLock.lockInterruptibly();
            stopSignal.signalAll();
            log.info("signal task[{}] to stop", getTask().getId());
        } catch (InterruptedException e) {
            log.error(StringUtils.format("an interrupt exception occurred signal task[{}] to stop", getTask().getId()), e);
        } finally {
            runningLock.unlock();
        }
    }
}
