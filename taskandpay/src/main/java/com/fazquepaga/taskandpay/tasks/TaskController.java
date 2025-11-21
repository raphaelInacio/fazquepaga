package com.fazquepaga.taskandpay.tasks;

import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {

        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestParam("child_id") String childId, @RequestBody CreateTaskRequest request)
            throws ExecutionException, InterruptedException {

        Task createdTask = taskService.createTask(childId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(@RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {

        List<Task> tasks = taskService.getTasksByUserId(childId);

        return ResponseEntity.ok(tasks);
    }
}
