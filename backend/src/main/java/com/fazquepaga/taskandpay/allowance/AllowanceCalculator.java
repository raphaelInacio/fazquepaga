package com.fazquepaga.taskandpay.allowance;

public interface AllowanceCalculator {
    java.math.BigDecimal calculateTaskValue(
            com.fazquepaga.taskandpay.tasks.Task task,
            java.math.BigDecimal monthlyAllowance,
            java.util.List<com.fazquepaga.taskandpay.tasks.Task> allTasksForMonth,
            java.time.YearMonth yearMonth);

    long calculateTotalPointsPossible(
            java.util.List<com.fazquepaga.taskandpay.tasks.Task> tasks,
            java.time.YearMonth yearMonth);

    int getPointsForWeight(com.fazquepaga.taskandpay.tasks.Task.TaskWeight weight);
}
