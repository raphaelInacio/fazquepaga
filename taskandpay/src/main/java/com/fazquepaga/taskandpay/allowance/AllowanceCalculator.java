package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.tasks.Task;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public interface AllowanceCalculator {
    BigDecimal calculateTaskValue(Task task, BigDecimal monthlyAllowance, List<Task> allTasksForMonth, YearMonth yearMonth);
}
