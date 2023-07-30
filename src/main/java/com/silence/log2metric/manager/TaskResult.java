package com.silence.log2metric.manager;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class TaskResult {
    private final String taskId;
    private final State state;
    private final String msg;

    public static TaskResult success(String taskId) {
        return new TaskResult(taskId, State.SUCCESS, null);
    }

    public static TaskResult failure(String taskId, String msg) {
        return new TaskResult(taskId, State.FAILURE, msg);
    }

    public enum State {
        FAILURE,
        SUCCESS;
    }

}
