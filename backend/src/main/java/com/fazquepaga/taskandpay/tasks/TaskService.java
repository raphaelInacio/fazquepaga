package com.fazquepaga.taskandpay.tasks;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.subscription.SubscriptionLimitReachedException;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jakarta.inject.Provider;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final SubscriptionService subscriptionService;

    private final com.fazquepaga.taskandpay.allowance.LedgerService ledgerService;
    private final Provider<com.fazquepaga.taskandpay.allowance.AllowanceService> allowanceServiceProvider;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
            SubscriptionService subscriptionService,
            com.fazquepaga.taskandpay.allowance.LedgerService ledgerService,
            Provider<com.fazquepaga.taskandpay.allowance.AllowanceService> allowanceServiceProvider) {

        this.taskRepository = taskRepository;

        this.userRepository = userRepository;

        this.subscriptionService = subscriptionService;
        this.ledgerService = ledgerService;
        this.allowanceServiceProvider = allowanceServiceProvider;
    }

    public Task createTask(String userId, CreateTaskRequest request)
            throws ExecutionException, InterruptedException {

        User child = userRepository.findByIdSync(userId);

        if (child == null || child.getRole() != User.Role.CHILD) {

            throw new IllegalArgumentException("Child with ID " + userId + " not found.");
        }

        // Get parent to check subscription limits
        User parent = userRepository.findByIdSync(child.getParentId());
        if (parent == null) {
            throw new IllegalArgumentException("Parent not found for child " + userId);
        }

        // Check subscription limits for recurring tasks
        if (request.getType() == Task.TaskType.DAILY || request.getType() == Task.TaskType.WEEKLY) {
            int currentRecurringTaskCount = countRecurringTasks(userId);
            if (!subscriptionService.canCreateTask(parent, currentRecurringTaskCount)) {
                throw new SubscriptionLimitReachedException(
                        "Recurring task limit reached for Free tier. Upgrade to Premium for unlimited tasks.");
            }
        }

        Task task = Task.builder()
                .description(request.getDescription())
                .type(request.getType())
                .weight(request.getWeight())
                .value(request.getValue() != null ? request.getValue() : java.math.BigDecimal.ZERO)
                .requiresProof(request.isRequiresProof())
                .createdAt(Instant.now())
                .dayOfWeek(request.getDayOfWeek())
                .scheduledDate(request.getScheduledDate())
                .status(Task.TaskStatus.PENDING)
                .build();

        taskRepository.save(userId, task).get();

        return task;
    }

    public List<Task> getTasksByUserId(String userId)
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documents = taskRepository.findTasksByUserId(userId).get().getDocuments();

        return documents.stream().map(doc -> doc.toObject(Task.class)).collect(Collectors.toList());
    }

    public Task approveTask(String taskId, String parentId) throws ExecutionException, InterruptedException {
        // 1. Fetch task
        // Ideally we should have a findById in TaskRepository or search by ID across
        // all users (inefficient)
        // or we need the childId to find the task efficiently if it's stored under
        // child's collection.
        // Assuming for now we might need to iterate or we change the API to require
        // childId.
        // However, the requirement says `POST /api/v1/tasks/{taskId}/approve`.
        // If TaskRepository is structured by userId, we have a problem finding a task
        // just by ID without userId.
        // Let's check TaskRepository.
        // It seems `save` takes `userId`. `findTasksByUserId` takes `userId`.
        // Firestore structure is likely /users/{userId}/tasks/{taskId}.
        // So we NEED the childId (userId) to find the task efficiently.
        // OR we can use a collection group query if we want to find by ID globally.
        // For this MVP, let's assume the frontend sends the childId or we find it.
        // BUT, the requirement 8.1 says: `POST /api/v1/tasks/{taskId}/approve`.
        // It doesn't mention childId.
        // Let's stick to the requirement but we might need to do a Collection Group
        // query or
        // ask the user to pass childId.
        // Given the current repository structure, let's assume we can't easily find it
        // without childId.
        // I will add `childId` as a request param to the controller for efficiency, or
        // I'll implement a global search.
        // Let's implement a global search in TaskRepository or just ask for childId.
        // Adding `childId` to the API is cleaner for Firestore.
        // Let's assume the controller will pass `childId`.

        // Wait, I can't change the controller signature easily if I want to stick to
        // REST standard of /tasks/{id}.
        // But if the resource is nested, it should be /users/{id}/tasks/{id}.
        // The current API is /api/v1/tasks?child_id=...
        // So /api/v1/tasks/{taskId}/approve?child_id=... is acceptable.

        // Let's implement `approveTask(String childId, String taskId, String parentId)`
        return null; // Placeholder to be replaced by actual implementation in next step
    }

    public Task approveTask(String childId, String taskId, String parentId)
            throws ExecutionException, InterruptedException {
        User parent = userRepository.findByIdSync(parentId);
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("User is not a parent");
        }

        // Verify child belongs to parent
        User child = userRepository.findByIdSync(childId);
        if (child == null || !child.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Child not found or does not belong to this parent");
        }

        List<Task> tasks = getTasksByUserId(childId);
        Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == Task.TaskStatus.APPROVED) {
            throw new IllegalStateException("Task is already approved");
        }

        // Calculate value
        java.math.BigDecimal value = allowanceServiceProvider.get().calculateValueForTask(childId, taskId);

        // Update task status
        task.setStatus(Task.TaskStatus.APPROVED);
        taskRepository.save(childId, task).get();

        // Add transaction
        ledgerService.addTransaction(
                childId,
                value,
                "Task approved: " + task.getDescription(),
                com.fazquepaga.taskandpay.allowance.Transaction.TransactionType.CREDIT);

        return task;
    }

    private int countRecurringTasks(String userId) throws ExecutionException, InterruptedException {
        List<Task> tasks = getTasksByUserId(userId);
        return (int) tasks.stream()
                .filter(task -> task.getType() == Task.TaskType.DAILY || task.getType() == Task.TaskType.WEEKLY)
                .count();
    }
}
