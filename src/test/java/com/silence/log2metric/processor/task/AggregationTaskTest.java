package com.silence.log2metric.processor.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.silence.log2metric.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class AggregationTaskTest {
    @Test
    public void marshal() throws JsonProcessingException {
        AggregationTask aggregationTask = new AggregationTask();

        System.out.println(JsonUtils.marshal(aggregationTask));
    }

    @Test
    public void unmarshal() {

    }
}