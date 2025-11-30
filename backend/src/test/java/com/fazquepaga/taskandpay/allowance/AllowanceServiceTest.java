package com.fazquepaga.taskandpay.allowance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AllowanceServiceTest {

    @Mock private TaskService taskService;

    @Mock private UserRepository userRepository;

    @Mock private AllowanceCalculator allowanceCalculator;

    private AllowanceService allowanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        allowanceService = new AllowanceService(allowanceCalculator, taskService, userRepository);
    }

    @Test
    void testCalculatePredictedAllowance() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        User child = User.builder().id(childId).build();

        Task task1 =
                Task.builder()
                        .status(Task.TaskStatus.APPROVED)
                        .value(new BigDecimal("10.00"))
                        .build();

        Task task2 =
                Task.builder()
                        .status(Task.TaskStatus.PENDING_APPROVAL)
                        .value(new BigDecimal("5.00"))
                        .build();

        Task task3 =
                Task.builder()
                        .status(Task.TaskStatus.PENDING)
                        .value(new BigDecimal("20.00"))
                        .build();

        List<Task> tasks = Arrays.asList(task1, task2, task3);

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(tasks);

        // When
        BigDecimal predictedAllowance = allowanceService.calculatePredictedAllowance(childId);

        // Then
        assertEquals(new BigDecimal("15.00"), predictedAllowance);
    }

    @Test
    void testCalculatePredictedAllowanceChildNotFound()
            throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        when(userRepository.findByIdSync(childId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            allowanceService.calculatePredictedAllowance(childId);
                        });
        assertEquals("Child not found", exception.getMessage());
    }

    @Test
    void testCalculateValueForTask() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        String taskId = "task1";
        User child = User.builder().id(childId).monthlyAllowance(new BigDecimal("100.00")).build();
        Task task = Task.builder().id(taskId).type(Task.TaskType.DAILY).build();
        List<Task> tasks = Collections.singletonList(task);

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(tasks);
        when(allowanceCalculator.calculateTaskValue(
                        task, child.getMonthlyAllowance(), tasks, YearMonth.now()))
                .thenReturn(new BigDecimal("10.00"));

        // When
        BigDecimal taskValue = allowanceService.calculateValueForTask(childId, taskId);

        // Then
        assertEquals(new BigDecimal("10.00"), taskValue);
    }

    @Test
    void testCalculateValueForTaskChildNotFound() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        String taskId = "task1";
        when(userRepository.findByIdSync(childId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            allowanceService.calculateValueForTask(childId, taskId);
                        });
        assertEquals("Child not found", exception.getMessage());
    }

    @Test
    void testCalculateValueForTaskNotFound() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        String taskId = "task1";
        User child = User.builder().id(childId).build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(Collections.emptyList());

        // When & Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            allowanceService.calculateValueForTask(childId, taskId);
                        });
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void testRecalculateTaskValues() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        User child = User.builder().id(childId).monthlyAllowance(new BigDecimal("100.00")).build();
        Task task1 = Task.builder().type(Task.TaskType.DAILY).weight(Task.TaskWeight.LOW).build();
        Task task2 =
                Task.builder().type(Task.TaskType.WEEKLY).weight(Task.TaskWeight.MEDIUM).build();
        List<Task> tasks = Arrays.asList(task1, task2);

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(tasks);

        // When
        allowanceService.recalculateTaskValues(childId);

        // Then
        verify(taskService, times(2)).updateTaskValue(eq(childId), any(Task.class));
    }

    @Test
    void testRecalculateTaskValuesNoChild() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        when(userRepository.findByIdSync(childId)).thenReturn(null);

        // When
        allowanceService.recalculateTaskValues(childId);

        // Then
        verify(taskService, never()).updateTaskValue(any(), any());
    }

    @Test
    void testRecalculateTaskValuesNoMonthlyAllowance()
            throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        User child = User.builder().id(childId).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When
        allowanceService.recalculateTaskValues(childId);

        // Then
        verify(taskService, never()).updateTaskValue(any(), any());
    }

    @Test
    void testRecalculateTaskValuesNoTasks() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        User child = User.builder().id(childId).monthlyAllowance(new BigDecimal("100.00")).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(Collections.emptyList());

        // When
        allowanceService.recalculateTaskValues(childId);

        // Then
        verify(taskService, never()).updateTaskValue(any(), any());
    }

    @Test
    void testRecalculateTaskValuesWithNullWeight() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        User child = User.builder().id(childId).monthlyAllowance(new BigDecimal("100.00")).build();
        Task task1 = Task.builder().type(Task.TaskType.DAILY).weight(null).build();
        List<Task> tasks = Collections.singletonList(task1);

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(tasks);

        // When
        allowanceService.recalculateTaskValues(childId);

        // Then
        verify(taskService, times(1)).updateTaskValue(eq(childId), any(Task.class));
    }

    @Test
    void testRecalculateTaskValuesWithNullType() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child1";
        User child = User.builder().id(childId).monthlyAllowance(new BigDecimal("100.00")).build();
        Task task1 = Task.builder().type(null).weight(Task.TaskWeight.LOW).build();
        List<Task> tasks = Collections.singletonList(task1);

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(taskService.getTasksByUserId(childId)).thenReturn(tasks);

        // When
        allowanceService.recalculateTaskValues(childId);

        // Then
        verify(taskService, times(1)).updateTaskValue(eq(childId), any(Task.class));
    }
}
