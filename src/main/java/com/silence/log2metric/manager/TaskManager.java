package com.silence.log2metric.manager;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.silence.log2metric.commons.utils.StringUtils;
import com.silence.log2metric.commons.utils.ThreadUtils;
import com.silence.log2metric.config.WorkerConfig;
import com.silence.log2metric.processor.ITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class TaskManager implements ITaskManager {
    private final WorkerConfig workerConfig;

    private final ConcurrentHashMap<String, WorkItem> tasks = new ConcurrentHashMap<>();
    private final ListeningExecutorService taskExecutor;
    private final ListeningExecutorService controlExecutor;
    private final ExecutorService resultExecutor = Executors.newSingleThreadExecutor(ThreadUtils.makeThreadFactory("task-manager-result"));


    @Autowired
    public TaskManager(WorkerConfig workerConfig) {
        this.workerConfig = workerConfig;
        taskExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(workerConfig.getCapacity(), ThreadUtils.makeThreadFactory("task-manager-executor-%d")));
        controlExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(workerConfig.getCapacity(), ThreadUtils.makeThreadFactory("task-manager-control-%d")));
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        log.info("task manager ready to stop");
        resultExecutor.shutdown();
        taskExecutor.shutdown();
        List<String> ids = new ArrayList<>(tasks.size());
        List<ListenableFuture<Void>> shutdownFutures = new ArrayList<>(tasks.size());
        synchronized (tasks) {
            for (Map.Entry<String, WorkItem> entry : tasks.entrySet()) {
                ids.add(entry.getKey());
                shutdownFutures.add(handleShutdown(entry.getValue()));
            }
        }
        controlExecutor.shutdown();
        ListenableFuture<List<Void>> shutdownFuture = Futures.successfulAsList(shutdownFutures);
        try {
            shutdownFuture.get();
        } catch (Exception e) {
            log.error("waiting for all task stop exception, ", e);
        }

        try {
            long waitTime = workerConfig.getStopWaitTime().toMillis();
            boolean terminated = controlExecutor.awaitTermination(waitTime, TimeUnit.MILLISECONDS);
            terminated = resultExecutor.awaitTermination(waitTime, TimeUnit.MILLISECONDS) && terminated;
            if (terminated) {
                log.info("successful stop all tasks, ids: {}", ids);
            } else {
                log.warn("failing stop all tasks, there are some tasks still running, waiting time: {}, ids: {}", waitTime, ids);
            }
        } catch (InterruptedException e) {
            log.error("an interrupt exception occurred while waiting for stop all tasks", e);
        }
        log.info("task manager stopped");
    }

    @Override
    public void run(ITask task) {
        synchronized (tasks) {
            tasks.computeIfAbsent(task.getId(), key -> new WorkItem(task, taskExecutor.submit(() -> {
                final WorkItem workItem;
                synchronized (tasks) {
                    workItem = tasks.get(task.getId());
                    if (Objects.isNull(workItem)) {
                        return TaskResult.success(task.getId());
                    }
                    if (workItem.isShutdown()) {
                        return TaskResult.success(task.getId());
                    }
                }
                final String threadName = Thread.currentThread().getName();
                Thread.currentThread()
                        .setName(StringUtils.format("[%s]-%s", task.getId(), threadName));
                TaskResult taskStatus;
                try {
                    workItem.setRunning();
                    taskStatus = task.run();
                } catch (Throwable throwable) {
                    return TaskResult.failure(task.getId(), throwable.getMessage());
                } finally {
                    workItem.setFinished();
                    Thread.currentThread().setName(threadName);
                    // 移除任务(FINISHED状态)
                    removeTask(task);
                }
                return Objects.isNull(taskStatus) ? TaskResult.failure(task.getId(), "unknown") : taskStatus;
            })));
        }
        Futures.addCallback(tasks.get(task.getId()).getResult(), new FutureCallback<>() {
            @Override
            public void onSuccess(TaskResult result) {
                log.info("task[{}] success completed: {}", task.getId(), result);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error(String.format("task[%s] failure", task.getId()), t);
            }
        }, resultExecutor);
    }

    private void removeTask(ITask task) {
        synchronized (tasks) {
            tasks.remove(task.getId());
        }
    }

    @Override
    public void shutdown(String taskId) {
        synchronized (tasks) {
            WorkItem workItem = tasks.get(taskId);
            if (Objects.isNull(workItem)) {
                log.info("ignore task shutdown, task[{}] not found", taskId);
                return;
            }
            if (workItem.isShutdown()) {
                log.info("ignore task shutdown, task[{}] already shutting down at [{}]", taskId, workItem.getShutdownAt());
                return;
            }
            workItem.setShutdown();
            handleShutdown(workItem);
        }
    }

    @Override
    public List<WorkItem> getTasks() {
        synchronized (tasks) {
            return tasks.values().stream().toList();
        }
    }

    private ListenableFuture<Void> handleShutdown(WorkItem workItem) {
        synchronized (tasks) {
            if (Objects.nonNull(workItem.getShutdownFuture())) {
                return workItem.getShutdownFuture();
            }
            workItem.setShutdownFuture(controlExecutor.submit(() -> {
                log.info("trigger task[{}] to shutdown", workItem.getTask().getId());
                WorkItem.State state = workItem.getState();
                workItem.getTask().stop();
                long waitTime = workerConfig.getTaskConfig().getShutdownWaitTime().toMillis();
                try {
                    workItem.getResult().get(waitTime, TimeUnit.MILLISECONDS);
                    // 移除任务(PENDING状态)
                    removeTask(workItem.getTask());
                    log.info("task[{}] shutdown", workItem.getTask().getId());
                } catch (TimeoutException e) {
                    workItem.getResult().cancel(true);
                    // 移除任务(PENDING状态)
                    removeTask(workItem.getTask());
                    log.info("waiting for task[{}] shutdown timeout[{}], forceful shutdown", workItem.getTask().getId(), waitTime);
                } catch (Exception e) {
                    log.error(StringUtils.format("an interrupt exception occurred while waiting for shutdown task[%s]", workItem.getTask().getId()), e);
                    if (Objects.nonNull(workItem.getShutdownFuture())) {
                        workItem.getShutdownFuture().cancel(true);
                        workItem.cancelShutdownFuture(state);
                    }
                }
                return null;
            }));
        }
        return workItem.getShutdownFuture();
    }
}
