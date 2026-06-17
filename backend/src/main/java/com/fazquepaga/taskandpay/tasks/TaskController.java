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
    private final com.fazquepaga.taskandpay.identity.IdentityService identityService;

    public TaskController(
            TaskService taskService,
            com.fazquepaga.taskandpay.identity.IdentityService identityService) {

        this.taskService = taskService;
        this.identityService = identityService;
    }

    private com.fazquepaga.taskandpay.identity.User getAuthenticatedUser() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.fazquepaga.taskandpay.identity.User) {
            return (com.fazquepaga.taskandpay.identity.User) auth.getPrincipal();
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestParam("child_id") String childId, @RequestBody CreateTaskRequest request)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate child belongs to parent
        identityService.getChild(childId, parent.getId());

        Task createdTask = taskService.createTask(childId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(@RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Security check: If parent, child must belong to them. If child, must be them.
        if (user.getRole() == com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            identityService.getChild(childId, user.getId());
        } else if (!user.getId().equals(childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Task> tasks = taskService.getTasksByUserId(childId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{taskId}/approve")
    public ResponseEntity<Task> approveTask(
            @PathVariable String taskId,
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate ownership
        identityService.getChild(childId, parent.getId());

        Task approvedTask = taskService.approveTask(childId, taskId, parent.getId());
        return ResponseEntity.ok(approvedTask);
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Task> completeTask(
            @PathVariable String taskId, @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Only the child can complete their own task
        if (user.getRole() != com.fazquepaga.taskandpay.identity.User.Role.CHILD || !user.getId().equals(childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Task completedTask = taskService.completeTask(taskId, childId);
        return ResponseEntity.ok(completedTask);
    }

    @PostMapping("/{taskId}/acknowledge")
    public ResponseEntity<Task> acknowledgeTask(
            @PathVariable String taskId,
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        identityService.getChild(childId, parent.getId());

        Task task = taskService.acknowledgeTask(childId, taskId, parent.getId());
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{taskId}/reject")
    public ResponseEntity<Task> rejectTask(
            @PathVariable String taskId,
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        identityService.getChild(childId, parent.getId());

        Task task = taskService.rejectTask(childId, taskId, parent.getId());
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        identityService.getChild(childId, parent.getId());

        taskService.deleteTask(childId, taskId, parent.getId());
        return ResponseEntity.noContent().build();
    }
}
