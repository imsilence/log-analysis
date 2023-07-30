package com.silence.log2metric.processor.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.silence.log2metric.commons.utils.JsonUtils;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LogResultTest {
    @Test
    public void marshal() throws JsonProcessingException {
        List<String> labelNames = ImmutableList.<String>builder().add("ip").build();
        Log log = new Log();
        log.setIp("1.1.1.1");

        LogResult<Long> result = new LogResult<Long>("log_count", new Windowed<>(new Labels(labelNames, log), new TimeWindow(1690707086629L, 1690707186629L)), 1L);
        System.out.println(JsonUtils.marshal(result));
        System.out.println(JsonUtils.unmarshal(JsonUtils.marshal(result), new TypeReference<LogResult<Long>>() {
        }));
    }
}