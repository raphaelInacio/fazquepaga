package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.tasks.Task;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AllowanceCalculatorImpl implements AllowanceCalculator {

    private static final int POINTS_LOW = 1;
    private static final int POINTS_MEDIUM = 5;
    private static final int POINTS_HIGH = 20;

    @Override
    public BigDecimal calculateTaskValue(
            Task task,
            BigDecimal monthlyAllowance,
            List<Task> allTasksForMonth,
            YearMonth yearMonth) {
        if (monthlyAllowance == null || monthlyAllowance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        if (allTasksForMonth == null || allTasksForMonth.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        long totalPointsPossible = calculateTotalPointsPossible(allTasksForMonth, yearMonth);

        if (totalPointsPossible == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        BigDecimal valuePerPoint =
                monthlyAllowance.divide(
                        BigDecimal.valueOf(totalPointsPossible), 4, RoundingMode.HALF_EVEN);
        int taskPoints = getPointsForWeight(task.getWeight());

        return valuePerPoint
                .multiply(BigDecimal.valueOf(taskPoints))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public long calculateTotalPointsPossible(List<Task> tasks, YearMonth yearMonth) {
        long totalPoints = 0;
        int daysInMonth = yearMonth.lengthOfMonth();

        for (Task t : tasks) {
            int points = getPointsForWeight(t.getWeight());
            long occurrences = 0;

            if (t.getType() == null) {
                continue;
            }
            switch (t.getType()) {
                case DAILY:
                    occurrences = daysInMonth;
                    break;
                case WEEKLY:
                    if (t.getDayOfWeek() != null) {
                        occurrences = countDayOfWeekInMonth(t.getDayOfWeek(), yearMonth);
                    }
                    break;
                case ONE_TIME:
                    occurrences = 1;
                    break;
                default:
                    occurrences = 0;
                    break;
            }
            totalPoints += (points * occurrences);
        }
        return totalPoints;
    }

    @Override
    public int getPointsForWeight(Task.TaskWeight weight) {
        if (weight == null) return 0;
        switch (weight) {
            case LOW:
                return POINTS_LOW;
            case MEDIUM:
                return POINTS_MEDIUM;
            case HIGH:
                return POINTS_HIGH;
            default:
                return 0;
        }
    }

    private long countDayOfWeekInMonth(int dayOfWeekIso, YearMonth yearMonth) {
        long count = 0;
        LocalDate date = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int i = 0; i < daysInMonth; i++) {
            if (date.getDayOfWeek().getValue() == dayOfWeekIso) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
