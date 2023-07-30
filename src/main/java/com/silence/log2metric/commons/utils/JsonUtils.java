package com.silence.log2metric.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T unmarshal(String txt, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(txt, clazz);
    }

    public static <T> T unmarshal(String txt, TypeReference<T> type) throws JsonProcessingException {
        return mapper.readValue(txt, type);
    }

    public static <T> String marshal(T value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }
}
