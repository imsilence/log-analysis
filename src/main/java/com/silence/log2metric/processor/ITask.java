package com.silence.log2metric.processor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.silence.log2metric.manager.TaskResult;
import com.silence.log2metric.processor.task.AggregationTask;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = AggregationTask.class)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = "aggregation", value = AggregationTask.class)
})
public interface ITask {
    String getId();

    TaskResult run();

    void stop();
}
