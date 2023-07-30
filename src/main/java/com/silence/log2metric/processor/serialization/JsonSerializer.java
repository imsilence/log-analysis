package com.silence.log2metric.processor.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class JsonSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectWriter writer;

    public JsonSerializer(JavaType javaType) {
        writer = objectMapper.writerFor(javaType);
    }

    public JsonSerializer(Class<?> clazz) {
        writer = objectMapper.writerFor(clazz);
    }

    public JsonSerializer(TypeReference<?> typeReference) {
        writer = objectMapper.writerFor(typeReference);
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return writer.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            log.error("json serialize", e);
            throw new ClassCastException("json serialize");
        }
    }
}