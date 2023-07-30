package com.silence.log2metric.processor;

import com.silence.log2metric.manager.TaskResult;

public interface ITaskRunner {
    TaskResult run();

    void stop();
}
