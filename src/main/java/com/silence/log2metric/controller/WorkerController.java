package com.silence.log2metric.controller;

import com.silence.log2metric.domain.response.Response;
import com.silence.log2metric.manager.ITaskManager;
import com.silence.log2metric.processor.ITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = {"/worker/v1"})
public class WorkerController {
    private final ITaskManager taskManager;

    @Autowired
    public WorkerController(ITaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @PostMapping(value = {"/tasks"})
    public Response<?> taskPost(@RequestBody ITask task) {
        taskManager.run(task);
        return Response.ok(null);
    }

    @GetMapping(value = {"/tasks"})
    public Response<?> taskPost() {
        return Response.ok(taskManager.getTasks());
    }

    @PostMapping(value = {"/tasks/{id}/shutdown"})
    public Response<?> taskPost(@PathVariable(name = "id") String id) {
        taskManager.shutdown(id);
        return Response.ok(null);
    }
}
