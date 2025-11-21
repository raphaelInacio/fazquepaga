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
}
