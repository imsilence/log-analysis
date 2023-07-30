package com.silence.log2metric.processor.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class JsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectReader reader;

    public JsonDeserializer(JavaType javaType) {
        reader = objectMapper.readerFor(javaType);
    }

    public JsonDeserializer(Class<?> clazz) {
        reader = objectMapper.readerFor(clazz);
    }

    public JsonDeserializer(TypeReference<?> typeReference) {
        reader = objectMapper.readerFor(typeReference);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return reader.readValue(data);
        } catch (IOException e) {
            log.error("json deserialize", e);
            throw new ClassCastException("json deserialize");
        }
    }
}
