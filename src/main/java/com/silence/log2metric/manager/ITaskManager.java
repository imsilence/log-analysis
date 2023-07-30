package com.silence.log2metric.manager;

import com.silence.log2metric.processor.ITask;

import java.util.List;

public interface ITaskManager {
    void start();

    void stop();

    void run(ITask task);

    void shutdown(String taskId);

    List<WorkItem> getTasks();
}
