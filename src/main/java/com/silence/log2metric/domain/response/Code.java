package com.silence.log2metric.domain.response;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Code {
    SUCCESS(200),
    ERROR(400);

    @JsonValue
    private final int value;

    Code(int value) {
        this.value = value;
    }
}
