package com.silence.log2metric.domain.response;

import lombok.Data;

@Data
public class Response<T> {
    private final long ts;
    private final Code code;
    private final String msg;
    private final T result;

    private Response(Code code, String msg, T result) {
        ts = System.currentTimeMillis();
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public static <T> Response<T> response(Code code, String msg, T result) {
        return new Response<>(code, msg, result);
    }

    public static <T> Response<T> ok(T result) {
        return response(Code.SUCCESS, "", result);
    }

    public static <T> Response<T> error(T result) {
        return response(Code.ERROR, "", result);
    }
}
