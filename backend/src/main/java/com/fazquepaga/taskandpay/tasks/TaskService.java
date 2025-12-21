package com.fazquepaga.taskandpay.tasks;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.subscription.SubscriptionLimitReachedException;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import jakarta.inject.Provider;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final SubscriptionService subscriptionService;

    private final com.fazquepaga.taskandpay.notification.NotificationService notificationService;
    private final com.fazquepaga.taskandpay.allowance.LedgerService ledgerService;
    private final Provider<com.fazquepaga.taskandpay.allowance.AllowanceService> allowanceServiceProvider;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            SubscriptionService subscriptionService,
            com.fazquepaga.taskandpay.notification.NotificationService notificationService,
            com.fazquepaga.taskandpay.allowance.LedgerService ledgerService,
            Provider<com.fazquepaga.taskandpay.allowance.AllowanceService> allowanceServiceProvider) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
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
                        "Recurring task limit reached for Free tier. Upgrade to Premium for"
                                + " unlimited tasks.");
            }
        }

        Task task = Task.builder()
                .description(request.getDescription())
                .type(request.getType())
                .weight(request.getWeight())
                .value(java.math.BigDecimal.ZERO) // Will be recalculated
                .requiresProof(request.isRequiresProof())
                .createdAt(Instant.now())
                .dayOfWeek(request.getDayOfWeek())
                .scheduledDate(request.getScheduledDate())
                .status(Task.TaskStatus.PENDING)
                .build();

        taskRepository.save(userId, task).get();

        // Automatically recalculate all task values based on allowance distribution
        allowanceServiceProvider.get().recalculateTaskValues(userId);

        // Return updated task with calculated value
        List<Task> updatedTasks = getTasksByUserId(userId);
        return updatedTasks.stream()
                .filter(t -> t.getId().equals(task.getId()))
                .findFirst()
                .orElse(task);
    }

    public List<Task> getTasksByUserId(String userId)
            throws ExecutionException, InterruptedException {
        return getAllTasksRaw(userId).stream()
                .filter(task -> task.getArchived() == null || !task.getArchived())
                .collect(Collectors.toList());
    }

    private List<Task> getAllTasksRaw(String userId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = taskRepository.findTasksByUserId(userId).get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(Task.class)).collect(Collectors.toList());
    }

    public Task approveTask(String taskId, String parentId)
            throws ExecutionException, InterruptedException {
        return null;
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

        List<Task> tasks = getAllTasksRaw(childId);
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
        task.setAcknowledged(true); // Manually approved, so acknowledged
        taskRepository.save(childId, task).get();

        // Send notification
        try {
            notificationService.sendTaskApproved(task, child);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add transaction
        ledgerService.addTransaction(
                childId,
                value,
                "Task approved: " + task.getDescription(),
                com.fazquepaga.taskandpay.allowance.Transaction.TransactionType.CREDIT);

        return task;
    }

    public Task completeTask(String taskId, String childId)
            throws ExecutionException, InterruptedException {

        // Verify child exists
        User child = userRepository.findByIdSync(childId);
        if (child == null || child.getRole() != User.Role.CHILD) {
            throw new IllegalArgumentException("Child not found");
        }

        // Find task
        List<Task> tasks = getAllTasksRaw(childId);
        Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Check if task is already completed or approved
        if (task.getStatus() == Task.TaskStatus.COMPLETED
                || task.getStatus() == Task.TaskStatus.PENDING_APPROVAL
                || task.getStatus() == Task.TaskStatus.APPROVED) {
            throw new IllegalStateException("Task is already completed");
        }

        // Update status based on proof requirement
        if (task.isRequiresProof()) {
            task.setStatus(Task.TaskStatus.PENDING_APPROVAL);
        } else {
            // Auto-approve tasks that don't require proof
            task.setStatus(Task.TaskStatus.APPROVED);
            task.setAcknowledged(false);

            // Calculate value and add transaction
            java.math.BigDecimal value = allowanceServiceProvider.get().calculateValueForTask(childId, taskId);
            ledgerService.addTransaction(
                    childId,
                    value,
                    "Task completed: " + task.getDescription(),
                    com.fazquepaga.taskandpay.allowance.Transaction.TransactionType.CREDIT);
        }

        taskRepository.save(childId, task).get();

        // Send notification to parent
        try {
            User parent = userRepository.findByIdSync(child.getParentId());
            if (parent != null) {
                notificationService.sendTaskCompleted(task, child, parent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return task;
    }

    public Task acknowledgeTask(String childId, String taskId, String parentId)
            throws ExecutionException, InterruptedException {
        // Verify parent rights (simplified for now, ideally check parent-child
        // relation)
        User parent = userRepository.findByIdSync(parentId);
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("User is not a parent");
        }

        List<Task> tasks = getAllTasksRaw(childId);
        Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setAcknowledged(true);
        taskRepository.save(childId, task).get();
        return task;
    }

    public Task rejectTask(String childId, String taskId, String parentId)
            throws ExecutionException, InterruptedException {
        User parent = userRepository.findByIdSync(parentId);
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("User is not a parent");
        }

        List<Task> tasks = getAllTasksRaw(childId);
        Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Only allow rejecting APPROVED tasks (that were auto-approved or manually
        // approved)
        if (task.getStatus() != Task.TaskStatus.APPROVED) {
            throw new IllegalStateException("Can only reject approved tasks");
        }

        // Calculate value to reverse
        java.math.BigDecimal value = allowanceServiceProvider.get().calculateValueForTask(childId, taskId);

        // Reverse transaction (Debit)
        ledgerService.addTransaction(
                childId,
                value,
                "Task rejected by parent: " + task.getDescription(),
                com.fazquepaga.taskandpay.allowance.Transaction.TransactionType.DEBIT);

        task.setStatus(Task.TaskStatus.PENDING);
        task.setAcknowledged(true); // Logic: Parent acted on it.

        taskRepository.save(childId, task).get();
        return task;
    }

    private int countRecurringTasks(String userId) throws ExecutionException, InterruptedException {
        List<Task> tasks = getTasksByUserId(userId);
        return (int) tasks.stream()
                .filter(
                        task -> task.getType() == Task.TaskType.DAILY
                                || task.getType() == Task.TaskType.WEEKLY)
                .count();
    }

    /** Updates the value of a task (used by automatic redistribution). */
    public void updateTaskValue(String childId, Task task)
            throws ExecutionException, InterruptedException {
        taskRepository.save(childId, task).get();
    }

    public void deleteTask(String childId, String taskId, String parentId)
            throws ExecutionException, InterruptedException {
        User parent = userRepository.findByIdSync(parentId);
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("User is not a parent");
        }

        List<Task> tasks = getAllTasksRaw(childId);
        Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (Boolean.TRUE.equals(task.getArchived())) {
            return; // Already deleted
        }

        task.setArchived(true);
        taskRepository.save(childId, task).get();
    }
}
