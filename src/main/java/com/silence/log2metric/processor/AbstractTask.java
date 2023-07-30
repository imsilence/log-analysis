package com.silence.log2metric.processor;

import com.google.common.base.Suppliers;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public abstract class AbstractTask implements ITask {
    @Getter
    @Setter
    private String id;
    private final Supplier<ITaskRunner> taskRunnerSupplier = Suppliers.memoize(this::createTaskRunner);


    protected abstract ITaskRunner createTaskRunner();

    protected ITaskRunner getTaskRunner() {
        return taskRunnerSupplier.get();
    }
}
