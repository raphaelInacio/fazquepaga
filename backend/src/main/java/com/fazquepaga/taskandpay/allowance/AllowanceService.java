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
        Task targetTask = allTasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        YearMonth currentMonth = YearMonth.now(); // Default to current month

        // Filter tasks relevant for this month
        List<Task> activeTasks = allTasks.stream()
                .filter(t -> isTaskActiveForMonth(t, currentMonth))
                .collect(Collectors.toList());

        return allowanceCalculator.calculateTaskValue(
                targetTask, child.getMonthlyAllowance(), activeTasks, currentMonth);
    }

    private boolean isTaskActiveForMonth(Task task, YearMonth yearMonth) {
        if (task.getType() == Task.TaskType.ONE_TIME) {
            if (task.getScheduledDate() == null)
                return false;
            YearMonth taskMonth = YearMonth.from(task.getScheduledDate().atZone(ZoneId.systemDefault()));
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
        YearMonth currentMonth = YearMonth.now();

        // Filter tasks active for this month
        List<Task> activeTasks = allTasks.stream()
                .filter(t -> isTaskActiveForMonth(t, currentMonth))
                .collect(Collectors.toList());

        // Calculate total possible points
        long totalPointsPossible = allowanceCalculator.calculateTotalPointsPossible(activeTasks, currentMonth);

        if (totalPointsPossible == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyAllowance = child.getMonthlyAllowance();
        if (monthlyAllowance == null || monthlyAllowance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal valuePerPoint = monthlyAllowance.divide(
                BigDecimal.valueOf(totalPointsPossible), 4, java.math.RoundingMode.HALF_EVEN);

        // Calculate value of completed/approved tasks
        BigDecimal predictedTotal = BigDecimal.ZERO;

        for (Task task : activeTasks) {
            if (task.getStatus() == Task.TaskStatus.COMPLETED
                    || task.getStatus() == Task.TaskStatus.APPROVED) {
                int points = allowanceCalculator.getPointsForWeight(task.getWeight());
                long occurrences = 0;

                // For simplicity in prediction, we count 1 occurrence for ONE_TIME
                // For DAILY/WEEKLY, we should ideally count how many times it was completed.
                // However, the current Task model seems to treat DAILY/WEEKLY as single
                // definitions
                // that generate multiple instances or just track status?
                // Looking at Task.java, it seems to be a single entity.
                // If the system creates a new Task entity for each occurrence, then we just sum
                // them up.
                // If it's a definition, we need to know how many times it was done.
                // The PRD says "parents assign value... track completion".
                // The current codebase has `TaskStatus`.
                // If a DAILY task is "COMPLETED", does it mean for today?
                // Assuming for this MVP that we are summing up individual task instances that
                // are completed.

                // Wait, AllowanceCalculatorImpl.calculateTaskValue calculates value for a
                // single task instance based on its weight relative to ALL tasks in the month.
                // So if I have 1 Daily task (30 occurrences) and 1 One-Time task (1
                // occurrence).
                // Total points = 30 * Weight + 1 * Weight.
                // Value of the One-Time task = (Allowance / Total Points) * Weight.

                // So to get predicted total, I need to sum the value of all *completed* tasks.
                // But `calculateTaskValue` needs `allTasksForMonth` to calculate the unit
                // value.

                BigDecimal taskValue = allowanceCalculator.calculateTaskValue(task, monthlyAllowance, activeTasks,
                        currentMonth);
                predictedTotal = predictedTotal.add(taskValue);
            }
        }

        return predictedTotal;
    }
}
