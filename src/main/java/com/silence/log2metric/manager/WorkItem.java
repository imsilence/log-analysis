package com.silence.log2metric.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.ListenableFuture;
import com.silence.log2metric.processor.ITask;
import lombok.Getter;

@Getter
public class WorkItem {
    private final ITask task;
    private volatile State state;
    @JsonIgnore
    private final ListenableFuture<TaskResult> result;

    @JsonIgnore
    private ListenableFuture<Void> shutdownFuture;

    private final long createdAt;
    private volatile long runningAt;
    private volatile long shutdownAt;
    private volatile long finishedAt;

    WorkItem(ITask task, ListenableFuture<TaskResult> result) {
        this.task = task;
        this.result = result;
        state = State.PENDING;
        createdAt = System.currentTimeMillis();
    }

    public void setRunning() {
        state = State.RUNNING;
        runningAt = System.currentTimeMillis();
    }

    public boolean isShutdown() {
        return state.equals(State.SHUTDOWN);
    }

    public void setShutdown() {
        state = State.SHUTDOWN;
        shutdownAt = System.currentTimeMillis();
    }

    public void setShutdownFuture(ListenableFuture<Void> shutdownFuture) {
        this.shutdownFuture = shutdownFuture;
    }

    public void cancelShutdownFuture(State state) {
        this.state = state;
        shutdownFuture = null;
        shutdownAt = 0;
    }

    public void setFinished() {
        state = State.FINISHED;
        finishedAt = System.currentTimeMillis();
    }

    public enum State {
        RUNNING,
        PENDING,
        SHUTDOWN,
        FINISHED,
    }
}
