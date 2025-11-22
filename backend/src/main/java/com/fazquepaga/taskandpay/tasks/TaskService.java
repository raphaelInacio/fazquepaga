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
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final SubscriptionService subscriptionService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
            SubscriptionService subscriptionService) {

        this.taskRepository = taskRepository;

        this.userRepository = userRepository;

        this.subscriptionService = subscriptionService;
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

    private int countRecurringTasks(String userId) throws ExecutionException, InterruptedException {
        List<Task> tasks = getTasksByUserId(userId);
        return (int) tasks.stream()
                .filter(task -> task.getType() == Task.TaskType.DAILY || task.getType() == Task.TaskType.WEEKLY)
                .count();
    }
}
