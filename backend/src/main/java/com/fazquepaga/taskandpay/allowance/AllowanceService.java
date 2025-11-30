package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AllowanceService {

    private final AllowanceCalculator allowanceCalculator;
    private final TaskService taskService;
    private final UserRepository userRepository;

    public AllowanceService(
            AllowanceCalculator allowanceCalculator,
            TaskService taskService,
            UserRepository userRepository) {
        this.allowanceCalculator = allowanceCalculator;
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    public BigDecimal calculateValueForTask(String childId, String taskId)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }

        List<Task> allTasks = taskService.getTasksByUserId(childId);
        Task targetTask =
                allTasks.stream()
                        .filter(t -> t.getId().equals(taskId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        YearMonth currentMonth = YearMonth.now(); // Default to current month

        // Filter tasks relevant for this month
        List<Task> activeTasks =
                allTasks.stream()
                        .filter(t -> isTaskActiveForMonth(t, currentMonth))
                        .collect(Collectors.toList());

        return allowanceCalculator.calculateTaskValue(
                targetTask, child.getMonthlyAllowance(), activeTasks, currentMonth);
    }

    private boolean isTaskActiveForMonth(Task task, YearMonth yearMonth) {
        if (task.getType() == Task.TaskType.ONE_TIME) {
            if (task.getScheduledDate() == null) return false;
            YearMonth taskMonth =
                    YearMonth.from(task.getScheduledDate().atZone(ZoneId.systemDefault()));
            return taskMonth.equals(yearMonth);
        }
        // DAILY and WEEKLY are assumed active if they exist
        return true;
    }

    public BigDecimal calculatePredictedAllowance(String childId)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }

        List<Task> allTasks = taskService.getTasksByUserId(childId);

        // Sum the value of PENDING_APPROVAL and APPROVED tasks
        BigDecimal predictedTotal =
                allTasks.stream()
                        .filter(
                                t ->
                                        t.getStatus() == Task.TaskStatus.PENDING_APPROVAL
                                                || t.getStatus() == Task.TaskStatus.APPROVED)
                        .map(t -> t.getValue() != null ? t.getValue() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return predictedTotal;
    }

    /**
     * Recalculates task values based on monthly allowance, weight, and type. Redistributes the
     * allowance proportionally among all tasks.
     */
    public void recalculateTaskValues(String childId)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null || child.getMonthlyAllowance() == null) {
            return; // No allowance = no calculation
        }

        BigDecimal monthlyAllowance = child.getMonthlyAllowance();
        List<Task> allTasks = taskService.getTasksByUserId(childId);

        if (allTasks.isEmpty()) {
            return; // No tasks = no distribution
        }

        // Calculate total points
        int totalPoints = calculateTotalPoints(allTasks);
        if (totalPoints == 0) {
            return;
        }

        // Value per point
        BigDecimal valuePerPoint =
                monthlyAllowance.divide(
                        BigDecimal.valueOf(totalPoints), 4, java.math.RoundingMode.HALF_EVEN);

        // Distribute value to each task
        for (Task task : allTasks) {
            int taskPoints = calculateTaskPoints(task);
            BigDecimal taskValue =
                    valuePerPoint
                            .multiply(BigDecimal.valueOf(taskPoints))
                            .setScale(2, java.math.RoundingMode.HALF_EVEN);
            task.setValue(taskValue);

            // Save task with new value
            taskService.updateTaskValue(childId, task);
        }
    }

    private int calculateTotalPoints(List<Task> tasks) {
        return tasks.stream().mapToInt(this::calculateTaskPoints).sum();
    }

    private int calculateTaskPoints(Task task) {
        int occurrences = getOccurrences(task.getType());
        int weightPoints = getWeightPoints(task.getWeight());
        return occurrences * weightPoints;
    }

    private int getOccurrences(Task.TaskType type) {
        if (type == null) return 1;
        switch (type) {
            case DAILY:
                return 30;
            case WEEKLY:
                return 4;
            case ONE_TIME:
                return 1;
            default:
                return 1;
        }
    }

    private int getWeightPoints(Task.TaskWeight weight) {
        if (weight == null) return 1;
        switch (weight) {
            case HIGH:
                return 3;
            case MEDIUM:
                return 2;
            case LOW:
                return 1;
            default:
                return 1;
        }
    }
}
