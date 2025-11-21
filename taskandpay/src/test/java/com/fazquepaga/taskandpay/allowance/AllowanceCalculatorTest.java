package com.fazquepaga.taskandpay.allowance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fazquepaga.taskandpay.tasks.Task;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AllowanceCalculatorTest {

    private AllowanceCalculatorImpl calculator;

    @BeforeEach
    void setUp() {
        calculator = new AllowanceCalculatorImpl();
    }

    @Test
    void calculateTaskValue_SimpleDaily() {
        YearMonth nov2023 = YearMonth.of(2023, 11); // 30 days
        BigDecimal allowance = new BigDecimal("300.00");

        Task dailyTask =
                Task.builder()
                        .type(Task.TaskType.DAILY)
                        .weight(Task.TaskWeight.LOW) // 1 point
                        .build();

        List<Task> tasks = Collections.singletonList(dailyTask);

        // Total points = 1 * 30 = 30
        // Value per point = 300 / 30 = 10
        // Task value = 1 * 10 = 10

        BigDecimal value = calculator.calculateTaskValue(dailyTask, allowance, tasks, nov2023);
        assertEquals(new BigDecimal("10.00"), value);
    }

    @Test
    void calculateTaskValue_Weekly_4Occurrences() {
        YearMonth nov2023 = YearMonth.of(2023, 11); // Nov 2023 has 4 Mondays
        BigDecimal allowance = new BigDecimal("100.00");

        Task weeklyTask =
                Task.builder()
                        .type(Task.TaskType.WEEKLY)
                        .weight(Task.TaskWeight.MEDIUM) // 5 points
                        .dayOfWeek(1) // Monday
                        .build();

        List<Task> tasks = Collections.singletonList(weeklyTask);

        // Total points = 5 * 4 = 20
        // Value per point = 100 / 20 = 5
        // Task value = 5 * 5 = 25

        BigDecimal value = calculator.calculateTaskValue(weeklyTask, allowance, tasks, nov2023);
        assertEquals(new BigDecimal("25.00"), value);
    }

    @Test
    void calculateTaskValue_Weekly_5Occurrences() {
        YearMonth nov2023 = YearMonth.of(2023, 11); // Nov 2023 has 5 Wednesdays
        BigDecimal allowance = new BigDecimal("100.00");

        Task weeklyTask =
                Task.builder()
                        .type(Task.TaskType.WEEKLY)
                        .weight(Task.TaskWeight.MEDIUM) // 5 points
                        .dayOfWeek(3) // Wednesday
                        .build();

        List<Task> tasks = Collections.singletonList(weeklyTask);

        // Total points = 5 * 5 = 25
        // Value per point = 100 / 25 = 4
        // Task value = 5 * 4 = 20

        BigDecimal value = calculator.calculateTaskValue(weeklyTask, allowance, tasks, nov2023);
        assertEquals(new BigDecimal("20.00"), value);
    }

    @Test
    void calculateTaskValue_Mixed() {
        YearMonth nov2023 = YearMonth.of(2023, 11); // 30 days
        BigDecimal allowance = new BigDecimal("700.00");

        Task daily =
                Task.builder()
                        .type(Task.TaskType.DAILY)
                        .weight(Task.TaskWeight.LOW)
                        .build(); // 1 pt * 30 = 30
        Task weekly =
                Task.builder()
                        .type(Task.TaskType.WEEKLY)
                        .weight(Task.TaskWeight.MEDIUM)
                        .dayOfWeek(1)
                        .build(); // 5 pts * 4 (Mon) = 20
        Task oneTime =
                Task.builder()
                        .type(Task.TaskType.ONE_TIME)
                        .weight(Task.TaskWeight.HIGH)
                        .build(); // 20 pts * 1 = 20

        List<Task> tasks = Arrays.asList(daily, weekly, oneTime);

        // Total points = 30 + 20 + 20 = 70
        // Value per point = 700 / 70 = 10

        assertEquals(
                new BigDecimal("10.00"),
                calculator.calculateTaskValue(daily, allowance, tasks, nov2023)); // 1 * 10
        assertEquals(
                new BigDecimal("50.00"),
                calculator.calculateTaskValue(weekly, allowance, tasks, nov2023)); // 5 * 10
        assertEquals(
                new BigDecimal("200.00"),
                calculator.calculateTaskValue(oneTime, allowance, tasks, nov2023)); // 20 * 10
    }

    @Test
    void calculateTaskValue_ZeroAllowance() {
        YearMonth nov2023 = YearMonth.of(2023, 11);
        Task daily = Task.builder().type(Task.TaskType.DAILY).weight(Task.TaskWeight.LOW).build();
        List<Task> tasks = Collections.singletonList(daily);

        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculateTaskValue(daily, BigDecimal.ZERO, tasks, nov2023));
    }

    @Test
    void calculateTaskValue_NoTasks() {
        YearMonth nov2023 = YearMonth.of(2023, 11);
        BigDecimal allowance = new BigDecimal("100.00");
        Task daily = Task.builder().type(Task.TaskType.DAILY).weight(Task.TaskWeight.LOW).build();

        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculateTaskValue(daily, allowance, Collections.emptyList(), nov2023));
    }

    @Test
    void calculateTaskValue_NullWeight() {
        YearMonth nov2023 = YearMonth.of(2023, 11);
        BigDecimal allowance = new BigDecimal("100.00");
        Task taskWithNullWeight = Task.builder().type(Task.TaskType.DAILY).weight(null).build();
        List<Task> tasks = Collections.singletonList(taskWithNullWeight);

        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculateTaskValue(taskWithNullWeight, allowance, tasks, nov2023));
    }

    @Test
    void calculateTaskValue_NullType() {
        YearMonth nov2023 = YearMonth.of(2023, 11);
        BigDecimal allowance = new BigDecimal("100.00");
        Task taskWithNullType = Task.builder().type(null).weight(Task.TaskWeight.LOW).build();
        List<Task> tasks = Collections.singletonList(taskWithNullType);

        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculateTaskValue(taskWithNullType, allowance, tasks, nov2023));
    }

    @Test
    void calculateTaskValue_Weekly_NullDayOfWeek() {
        YearMonth nov2023 = YearMonth.of(2023, 11);
        BigDecimal allowance = new BigDecimal("100.00");
        Task weeklyTask =
                Task.builder()
                        .type(Task.TaskType.WEEKLY)
                        .weight(Task.TaskWeight.MEDIUM)
                        .dayOfWeek(null)
                        .build();
        List<Task> tasks = Collections.singletonList(weeklyTask);

        assertEquals(
                new BigDecimal("0.00"),
                calculator.calculateTaskValue(weeklyTask, allowance, tasks, nov2023));
    }

    @Test
    void calculateTaskValue_February() {
        YearMonth feb2024 = YearMonth.of(2024, 2); // 29 days
        BigDecimal allowance = new BigDecimal("290.00");
        Task daily =
                Task.builder()
                        .type(Task.TaskType.DAILY)
                        .weight(Task.TaskWeight.LOW)
                        .build(); // 1 pt
        List<Task> tasks = Collections.singletonList(daily);
        // Total points = 1 * 29 = 29
        // Value per point = 290 / 29 = 10
        // Task value = 1 * 10 = 10

        assertEquals(
                new BigDecimal("10.00"),
                calculator.calculateTaskValue(daily, allowance, tasks, feb2024));
    }

    @Test
    void calculateTaskValue_Rounding() {
        YearMonth nov2023 = YearMonth.of(2023, 11);
        BigDecimal allowance = new BigDecimal("100.00");
        Task daily =
                Task.builder()
                        .type(Task.TaskType.DAILY)
                        .weight(Task.TaskWeight.LOW)
                        .build(); // 1 pt * 30 days = 30 points
        List<Task> tasks = Collections.singletonList(daily);
        // Total points = 30
        // Value per point = 100 / 30 = 3.3333...
        // Task value = 1 * 3.3333 = 3.33

        assertEquals(
                new BigDecimal("3.33"),
                calculator.calculateTaskValue(daily, allowance, tasks, nov2023));
    }
}
