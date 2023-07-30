package com.silence.log2metric.processor;

import lombok.Getter;

public abstract class AbstractTaskRunner<T extends ITask> implements ITaskRunner {
    @Getter
    private T task;

    protected AbstractTaskRunner(T task) {
        this.task = task;
    }
}
