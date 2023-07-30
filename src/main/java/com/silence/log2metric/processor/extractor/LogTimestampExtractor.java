package com.silence.log2metric.processor.extractor;

import com.silence.log2metric.processor.entity.Log;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class LogTimestampExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        Object value = record.value();
        if (!(value instanceof Log)) {
            return -1;
        }
        return ((Log) value).getTs();
    }
}
