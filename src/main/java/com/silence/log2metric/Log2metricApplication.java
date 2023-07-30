package com.silence.log2metric;

import com.silence.log2metric.config.WorkerConfig;
import com.silence.log2metric.manager.ITaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Log2metricApplication implements ApplicationRunner, DisposableBean {
    private final WorkerConfig workConfig;
    private final ITaskManager taskManager;

    @Autowired
    public Log2metricApplication(WorkerConfig workConfig, ITaskManager taskManager) {
        this.workConfig = workConfig;
        this.taskManager = taskManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("service ready to start");
        log.info("worker config: {}", workConfig);
        taskManager.start();
        log.info("service started");
    }

    @Override
    public void destroy() throws Exception {
        log.info("service ready to exit");
        taskManager.stop();
        log.info("service exited");
    }

    public static void main(String[] args) {
        SpringApplication.run(Log2metricApplication.class, args);
    }
}
